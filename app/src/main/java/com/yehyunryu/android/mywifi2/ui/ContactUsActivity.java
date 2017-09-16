package com.yehyunryu.android.mywifi2.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.yehyunryu.android.mywifi2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactUsActivity extends AppCompatActivity {
    @BindView(R.id.email_view) Button mEmailView;
    @BindView(R.id.facebook_view) Button mFacebookView;
    @BindView(R.id.github_view) Button mGithubView;
    @BindView(R.id.playstore_view) Button mPlaystoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        ButterKnife.bind(this);

        //set appropriate action bar background and up button
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F7C43D"))); //colorPrimary
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.email_view)
    public void onEmailClick() {
        //email intent
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"ryubearsdev@gmail.com"});
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick(R.id.facebook_view)
    public void onFacebookClick() {
        //facebook browser intent
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/josephyehyun.ryu"));
        startActivity(intent);
    }

    @OnClick(R.id.github_view)
    public void onGithubClick() {
        //github browser intent
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ryubears/MyWiFi2"));
        startActivity(intent);
    }

    @OnClick(R.id.playstore_view)
    public void onPlaystoreClick() {
        //play store intent
        String appPackageName = getPackageName();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            startActivity(intent);
        } catch(android.content.ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);
        }
    }
}
