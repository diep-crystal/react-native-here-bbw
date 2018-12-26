package com.heremapsrn.react.map;

import android.content.Context;
import android.util.Log;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.MapView;
import com.heremapsrn.R;

import java.io.IOException;

public class HereMapView extends MapView {

    private static final String TAG = HereMapView.class.getSimpleName();

    private static final String MAP_TYPE_NORMAL = "normal";
    private static final String MAP_TYPE_SATELLITE = "satellite";

    private Map map;

    private GeoCoordinate mapCenter;
    private String mapType = "normal";

    private boolean mapIsReady = false;

    private double zoomLevel = 15;
    private MapMarker currentPin;
    private HereCallback callback;

    public HereMapView(Context context, HereCallback callback) {
        super(context);
        this.callback = callback;

        MapEngine.getInstance().init(context, new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (Error.NONE == error) {
                    Log.i(TAG, "-----------------------------------------------------------------");
                    Log.i(TAG, "Initialization ok -----------------------------------------------");
                    Log.i(TAG, "-----------------------------------------------------------------");

                    map = new Map();
                    setMap(map);

                    GeoCoordinate center = map.getCenter();
                    Log.d(TAG, String.format("Map center: %s, %s",
                            center.getLongitude(), center.getLongitude()));

                    map.setMapScheme(Map.Scheme.NORMAL_DAY);

                    mapIsReady = true;

                    if (center != null) map.setCenter(mapCenter, Map.Animation.LINEAR);

                    Log.d(TAG, String.format("mapType: %s", mapType));
                    setMapType(mapType);

                    setZoomLevel(zoomLevel);

                    initControl();

                } else {
                    Log.e(TAG, String.format("Error initializing map: %s", error.getDetails()));
                }
            }
        });

    }

    public void initControl() {
        if (map == null) {
            return;
        }
        Map.OnTransformListener onTransformListener = new Map.OnTransformListener() {
            @Override
            public void onMapTransformStart() {
            }

            @Override
            public void onMapTransformEnd(MapState mapState) {
                if(callback != null){
                    callback.onCallback(getMapCenter());
                }
            }
        };
        map.addTransformListener(onTransformListener);
    }

    public LatLng getMapCenter(){
        GeoCoordinate center = map.getCenter();
        return new LatLng(center.getLatitude(), center.getLongitude());
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause...");
        MapEngine.getInstance().onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume...");
        MapEngine.getInstance().onResume();
    }

    public void setCenter(String center) {
        String[] values = center.split(",");

        if (values.length == 2) {
            double latitude = Double.parseDouble(values[0]);
            double longitude = Double.parseDouble(values[1]);

            mapCenter = new GeoCoordinate(latitude, longitude);
            if (mapIsReady && map != null) {
                map.setCenter(mapCenter, Map.Animation.LINEAR);
//                updateCenterPin(mapCenter);

            }
        } else {
            Log.w(TAG, String.format("Invalid center: %s", center));
        }
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
        if (!mapIsReady) return;

        if (mapType.equals(MAP_TYPE_NORMAL)) {
            map.setMapScheme(Map.Scheme.NORMAL_DAY);
        } else if (MAP_TYPE_SATELLITE.equals(mapType)) {
            map.setMapScheme(Map.Scheme.SATELLITE_DAY);
        }
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
        if (!mapIsReady) return;

        map.setZoomLevel(zoomLevel);
    }

    public void updateCenterPin(GeoCoordinate pin) {
        // Create a custom marker image
        com.here.android.mpa.common.Image myImage = new com.here.android.mpa.common.Image();
        try {
            myImage.setImageResource(R.mipmap.ic_pin_map1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (currentPin != null) {
            map.removeMapObject(currentPin);
        }
        currentPin = new MapMarker(pin, myImage);
        map.addMapObject(currentPin);
    }
}
