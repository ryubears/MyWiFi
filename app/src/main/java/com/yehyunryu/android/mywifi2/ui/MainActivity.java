package com.yehyunryu.android.mywifi2.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.yehyunryu.android.mywifi2.R;
import com.yehyunryu.android.mywifi2.utils.Geofencing;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.main_nav_view) NavigationView mNavigationView;
    @BindView(R.id.main_drawer_layout) DrawerLayout mDrawerLayout;

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //place picker intent id
    public static final int PLACE_PICKER_REQUEST = 500;

    //index to identify current nav menu item
    public static int sNavItemIndex = 0;

    //tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_PLACES = "places";
    private static final String TAG_SETTINGS = "settings";
    public static String sCurrentTag = TAG_HOME;

    //toolbar titles respected to selected nav menu item
    private String[] mActivityTitles;

    //flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    //GoogleApiClient to access Google Apis
    public GoogleApiClient mGoogleApiClient;
    public Geofencing mGeofencing;

    public Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find and attach toolbar
        mAppBarLayout = findViewById(R.id.main_app_bar_layout);
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        //find and bind views
        ButterKnife.bind(this);

        //handles runnable objects and process them in a separate thread
        mHandler = new Handler();

        //load toolbar titles from string resources
        mActivityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        //initializing navigation menu
        setUpNavigationView();

        //use default values if first time opening
        if(savedInstanceState == null) {
            sNavItemIndex = 0;
            sCurrentTag = TAG_HOME;
            loadFragment();
        }

        //build Google Api Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        //create geofencing object
        mGeofencing = new Geofencing(this, mGoogleApiClient);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
        mGoogleApiClient.disconnect();
    }

    //loads selected fragment
    public void loadFragment() {
        //select appropriate menu item
        selectNavMenu();

        //set appropriate toolbar title
        setToolbarTitle();

        //set appropriate toolbar background
        setToolbarBackground();

        //checks to see if user selected current fragment; if so, just closes drawer
        if(getSupportFragmentManager().findFragmentByTag(sCurrentTag) != null) {
            mDrawerLayout.closeDrawers();
            return;
        }

        //replaces fragment with a fade in/out animation on a separate thread
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.main_frame, fragment, sCurrentTag);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        //check if runnable already exists, then add to queue
        if(mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        //close drawers
        mDrawerLayout.closeDrawers();

        //refresh toolbar menu
        invalidateOptionsMenu();
    }

    //return current fragment
    private Fragment getFragment() {
        switch(sNavItemIndex) {
            case 0:
                return new HomeFragment();
            case 1:
                return new PlacesFragment();
            case 2:
                return new SettingsFragment();
            default:
                return new HomeFragment();
        }
    }

    //set appropriate toolbar title
    private void setToolbarTitle() {
        getSupportActionBar().setTitle(mActivityTitles[sNavItemIndex]);
    }

    //set appropriate menu for fragment
    private void selectNavMenu() {
        mNavigationView.getMenu().getItem(sNavItemIndex).setChecked(true);
    }

    //set appropriate background and elevation for toolbar
    private void setToolbarBackground() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(sNavItemIndex == 0) {
                mToolbar.getBackground().setAlpha(0);
                mAppBarLayout.setElevation(0);
            } else {
                mToolbar.getBackground().setAlpha(255);
                mAppBarLayout.setElevation(8);
            }
        }
    }

    //set navigation item selection listener
    private void setUpNavigationView() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        sNavItemIndex = 0;
                        sCurrentTag = TAG_HOME;
                        break;
                    case R.id.nav_places:
                        sNavItemIndex = 1;
                        sCurrentTag = TAG_PLACES;
                        break;
                    case R.id.nav_settings:
                        sNavItemIndex = 2;
                        sCurrentTag = TAG_SETTINGS;
                        break;
                    case R.id.nav_contact_us:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                        return true;
                    case R.id.nav_privacy_policy:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
                        return true;
                    default:
                        sNavItemIndex = 0;
                }

                //check menu if it weren't
                if(!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                }

                //load fragment
                loadFragment();

                return true;
            }
        });

        //handles closing and opening of drawer
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        //set drawer toggle to drawer
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        //necessary for hamburger icon to show
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        //closes drawer if open
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        //loads home fragment
        if(shouldLoadHomeFragOnBackPress) {
            if(sNavItemIndex != 0) {
                sNavItemIndex = 0;
                sCurrentTag = TAG_HOME;
                loadFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "API Client Connection Successful");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "API Client Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "API Client Connection Failed");
    }
}
