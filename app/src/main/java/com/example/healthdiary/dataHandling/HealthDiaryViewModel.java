package com.example.healthdiary.dataHandling;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.HealthDiaryLocation;

import java.util.ArrayList;
import java.util.List;

public class HealthDiaryViewModel extends ViewModel {
    private final MutableLiveData<HealthDiaryPatient> currentPatient = new MutableLiveData<>();
    private final MutableLiveData<HealthDiaryPatient> newPatient = new MutableLiveData<>();
    private final MutableLiveData<List<HealthDiaryPatient>> allPatients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<HealthDiaryLocation> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<State> viewState = new MutableLiveData<>(State.NONE);
    private final MutableLiveData<Boolean> cancellable = new MutableLiveData<>(false);
    private MutableLiveData<String> medicationName = new MutableLiveData<>("");


    public LiveData<HealthDiaryPatient> getCurrentPatient(){
        return currentPatient;
    }
    public void setCurrentPatient(HealthDiaryPatient newCurrentPatient){
        currentPatient.setValue(newCurrentPatient);
    }

    public LiveData<HealthDiaryPatient> getNewPatient(){
        return newPatient;
    }
    public void setNewPatient(HealthDiaryPatient newNewPatient){
        newPatient.setValue(newNewPatient);
    }

    public LiveData<List<HealthDiaryPatient>> getAllPatients(){
        return allPatients;
    }
    public void setAllPatients(List<HealthDiaryPatient> allPatients){
        this.allPatients.setValue(allPatients);
    }

    public LiveData<HealthDiaryLocation> getLocation(){
        return currentLocation;
    }
    public void setLocation(HealthDiaryLocation newLocation){
        currentLocation.setValue(newLocation);
    }

    public LiveData<State> getState(){
        return viewState;
    }
    public void setState(State newState){
        viewState.setValue(newState);
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable.setValue(cancellable);
    }
    public boolean getCancellable(){
        return Boolean.TRUE.equals(cancellable.getValue());
    }

    public LiveData<String> getMedicationName(){
        return medicationName;
    }
    public void setMedicationName(String newMedicationName){
        medicationName.setValue(newMedicationName);
    }

    public enum State {
        NONE, CREATE_NEW, LOGIN, CANCELLED, NOT_AVAILABLE, ADD, DONE, RATIONALE_SEEN, PERMISSION_GRANTED, NO_LAST_LOCATION
    }
}
