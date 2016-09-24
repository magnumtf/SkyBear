package com.buber.skybear;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by o on 9/15/2016.
 */
public class SkyBearFragment extends SupportMapFragment implements LatLngInterpolator, MyListener  {
    private static final String TAG = "SkyBearFragment";

    private static final String mFilename = "inLocDat.txt";
    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private File myExternalFile;
    private FileInputStream mFIn;
    private InputStreamReader mWhy;
    private BufferedReader mBufferedReader;
    private Marker mHome;
    private Marker mRhumbLine;
    private MarkerAnimation mMarkerAnimation;
    private int mNumberOfAnimations = 2;
    private int mAnimationCnts = 0;
    private LatLng mLocation_1;
    private LatLng mLocation_2;
    private LatLng mLocation_3;
    private LatLng mLocation_4;
    private LatLng mLocation_5;
    private LatLng mLastLocation;
    private Location mCurrentLocation;
    private RouteUpdate mRouteUpdate = new RouteUpdate();

    public static SkyBearFragment newInstance() {
        return new SkyBearFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "Connection Suspended");
                    }
                })
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                    @Override
                    public void onCameraChange(CameraPosition arg0) {
                        // Move camera.
                        mLocation_1 = mRouteUpdate.get();
                        mLocation_2 = mRouteUpdate.get();
                        mLocation_3 = mRouteUpdate.getFinalLocation();
                        Long duration = mRouteUpdate.deltaTime();
                        Log.i(TAG, "onCameraChange: setDuration = " + duration.toString());
                        duration = (long)2000;
                        if (mLocation_1 == null || mLocation_2 == null || duration < 0) {
                            mLocation_1 = new LatLng(42.65868, -70.62126);
                            mLocation_2 = new LatLng(42.66861, -70.66861);
                            duration = (long)9000;
                        }
                        mMarkerAnimation.setDuration(duration);
                        mHome = mMap.addMarker(new MarkerOptions().position(mLocation_1).title("Home"));
                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(mLocation_1)
                                .include(mLocation_3)
                                .build();
                        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, margin));
                        startTripAnimation(mLocation_2);
                        mLastLocation = mLocation_2;
                        // Remove listener to prevent position reset on camera move.
                        mMap.setOnCameraChangeListener(null);
                    }
                });

