package com.app.android.work_manager.data;

import android.location.Location;

/**
 * @Author: Muhammad Hasnain Altaf
 * @Date: 07/02/2024
 */
public class LocationEvent {


    private Double averageSpeed;
    private Double totalDistance;
    private long totalTime;

    private Location location;


    public LocationEvent(Location location,Double totalDistance,Double averageSpeed,long totalTime) {
        this.totalDistance=totalDistance;
        this.location = location;
        this.averageSpeed=averageSpeed;
        this.totalTime=totalTime;
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

    public Double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }
}
