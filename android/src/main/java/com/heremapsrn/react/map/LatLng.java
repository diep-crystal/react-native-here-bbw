package com.heremapsrn.react.map;

public class LatLng {
    public double lat, lng;

    LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "{\"lat\":" + lat + ",\"lng\":" + lng+"}";
    }
}
