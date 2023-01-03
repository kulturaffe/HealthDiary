package com.example.healthdiary.dataTypes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class BodyMassReading extends HealthDiaryMeasurement implements Parcelable {
    private final double mass;
    public static final String loinc = "29463-7";

    public BodyMassReading(double weight){
        super(Double.isNaN(weight) ? "N/A" :
                "kg",-1,System.currentTimeMillis());
        mass = weight;
    }
    public BodyMassReading(double weight, long patientId){
        super("kg", patientId);
        mass = weight;
    }

    public BodyMassReading(double weight, String unit, long patientId){
        super(unit,patientId,System.currentTimeMillis());
        mass = weight;
    }
    public BodyMassReading(double weight,long patientId, long ts){
        super("kg",patientId,ts);
        mass = weight;
    }
    public BodyMassReading(double weight, String unit,long patientId, long ts){
        super(unit,patientId,ts);
        mass = weight;
   }
    public BodyMassReading(double weight, String unit,long patientId, long ts, long id){
        super(unit,patientId,ts,id);
        mass = weight;
    }

    protected BodyMassReading(Parcel in) {
        super(in.readString(), in.readLong(), in.readLong(), in.readLong());
        mass = in.readDouble();
    }

    public double getMass(){ return mass; }

    @Nullable // only works properly if all readings have the same unit as first
    public static BodyMassReading calcAverage(List<BodyMassReading> readings){
        if(readings.size()>0){
            double sum = 0;
            for (BodyMassReading reading : readings) {
                sum += reading.getMass();
            }
            return new BodyMassReading(sum/readings.size(),readings.get(0).getUnit(), readings.get(0).getPatientId());
        } else return null;
    }


    @Nullable
    @Override
    public String toValueOnlyString(){
        if(!Double.isNaN(mass) && mass > 0 && mass < 1e3)
            return String.format(Locale.ENGLISH,"%.2f %s",mass, unit);
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        if (!Double.isNaN(mass) && mass > 0 && mass < 1e3)
            return String.format(Locale.ENGLISH, "%.2f %s\u0009at %s", mass, unit, this.getDate());
        return "null";
    }

    public static final Creator<BodyMassReading> CREATOR = new Creator<BodyMassReading>() {
        @Override
        public BodyMassReading createFromParcel(Parcel in) {
            return new BodyMassReading(in);
        }

        @Override
        public BodyMassReading[] newArray(int size) {
            return new BodyMassReading[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(unit);
        parcel.writeLong(patientId);
        parcel.writeLong(timeStamp);
        parcel.writeLong(id);
        parcel.writeDouble(mass);
    }
}
