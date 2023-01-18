package com.example.healthdiary.dataTypes;

import static java.lang.Double.isNaN;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * units of temperature can be changed in the future, coordinates are meant to always be in degrees <br>
 * patientId is always set to -1 since temperature can be used for every patient
 */
public class TemperatureReading extends HealthDiaryMeasurement implements Parcelable {
    private final double temp;
    private final HealthDiaryLocation location;
    public static final String loinc = "LP101925-8"; // only the Temperature.ambient component since there is no code for outdoor-temp

    protected TemperatureReading(Parcel in) {
        super(in.readString(), in.readLong(), in.readLong(), in.readLong());
        temp = in.readDouble();
        location = new HealthDiaryLocation(in.readDouble(),in.readDouble(),in.readString());
    }

    public double getTemperature(){return temp;}
    public String getLocationName(){return location.getName();}
    /**
     * wrapper around the {@link HealthDiaryLocation#getCoordinates()}-method.
     * @return double[2] in the order: Latitude, Longitude
     */
    public double[] getCoordinates(){return location.getCoordinates();}

    /**
     * 'default' constructor, the only one for invalid values.
     * @param temp temperature in [degC] or NaN for invalid reading
     */
    public TemperatureReading(double temp){
        super(isNaN(temp)?"N/A":"\u00b0C",-1);
        this.temp = temp;
        if(!isNaN(temp)){
            location = new HealthDiaryLocation();
        } else{
            location = new HealthDiaryLocation(Double.NaN,Double.NaN,"");
        }

    }

    public TemperatureReading(double temp, double lat, double lon, String loc, long timeStamp) {
        super("\u00b0C",-1, timeStamp);
        this.temp = temp;
        location = new HealthDiaryLocation(lat,lon,loc);
    }

    public TemperatureReading(double temperature, String unit, double latitude, double longitude, String locationName, long timeStamp) {
        super(unit,-1,timeStamp);
        temp = temperature;
        location = new HealthDiaryLocation(latitude,longitude,locationName);
    }

    @NonNull
    @Override
    public String toString() {
        return (Double.isNaN(temp) || temp < -459.67 || temp > 1.41e32) ? "null" :
                String.format(Locale.ENGLISH,"%.2f %s in %s (%f,%f) at %s", temp, unit, location.getName(), location.getLat(), location.getLon(), this.getDate());

    }

    @Nullable
    @Override
    public String toValueOnlyString(){
        return (Double.isNaN(temp) || temp < -459.67 || temp > 1.41e32) ? null :
                String.format(Locale.ENGLISH,"%.2f %s, %s, %s", temp, unit, location.getName(), this.getDate());
    }

    public static final Creator<TemperatureReading> CREATOR = new Creator<TemperatureReading>() {
        @Override
        public TemperatureReading createFromParcel(Parcel in) {
            return new TemperatureReading(in);
        }

        @Override
        public TemperatureReading[] newArray(int size) {
            return new TemperatureReading[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(unit);
        dest.writeLong(patientId);
        dest.writeLong(timeStamp);
        dest.writeLong(id);
        dest.writeDouble(temp);
        dest.writeDouble(location.getLat());
        dest.writeDouble(location.getLon());
        dest.writeString(location.getName());
    }
}
