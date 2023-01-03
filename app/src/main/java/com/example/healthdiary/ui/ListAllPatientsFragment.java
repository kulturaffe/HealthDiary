package com.example.healthdiary.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;

import java.util.List;
import java.util.Objects;

/**
 * simple fragment showing all patients and setting current patient with selected item
 */
public class ListAllPatientsFragment extends DialogFragment {
    HealthDiaryViewModel model;
    HealthDiaryPatient[] allPatients;
    String[] allPatientNames;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);
        setCancelable(model.getCancellable());
        List<HealthDiaryPatient> allPatientsList = model.getAllPatients().getValue();
        if(allPatientsList == null || allPatientsList.size() < 1){
            model.setSelectedListItem(-10); // lower than -5: no patients available
            dismiss();
        }

        int size = Objects.requireNonNull(allPatientsList).size(); // should be, because otherwise dismisses, so throwing NPE is okay
        allPatients = new HealthDiaryPatient[size];
        allPatientNames = new String[size];
        for (int i = 0; i < size; i++){
            allPatients[i] = allPatientsList.get(i);
            allPatientNames[i] = allPatientsList.get(i).toValueOnlyString();
        }

        builder.setItems(allPatientNames, (dialog, which) -> {
            model.setCurrentPatient(allPatients[which]);
            model.setSelectedListItem(which);
            dismiss();
        });
        return builder.create();
    }
}
