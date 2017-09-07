package com.yehyunryu.android.mywifi2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.yehyunryu.android.mywifi2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.main_nav_view) NavigationView mNavigationView;
    @BindView(R.id.main_drawer_layout) DrawerLayout mDrawerLayout;

    private Toolbar mToolbar;

    private String LOG_TAG = MainActivity.class.getSimpleName();

    //index to identify current nav menu item
    public static int mNavItemIndex = 0;

    //tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_PLACES = "places";
    private static final String TAG_SETTINGS = "settings";
    public static String CURRENT_TAG = TAG_HOME;

    //toolbar titles respected to selected nav menu item
    private String[] mActivityTitles;

    //flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    //GoogleApiClient to access Google Apis
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find and attach toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
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
            mNavItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
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
    }

    //loads selected fragment
    private void loadFragment() {
        //select appropriate menu item
        selectNavMenu();

        //set appropriate toolbar title
        setToolbarTitle();

        //checks to see if user selected current fragment; if so, just closes drawer
        if(getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
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
                fragmentTransaction.replace(R.id.main_frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        //check if runnable is already running, then add to queue
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
        switch(mNavItemIndex) {
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
        getSupportActionBar().setTitle(mActivityTitles[mNavItemIndex]);
    }

    //set appropriate menu for fragment
    private void selectNavMenu() {
        mNavigationView.getMenu().getItem(mNavItemIndex).setChecked(true);
    }

    //set navigation item selection listener
    private void setUpNavigationView() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        mNavItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_places:
                        mNavItemIndex = 1;
                        CURRENT_TAG = TAG_PLACES;
                        break;
                    case R.id.nav_settings:
                        mNavItemIndex = 2;
                        CURRENT_TAG = TAG_SETTINGS;
                        break;
                    case R.id.nav_contact_us:
                        startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                        mDrawerLayout.closeDrawers();
                        return true;
                    case R.id.nav_privacy_policy:
                        startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
                        mDrawerLayout.closeDrawers();
                        return true;
                    default:
                        mNavItemIndex = 0;
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
            if(mNavItemIndex != 0) {
                mNavItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflates menu for home fragment
        if(mNavItemIndex == 0) {
            getMenuInflater().inflate(R.menu.home, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handles menu selection for home fragment
        if(item.getItemId() == R.id.home_add_menu) {
            Toast.makeText(getApplicationContext(), "Add Place", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
