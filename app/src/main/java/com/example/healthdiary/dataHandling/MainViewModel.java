package com.example.healthdiary.dataHandling;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.Location;

public class MainViewModel extends ViewModel {
    private MutableLiveData<HealthDiaryPatient> currentPatient;
    private MutableLiveData<Location> currentLocation;

    public MutableLiveData<HealthDiaryPatient> getPatient(){
        if(currentPatient == null){
            currentPatient = new MutableLiveData<>();
        }
        //currentPatient.setValue(DataRepository.getInstance().getPatient().getValue());
        return currentPatient;
    }

    public MutableLiveData<Location> getLocation(){
        if(currentLocation == null){
            currentLocation = new MutableLiveData<>();
        }
        //currentLocation.setValue(DataRepository.getInstance().getLocation().getValue());
        return currentLocation;
    }

}