//                mHome = mMap.addMarker(new MarkerOptions().position(mLocation_1).title("Home"));
            }
        });

        mMarkerAnimation = new MarkerAnimation(this);
        mLocation_1 = new LatLng(42.65868, -70.62126);
        mLocation_2 = new LatLng(42.66861, -70.66861);
        mLocation_3 = new LatLng(32.79525, -116.96197);
        mLocation_4 = new LatLng(-33.89318, 18.63091);
        mLocation_5 = new LatLng(36.75739, 3.08574);
        Date t = new Date();
        LatLng currentLocation;
        // Open File
        String sCurrentLine;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        myExternalFile = new File(path, mFilename);
        try {
            mFIn = new FileInputStream(myExternalFile);
        } catch (IOException ioe) {
            Log.i(TAG, "onCreate: File Open Error " + ioe.toString());
        }
        mWhy = new InputStreamReader(mFIn);
        mBufferedReader = new BufferedReader(mWhy);
        try {
            while ((sCurrentLine = mBufferedReader.readLine()) != null) {
                currentLocation = getLocationAndTime(sCurrentLine, t);
                if ((currentLocation != null) && (t != null)) {
                    mRouteUpdate.insert(currentLocation, t.getTime());
                }
            }
        } catch (IOException ioe) {
            Log.i(TAG, "onCreate: File Read Error " + ioe.toString());
        } finally {
            try {
                mBufferedReader.close();
                mWhy.close();
                mFIn.close();
            } catch (IOException ioe) {
                Log.i(TAG, "onCreate: File Close Error " + ioe.toString());
            }
        }
        mBufferedReader = null;
        mWhy = null;
        mFIn = null;
        myExternalFile = null;

    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        mClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_sky_bear, menu);

        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());

        MenuItem animateItem = menu.findItem(R.id.action_simulate);
        animateItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                findText();
                updateUI();
                return true;
            case R.id.action_simulate:
                startTripAnimation(mLocation_2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findText() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        boolean permissionCheck = checkLocationPermission();
        if (permissionCheck) {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mClient, request, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i(TAG, "Got a fix: " + location);
                            mCurrentLocation = location;
                        }
                    });
        }
    }

    private void updateUI() {
        if (mMap == null)
            return;

        MarkerOptions myMarker = new MarkerOptions()
                .position(mLocation_1);
//        mHome = mMap.addMarker(new MarkerOptions().position(mLocation_1).title("Home"));
//        mRhumbLine = mMap.addMarker(new MarkerOptions().position(mLocation_2).title("Bar"));

        mMap.clear();
        mMap.addMarker(myMarker);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(mLocation_1)
                .include(mLocation_2)
                .build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);
    }

    private boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void startTripAnimation(LatLng destination) {
        Log.i(TAG, "startTripAnimation()");
        mMarkerAnimation.animateMarkerToICS(mHome, destination, this);
    }

    @Override
    public LatLng interpolate(float fraction, LatLng a, LatLng b) {
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lngDelta = b.longitude - a.longitude;

        // Take the shortest path across the 180th meridian.
        if (Math.abs(lngDelta) > 180) {
            lngDelta -= Math.signum(lngDelta) * 360;
        }
        double lng = lngDelta * fraction + a.longitude;
        return new LatLng(lat, lng);
    }

    @Override
    public void callback(State state) {
        LatLng destination;
        LatLng curretLocation;
        int currentTime;
        Long deltaTime;
        int index;
        if (state == State.DONE) {
            Log.i(TAG, "MyListener.callback(): Animation Done " + mRouteUpdate.getSize());
//            Log.i(TAG, "MyListener.callback(): Location 1 Time = " + mUpdateTimeList.get(0).toString());
//            Log.i(TAG, "MyListener.callback(): Location 1 Location = " + mLocationList.get(0).toString());
            if (!mRouteUpdate.isDone()) {
                destination = mRouteUpdate.get();
                deltaTime = mRouteUpdate.deltaTime();
                index = mRouteUpdate.getIndex();
                if (index == 16 || index == 24 || index == 28 || index == 30) {
                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(mLastLocation)
                            .include(mLocation_3)
                            .build();
                    int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, margin));
                }

                Log.i(TAG, "AnimationDone: setDuration = " + deltaTime.toString() + " for " + index);
//                mMarkerAnimation.setDuration(deltaTime);
                startTripAnimation(destination);
                mLastLocation = destination;
            }
            mAnimationCnts += 1;
//            if (mAnimationCnts <= mNumberOfAnimations) {
//                if ((mAnimationCnts % 2) == 0) {
//                    destination = mLocation_2;
//                } else {
//                    destination = mLocation_1;
//                }
//            }
        } else if (state == State.RUNNING) {
            Log.i(TAG, "MyListener.callback(): Animation Started");
        }
    }

    private LatLng getLocationAndTime(String line, Date dtime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String[] chunks = line.split(",");
        dtime = null;
        Double lat;
        Double lon;
        int err = 0;
        LatLng retLatLng = null;

        if (chunks.length == 3) {
            try {
                dtime = sdf.parse(chunks[0].trim());
            } catch (ParseException pe) {
                Log.i(TAG, "getLocationAndTime: Parse Exception " + pe.toString());
                err = 1;
            }
            if (err == 0) {
                lat = Double.valueOf(chunks[1].trim());
                lon = Double.valueOf(chunks[2].trim());
                retLatLng = new LatLng(lat, lon);
            }
        }
        if (err > 0) {
            dtime = null;
        }

        return retLatLng;
    }

/*
    private class SearchTask extends AsyncTask<Location, Void, Void> {
        private Location mLocation;

        @Override
        protected Void doInBackground(Location... params) {
            mLocation = params[0];
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mCurrentLocation = mLocation;
        }
    }
    */
}
