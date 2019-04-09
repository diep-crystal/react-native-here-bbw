package com.heremapsrn.react.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.MapView;
import com.heremapsrn.R;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Nullable;

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
    private ArrayList<MapObject> mapObjects;
    private Context _context;

    public HereMapView(Context context, HereCallback callback) {
        super(context);
        this.callback = callback;
        this._context = context;
        MapEngine.getInstance().init(context, new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (Error.NONE == error) {
                    Log.i(TAG, "-----------------------------------------------------------------");
                    Log.i(TAG, "Initialization ok -----------------------------------------------");
                    Log.i(TAG, "-----------------------------------------------------------------");

                    map = new Map();
                    setMap(map);

                    map.setMapScheme(Map.Scheme.NORMAL_DAY);

                    mapIsReady = true;

                    if (mapCenter != null) map.setCenter(mapCenter, Map.Animation.LINEAR);

                    enableMyLocation();

                    Log.d(TAG, String.format("mapType: %s", mapType));
                    setMapType(mapType);

                    setZoomLevel(zoomLevel);

                    updatePin();
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
                if (callback != null) {
                    callback.onCallback(getMapCenter());
                }
            }
        };
        map.addTransformListener(onTransformListener);
    }

    public LatLng getMapCenter() {
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
            Log.d(TAG, "======================= CENTER " + center);

            double latitude = Double.parseDouble(values[0]);
            double longitude = Double.parseDouble(values[1]);

            mapCenter = new GeoCoordinate(latitude, longitude);
            if (mapIsReady && map != null) {
                map.setCenter(mapCenter, Map.Animation.LINEAR);
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

    public void updatePin(){
        if (map != null && currentPin != null) {
            map.removeMapObject(currentPin);
            map.addMapObject(currentPin);
        }
        if(map != null && mapObjects != null) {
            map.removeMapObjects(mapObjects);
            map.addMapObjects(mapObjects);
        }
    }

    public void addMarker(String _marker) {
        String[] values = _marker.split(",");

        if (values.length == 2) {
            double latitude = Double.parseDouble(values[0]);
            double longitude = Double.parseDouble(values[1]);

            GeoCoordinate geo = new GeoCoordinate(latitude, longitude);

            com.here.android.mpa.common.Image myImage = new com.here.android.mpa.common.Image();
            try {
                myImage.setImageResource(R.mipmap.ic_pin_map);
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentPin = new MapMarker(geo, myImage);
            currentPin.setAnchorPoint(new PointF(myImage.getWidth()/2, myImage.getHeight()));
            updatePin();

        } else {
            Log.w(TAG, String.format("Invalid maker: %s", _marker));
        }
    }

    public void removeMapObjectOld() {
        if(map != null && mapObjects != null && mapObjects.size() > 0) {
            map.removeMapObjects(mapObjects);
        }
    }

    public void addMarkers(ReadableArray _markers) {
        // remove object old
        Log.w(TAG, String.format("addMarkers %s", _markers));
        this.removeMapObjectOld();
        if(_markers == null) return;

        mapObjects = new ArrayList<>();
        for (int i = 0; i < _markers.size(); i++) {
            ReadableMap values = _markers.getMap(i);

            double latitude = values.getDouble("lat");
            double longitude = values.getDouble("lng");
            String title = values.getString("name");
            String url = values.getString("avatar");
            Log.w(TAG, String.format("Latitude %s", latitude));
            Log.w(TAG, String.format("Longitude %s", longitude));
            GeoCoordinate geo = new GeoCoordinate(latitude, longitude);
            MapMarker _mapMarker = new MapMarker();
           // _mapMarker.setTitle(title);
           // _mapMarker.setDescription(title);
            //
            try {
                //
                if(url != null && (url.startsWith("http://") || url.startsWith("https://") ||
                        url.startsWith("file://") || url.startsWith("asset://"))){
                   this.setImage(url, geo, _mapMarker);
                } else {
                    //com.here.android.mpa.common.Image myImage = new com.here.android.mpa.common.Image();
                    //myImage.setImageResource(R.mipmap.ic_pin_map);
                    _mapMarker.setCoordinate(geo);
                    mapObjects.add(_mapMarker);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        this.updatePin();
    }

    public void enableMyLocation() {
        // Create a custom marker image
        com.here.android.mpa.common.Image myImage = new com.here.android.mpa.common.Image();
        try {
            myImage.setImageResource(R.mipmap.ic_location);
        } catch (IOException e) {
            e.printStackTrace();
        }

       // map.getPositionIndicator().setVisible(true);
        map.getPositionIndicator().setMarker(myImage);
    }

    public void showUserLocation(Boolean isShow) {
        if(map != null) {
            map.getPositionIndicator().setVisible(isShow);
        }
    }

    public void setImage(String url, final GeoCoordinate geo, final MapMarker _mapMarker) {

        try {
            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(url))
                    .build();
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
         final DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, this._context);
          dataSource.subscribe(new BaseBitmapDataSubscriber() {
              @Override
              protected void onNewResultImpl(@Nullable Bitmap bitmap) {
                if(dataSource.isFinished() && bitmap != null) {
                    com.here.android.mpa.common.Image myImage = new com.here.android.mpa.common.Image();
//                    LinearLayout LL = new LinearLayout(_context);
//                    LL.setOrientation(LinearLayout.VERTICAL);

                    Bitmap _bitmap = Bitmap.createBitmap(bitmap);
                    myImage.setBitmap(_bitmap);
                    _mapMarker.setIcon(myImage);
                    _mapMarker.setAnchorPoint(new PointF(_bitmap.getWidth() / 2, _bitmap.getHeight()));
                    _mapMarker.setCoordinate(geo);

                    mapObjects.add(_mapMarker);
                    Log.w(TAG, "onNewResultImpl -> width: " + String.valueOf(bitmap.getWidth()) + ", height: " + String.valueOf(bitmap.getHeight()));
                    dataSource.close();

                    updatePin();
                }
              }

              @Override
              protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                  if(dataSource != null) {
                      dataSource.close();
                  }
              }
          }, CallerThreadExecutor.getInstance());

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
