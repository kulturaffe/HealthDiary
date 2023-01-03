package com.example.healthdiary.dataTypes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Represents a real-world location. coordinates are always degrees. name is typically in english but can be a local name.
 */
public class Location implements Parcelable {
    private double lat, lon;
    private String name;

    public void setLat(double lat) {
        this.lat = lat;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public void setCoordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    /**
     * @return double[2] in the order: Latitude, Longitude
     */
    public double[] getCoordinates(){return new double[]{lat, lon};}

    public Location(){
        lat = 48.23925;
        lon = 16.37811;
        name = "Vienna";
    }

    public Location(double latitude, double longitude, String name){
        lat=latitude;
        lon=longitude;
        this.name = name;
    }
    public Location(double latitude, double longitude){
        lat=latitude;
        lon=longitude;
    }
    public Location(String name){
        this.name = name;
    }

    protected Location(Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
        name = in.readString();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeString(name);
    }
}
