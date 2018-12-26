package com.heremapsrn.react.map;

import android.util.Log;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import javax.annotation.Nullable;

class HereMapManager extends ViewGroupManager<HereMapView> {

    private static final int COMMAND_ZOOM_IN = 1;
    private static final int COMMAND_ZOOM_OUT = 2;
    private static final int COMMAND_SET_CENTER = 3;

    static final String REACT_CLASS = "HereMapView";

    private static final String TAG = HereMapManager.class.getSimpleName();

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected HereMapView createViewInstance(final ThemedReactContext reactContext) {
        return new HereMapView(reactContext, new HereCallback() {
            @Override
            public void onCallback(LatLng latLng) {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("HERE_MAP_ON_CHANGED", latLng.toString());
            }
        });
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        Log.d("React"," View manager getCommandsMap:");

        return MapBuilder.of(
                "zoomIn", COMMAND_ZOOM_IN,
                "zoomOut", COMMAND_ZOOM_OUT,
                "setCenter", COMMAND_SET_CENTER
                );
    }

    @Override
    public void receiveCommand(HereMapView view,
                               int commandType,
                               @Nullable ReadableArray args) {

        Assertions.assertNotNull(view);
        Assertions.assertNotNull(args);

        switch (commandType) {
            case COMMAND_ZOOM_IN: {
                double zoomLevel = args.getDouble(0);
                view.setZoomLevel(zoomLevel);
                return;
            }

            case COMMAND_ZOOM_OUT: {
                double zoomLevel = args.getDouble(0);
                view.setZoomLevel(zoomLevel);
                return;
            }

            case COMMAND_SET_CENTER: {
                String coordinate = args.getString(0);
                view.setCenter(coordinate);
                return;
            }

            default:
                throw new IllegalArgumentException(String.format(
                        "Unsupported command %d received by %s.",
                        commandType,
                        getClass().getSimpleName()));
        }
    }

    @ReactProp(name = "center")
    public void setCenter(HereMapView view, @Nullable String center) {
        view.setCenter(center);
    }

    @ReactProp(name = "mapType")
    public void setMapType(HereMapView view, @Nullable String type) {
        view.setCenter(type);
    }

    @ReactProp(name = "initialZoom", defaultDouble = 10.0)
    public void setZoomLevel(HereMapView view, double zoomLevel) {
        Log.d(TAG, "======================= ZOOM " + zoomLevel);
        view.setZoomLevel(zoomLevel);
    }
}
