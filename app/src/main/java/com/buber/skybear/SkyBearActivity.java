package com.buber.skybear;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class SkyBearActivity extends SingleFragmentActivity {
    private static final String TAG = "SkyBearActivity";

    @Override
    protected Fragment createFragment() {
        return SkyBearFragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (errorCode != ConnectionResult.SUCCESS) {
            Log.i(TAG, "No Connection to Google Play Services, Hard Exit!");
            finish();
        }
    }
}
