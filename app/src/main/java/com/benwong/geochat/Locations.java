package com.benwong.geochat;

/**
 * Created by benwong on 2016-03-05.
 */
public class Locations {
    String id;
    String latitude;
    String longitude;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "id:"+this.getId ()+",  Latitude:"+this.getLatitude ()+",  Longitude:"+this.getLongitude ()+"\n";
    }
}
