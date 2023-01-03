package com.example.healthdiary.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;


public class LoginPatientFragment extends DialogFragment {
    HealthDiaryViewModel model;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);
        setCancelable(model.getCancellable());
        HealthDiaryPatient currentPatient = model.getCurrentPatient().getValue();

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> model.setSelectionState(HealthDiaryViewModel.PatientSelectionState.DONE));
        return builder.create();
    }
}