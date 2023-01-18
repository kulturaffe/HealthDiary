package com.example.healthdiary.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

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
    String[] allPatientNames;
    HealthDiaryPatient chosenPatient;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);
        setCancelable(model.getCancellable());
        List<HealthDiaryPatient> allPatientsList = model.getAllPatients().getValue();
        if(allPatientsList == null || allPatientsList.size() < 1){
            model.setState(HealthDiaryViewModel.State.NOT_AVAILABLE);
            dismiss();
        }

        int size = Objects.requireNonNull(allPatientsList).size(); // should be, because otherwise dismisses, so throwing NPE should be okay
        allPatientNames = new String[size];
        for (int i = 0; i < size; i++){
            allPatientNames[i] = allPatientsList.get(i).toValueOnlyString();
        }

        // preselect current patient or none
        int current = allPatientsList.indexOf(model.getCurrentPatient().getValue()); // -1 if not in list -> perfect!

        AlertDialog dialog = builder.setPositiveButton(android.R.string.ok, null)
                .setSingleChoiceItems(allPatientNames, current, (d, which) -> chosenPatient = allPatientsList.get(which))
                .create();

        // override the positive button to allow ok only when sthg is selected
        dialog.setOnShowListener(d -> {
            Button button = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (chosenPatient != null){
                    model.setCurrentPatient(chosenPatient);
                    model.setState(HealthDiaryViewModel.State.DONE);
                }
            });

        });
        /*

         */


        return dialog;
    }
}
