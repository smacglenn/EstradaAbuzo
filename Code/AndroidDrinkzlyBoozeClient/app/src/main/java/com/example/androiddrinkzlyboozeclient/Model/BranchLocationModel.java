package com.example.androiddrinkzlyboozeclient.Model;

public class BranchLocationModel {
    private double lat,lng;

    public BranchLocationModel() {
    }

    public BranchLocationModel(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
