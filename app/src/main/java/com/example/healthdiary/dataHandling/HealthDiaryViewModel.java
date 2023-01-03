package com.example.healthdiary.dataHandling;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.Location;

import java.util.ArrayList;
import java.util.List;

public class HealthDiaryViewModel extends ViewModel {
    private final MutableLiveData<HealthDiaryPatient> currentPatient = new MutableLiveData<>();
    private final MutableLiveData<List<HealthDiaryPatient>> allPatients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<PatientSelectionState> selectionState = new MutableLiveData<>(PatientSelectionState.NONE);
    private final MutableLiveData<Boolean> cancellable = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> selectedListItem = new MutableLiveData<>(-2);

    public LiveData<Integer> getSelectedListItem() {
        return selectedListItem;
    }
    public void setSelectedListItem(int selection){
        selectedListItem.setValue(selection);
    }

    public LiveData<HealthDiaryPatient> getCurrentPatient(){
        return currentPatient;
    }
    public void setCurrentPatient(HealthDiaryPatient newPatient){
        currentPatient.setValue(newPatient);
    }

    public LiveData<List<HealthDiaryPatient>> getAllPatients(){
        return allPatients;
    }
    public void setAllPatients(List<HealthDiaryPatient> allPatients){
        this.allPatients.setValue(allPatients);
    }

    public LiveData<Location> getLocation(){
        return currentLocation;
    }
    public void setLocation(Location newLocation){
        currentLocation.setValue(newLocation);
    }

    public LiveData<PatientSelectionState> getSelectionState(){
        return selectionState;
    }
    public void setSelectionState(PatientSelectionState newState){
        selectionState.setValue(newState);
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable.setValue(cancellable);
    }
    public boolean getCancellable(){
        return Boolean.TRUE.equals(cancellable.getValue());
    }

    public enum PatientSelectionState {
        NONE, CREATE_NEW, LOGIN, CANCELLED, DONE, ADD
    }
}
