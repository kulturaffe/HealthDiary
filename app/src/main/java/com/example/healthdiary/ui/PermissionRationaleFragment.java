package com.example.healthdiary.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;


public class PermissionRationaleFragment extends DialogFragment {
    HealthDiaryViewModel model;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);
        setCancelable(model.getCancellable());
        HealthDiaryPatient currentPatient = model.getCurrentPatient().getValue();

        builder.setTitle(R.string.permission_location_why)
                .setMessage(R.string.permission_location_explanation)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> model.setState(HealthDiaryViewModel.State.RATIONALE_SEEN));
        return builder.create();
    }
}