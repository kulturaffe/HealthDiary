package com.example.healthdiary.dataTypes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * This class shall be used to contain a reading of
 * systolic and diastolic blood pressure readings, as
 * well as the corresponding timestamp when the measurement
 * was taken
 *
 * Please extend this class with necessary functions (e.g.
 * constructor and/or getter and setter)
 */
public class BloodPressureReading extends HealthDiaryMeasurement implements Parcelable {
    private final int sys;
    private final int dia;
    public static final String loinc = "55284-4";

    // needed for Parcelable-interface in order to be quickly and safely passed between activities as intent-extras
    public static final Creator<BloodPressureReading> CREATOR = new Creator<BloodPressureReading>() {
        @Override
        public BloodPressureReading createFromParcel(Parcel in) {
            return new BloodPressureReading(in);
        }

        @Override
        public BloodPressureReading[] newArray(int size) {
            return new BloodPressureReading[size];
        }
    };

    public int getSys() {
        return sys;
    }

    public int getDia() {
        return dia;
    }

    public BloodPressureReading(boolean bool){
        super("N/A", -1);
        sys = -1;
        dia = -1;
    }

    public BloodPressureReading(int sys, int dia, long patientId){
        super("mmHg", patientId);
        this.sys = sys;
        this.dia = dia;
    }
    public BloodPressureReading(int sys, int dia, String unit, long patientId){
        super(unit, patientId);
        this.sys = sys;
        this.dia = dia;
    }
    // handy constructor for selected date
    public BloodPressureReading(int s, int d, long patientId, long ts){
        super("mmHg", patientId, ts);
        sys = s;
        dia = d;
    }
    public BloodPressureReading(int s, int d, String u, long patientId, long ts){
        super(u, patientId, ts);
        sys = s;
        dia = d;
    }
    public BloodPressureReading(int s, int d, String u, long patientId, long ts, long id){
        super(u, patientId, ts, id);
        sys = s;
        dia = d;
    }
    // for Parcelable-interface
    public BloodPressureReading(Parcel in){
        super(in.readString(), in.readLong(), in.readLong(), in.readLong());
        sys = in.readInt();
        dia = in.readInt();
    }

    @Nullable // only works properly if all readings have the same unit as first
    public static BloodPressureReading calcAverage(List<BloodPressureReading> readings){
        int sumSys = 0, sumDia = 0, i = 0;
        if (readings != null) {
            for (BloodPressureReading reading : readings) {
                sumSys += reading.getSys();
                sumDia += reading.getDia();
                i++;
            }
        } else {
            return null;
        }

        if (i > 0)
            return new BloodPressureReading(sumSys / i, sumDia / i, readings.get(0).getUnit(), readings.get(0).getPatientId());
        return null;
    }

    @Nullable
    @Override
    public String toValueOnlyString(){
        if (sys > 0 && dia > 0 && sys < 500 && dia < 400)
            return String.format(Locale.ENGLISH,"%d/%d %s",sys,dia,unit);
        return null;
    }


    @NonNull
    @Override
    public String toString() {
        if(sys > 0 && dia > 0 && sys < 500 && dia < 400)
            return String.format(Locale.ENGLISH,"%d/%d %s\u0009at %s",sys,dia,unit,this.getDate());
        return "null";
    }

    // both for Parcelable-interface
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
        parcel.writeInt(sys);
        parcel.writeInt(dia);
    }
}
