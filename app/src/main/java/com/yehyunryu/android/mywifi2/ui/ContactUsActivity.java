package com.yehyunryu.android.mywifi2.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.yehyunryu.android.mywifi2.R;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        //set appropriate action bar background
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F7C43D"))); //colorPrimary
    }
}
