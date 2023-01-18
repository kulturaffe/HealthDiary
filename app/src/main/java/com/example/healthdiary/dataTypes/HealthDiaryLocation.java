package com.example.healthdiary.dataTypes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Represents a real-world location. coordinates are always degrees. name is typically in english but can be a local name.
 */
public class HealthDiaryLocation implements Parcelable {
    private double lat = Double.NaN, lon = Double.NaN;
    private String name = null;
    private Status status;

    public HealthDiaryLocation(double latitude, double longitude, String name){
        lat=latitude;
        lon=longitude;
        this.name = name;
        status = validate();
    }
    public HealthDiaryLocation(double latitude, double longitude){
        lat=latitude;
        lon=longitude;
        status = validate();
    }
    public HealthDiaryLocation(String name){
        this.name = name;
        status = validate();
    }

    protected HealthDiaryLocation(Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
        name = in.readString();
        status = validate();
    }

    public HealthDiaryLocation setLat(double lat) {
        this.lat = lat;
        status = validate();
        return this;
    }
    public HealthDiaryLocation setLon(double lon) {
        this.lon = lon;
        status = validate();
        return this;
    }
    public HealthDiaryLocation setCoordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        status = validate();
        return this;
    }
    public HealthDiaryLocation setName(String name) {
        this.name = name;
        status = validate();
        return this;
    }

    public HealthDiaryLocation setStatus(Status newStatus) {
        if(newStatus == Status.INVALID ) status = newStatus;
        return this;
    }
    public Status getStatus() { return status; }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    /** @return double[2] in the order: Latitude, Longitude */
    public double[] getCoordinates(){return new double[]{lat, lon};}

    public HealthDiaryLocation(){
        lat = 48.23925;
        lon = 16.37811;
        name = "Vienna";
        status = Status.COMPLETE;
    }


    public String toValueOnlyString() {
        if(status == Status.COMPLETE)
            return name;
        else return status.getText();

    }

    public static final Creator<HealthDiaryLocation> CREATOR = new Creator<HealthDiaryLocation>() {
        @Override
        public HealthDiaryLocation createFromParcel(Parcel in) {
            return new HealthDiaryLocation(in);
        }

        @Override
        public HealthDiaryLocation[] newArray(int size) {
            return new HealthDiaryLocation[size];
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

    @NonNull
    @Override
    public String toString() {
        return "Location{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", name='" + name + '\'' +
                ", status=" +
                '}';
    }

    private Status validate(){
        return !Double.isNaN(lat) && !Double.isNaN(lon) && null != name && !"".equals(name) ?
                Status.COMPLETE :
                !Double.isNaN(lat) && !Double.isNaN(lon) ?
                        Status.MISSING_NAME :
                        "".equals(name) ?
                                Status.NO_NAME :
                                null != name?
                                        Status.MISSING_COORD :
                                        Status.INVALID;
    }

    public enum Status {
        MISSING_NAME("fetching name\u2026"),
        MISSING_COORD("fetching coordinates\u2026"),
        COMPLETE("complete!"),
        INVALID("invalid/none."),
        NO_NAME("no name found for coordinates");


        private final String text;
        Status(String text) {
            this.text = text;
        }
        public String getText(){return text;}
    }
}
