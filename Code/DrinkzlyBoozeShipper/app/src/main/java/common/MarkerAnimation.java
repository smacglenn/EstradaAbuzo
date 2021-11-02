package common;


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MarkerAnimation {
    public static void animateMarkerToGB(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterPolator latLngInterPolator)
    {
        LatLng startPosition = marker.getPosition();
        Handler handler = new Handler();
        long start = SystemClock.uptimeMillis();
        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        float durationInMs = 3000;

       handler.post(new Runnable() {
           long elapsed;
           float t,v;
           @Override
           public void run() {
                elapsed = SystemClock.uptimeMillis() - start;
                t=elapsed/durationInMs;
                v=interpolator.getInterpolation(t);

                marker.setPosition(latLngInterPolator.interpolate(v,startPosition,finalPosition));

                if(t<1)
                {
                    handler.postDelayed(this, 16);
                }
           }
       });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void animateMarkerToHC(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterPolator latLngInterPolator)
    {
        LatLng startLocation = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(animation -> {
            float v = animation.getAnimatedFraction();
            LatLng newPosition = latLngInterPolator.interpolate(v,startLocation,finalPosition);
            marker.setPosition(newPosition);
        });

        valueAnimator.setFloatValues(0,1);
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void animateMarkerToICS(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterPolator latLngInterPolator)
    {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float v, LatLng latLng, LatLng t1) {
                return latLngInterPolator.interpolate(v,latLng,t1);
            }
        };
        Property<Marker,LatLng> property = Property.of(Marker.class,LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker,property,typeEvaluator,finalPosition);
        animator.setDuration(3000);
        animator.start();
    }
}
