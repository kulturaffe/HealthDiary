package com.example.healthdiary.dataHandling;

import androidx.lifecycle.MutableLiveData;

import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.Location;
import com.google.android.material.internal.Experimental;


/**
 * singleton data repository for shared access to liveData between activities.
 * i know this should not be done with livedata but it still works...
 */
@Experimental
public class DataRepository {
    private static final DataRepository instance = new DataRepository();
    private MutableLiveData<HealthDiaryPatient> currentPatient;
    private MutableLiveData<Location> currentLocation;

    private DataRepository(){}
    public static DataRepository getInstance(){return instance;}

    public MutableLiveData<HealthDiaryPatient> getPatient(){
        if (currentPatient == null) {
            currentPatient = new MutableLiveData<>();
        }
        return currentPatient;
    }
    public MutableLiveData<Location> getLocation(){
        return currentLocation;
    }

    /*

    public void setCurrentPatient(HealthDiaryPatient patient){
        currentPatient.set(patient);
    }

    public void setCurrentLocation(Location location){
        currentLocation.set(location);
    }

     */
}
