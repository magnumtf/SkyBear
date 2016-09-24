package com.buber.skybear;

/**
 * Created by o on 9/11/2016.
 */

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MarkerAnimation {
    private static final String TAG = "MarkerAnimation";
    public ObjectAnimator mAnimator;
    State mState;
    int mDuration;
    MyListener ml;

    // constructor
    MarkerAnimation(MyListener ml) {
        this.ml = ml;
        mDuration = 3000;
        mState = State.IDLE;
    }

    static void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                marker.setPosition(newPosition);
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        mAnimator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        if(mAnimator != null) {
            mAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    Log.i(TAG, "Listener: Start Animation");
                    mState = State.RUNNING;
                    ml.callback(mState);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.i(TAG, "Listener: End Animation");
                    mState = State.DONE;
                    ml.callback(mState);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    Log.i(TAG, "Listener: Cancel Animation");
                    mState = State.DONE;
                    ml.callback(mState);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    Log.i(TAG, "Listener: Repeat Animation");
                }
            });
        }

        startAnimation(mDuration);
    }

    public void setDuration(long dur) {
        mDuration = (int)dur;
    }

    private void startAnimation(int duration) {
        mAnimator.setDuration(duration);
        mAnimator.start();
    }
}
