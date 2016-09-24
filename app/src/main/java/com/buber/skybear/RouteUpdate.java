package com.buber.skybear;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by o on 9/22/2016.
 */
public class RouteUpdate {
    private List<LatLng> mLocationList;
    private List<Long> mUpdateTimeList;
    private int mIndex;
    private boolean mTripFinished;

    public RouteUpdate(List<LatLng> loclist, List<Long> timelist) {
        mLocationList = loclist;
        mUpdateTimeList = timelist;
    }

    public RouteUpdate() {
        mLocationList = new ArrayList<LatLng>();
        mUpdateTimeList = new ArrayList<Long>();
    }

    public void insert(LatLng location, long time) {
        mLocationList.add(location);
        mUpdateTimeList.add(time);
// put in first and last
    }

    public LatLng get() {
        LatLng retval = null;
        Long time = new Long(0);
        return this.get(time);
    }

    public LatLng get(Long time) {
        LatLng retval = null;
        time = null;
        if (!mTripFinished && mLocationList.size() > 0 && mIndex < mLocationList.size()) {
            retval = mLocationList.get(mIndex);
            time = mUpdateTimeList.get(mIndex);
            mIndex++;
            if (mIndex >= mLocationList.size()) {
                mTripFinished = true;
            }
        }
        return retval;
    }

    public LatLng getFirstLocation() {
        if (mLocationList.size() > 0) {
            return mLocationList.get(0);
        } else {
            return null;
        }
    }

    public LatLng getFinalLocation() {
        if (mLocationList.size() > 0) {
            return mLocationList.get(mLocationList.size() - 1);
        } else {
            return null;
        }
    }

    public int getSize() {
        return mLocationList.size();
    }

    public int getIndex() {
        return mIndex;
    }

    public long deltaTime() {
        if (mIndex > 0 && mIndex < mUpdateTimeList.size())
            return mUpdateTimeList.get(mIndex) - mUpdateTimeList.get(mIndex - 1);
        else
            return -1;
    }

    public boolean isDone() {
        return mTripFinished;
    }
}
