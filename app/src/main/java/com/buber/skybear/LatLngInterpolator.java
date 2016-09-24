package com.buber.skybear;

/**
 * Created by o on 9/11/2016.
 */

import com.google.android.gms.maps.model.LatLng;

public interface LatLngInterpolator {
    public LatLng interpolate(float fraction, LatLng a, LatLng b);

}
