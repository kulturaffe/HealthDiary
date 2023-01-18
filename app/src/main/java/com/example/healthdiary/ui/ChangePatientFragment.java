package com.example.healthdiary.ui;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.HealthDiaryViewModel;


public class ChangePatientFragment extends DialogFragment {
    HealthDiaryViewModel model;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);
        setCancelable(model.getCancellable());

        builder .setTitle(R.string.current_user)
                .setPositiveButton(R.string.create_new_short, (dialog, id) -> model.setState(HealthDiaryViewModel.State.CREATE_NEW))
                .setNeutralButton(R.string.action_sign_in_short, (dialog, which) -> model.setState(HealthDiaryViewModel.State.LOGIN));
        if(isCancelable()) {
            builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> model.setState(HealthDiaryViewModel.State.CANCELLED));
        }
        // Create the AlertDialog object and return it
        return builder.create();
    }

}