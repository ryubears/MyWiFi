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

/**
 * Copyright 2017 Yehyun Ryu

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class PrivacyPolicyActivity extends AppCompatActivity {
    @BindView(R.id.privacy_link) Button mPrivacyLinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        ButterKnife.bind(this);

        //set appropriate action bar background
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F7C43D"))); //colorPrimary
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.privacy_link)
    public void onPrivacyLinkClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/policies/privacy/"));
        startActivity(intent);
    }
}
