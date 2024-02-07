package com.app.android.work_manager.data;

import android.location.Location;

/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 07/02/2024
 */
public class LocationEvent {
    private Double latitude;
    private Double longitude;

    private Double averageSpeed;
    private Double totalDistance;
    private long totalTime;

    private Location location;


    public LocationEvent(Double latitude, Double longitude, Location location,Double totalDistance) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalDistance=totalDistance;
        this.location = location;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


}
