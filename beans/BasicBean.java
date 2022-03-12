package org.example.beans;

import java.time.LocalDateTime;

public class BasicBean {
    private String id;

    private LocalDateTime time;

    private long deltaSecond;

    private double latitude;

    private double longitude;

    private double deltaLatitude;

    private double deltaLongitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public double getDeltaLatitude() {
        return deltaLatitude;
    }

    public void setDeltaLatitude(double deltaLatitude) {
        this.deltaLatitude = deltaLatitude;
    }

    public double getDeltaLongitude() {
        return deltaLongitude;
    }

    public void setDeltaLongitude(double deltaLongitude) {
        this.deltaLongitude = deltaLongitude;
    }

    public long getDeltaSecond() {
        return deltaSecond;
    }

    public void setDeltaSecond(long deltaSecond) {
        this.deltaSecond = deltaSecond;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BasicBean{" +
                "id=" + id +
                ", time=" + time +
                ", deltaSecond=" + deltaSecond +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", deltaLatitude=" + deltaLatitude +
                ", deltaLongitude=" + deltaLongitude +
                '}';
    }
}
