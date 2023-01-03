package com.example.healthdiary.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;


public class CreatePatientFragment extends DialogFragment {
    HealthDiaryViewModel model;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);
        setCancelable(model.getCancellable());

        builder.setTitle("create test patient")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
            model.setCurrentPatient(new HealthDiaryPatient());
            model.setSelectionState(HealthDiaryViewModel.PatientSelectionState.ADD);
        });
        return builder.create();
    }
}