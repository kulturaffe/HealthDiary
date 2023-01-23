package com.example.healthdiary.dataTypes;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class HealthDiaryMeasurement implements Parcelable {
    protected final String unit;
    protected final long timeStamp;
    protected final long patientId;
    protected long id = 0;

    public HealthDiaryMeasurement(String unit, long patientId) {
        this.unit = unit;
        this.patientId = patientId;
        this.timeStamp = System.currentTimeMillis();
    }
    public HealthDiaryMeasurement(String unit, long patientId , long timeStamp) {
        this.unit = unit;
        this.patientId = patientId;
        this.timeStamp = timeStamp;
    }

    public HealthDiaryMeasurement(String unit, long patientId , long timeStamp, long id) {
        this.unit = unit;
        this.patientId = patientId;
        this.timeStamp = timeStamp;
        this.id = id;
    }

    public long getPatientId() {return patientId;}

    public long getId() {return id;}

    // returns self to enable chaining
    public HealthDiaryMeasurement setId(long id){
        this.id = id;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    // human readable version of timestamp
    public String getDate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Instant.ofEpochMilli(timeStamp).toString();
        } else {
            /*
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat iso8601date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            iso8601date.setTimeZone(TimeZone.getTimeZone("UTC"));
            return iso8601date.format(new Date(timeStamp));
             */
            Date d = new Date(timeStamp);
            return String.format(Locale.ENGLISH,"%tFT%tT.%tLZ",d,d,d);
        }
    }

    abstract String toValueOnlyString();

}
