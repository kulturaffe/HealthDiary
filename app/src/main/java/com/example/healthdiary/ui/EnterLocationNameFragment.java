package com.example.healthdiary.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.HealthDiaryLocation;

public class EnterLocationNameFragment extends DialogFragment {
    HealthDiaryViewModel model;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);

        final EditText input = new EditText(requireActivity().getApplicationContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);

        builder.setTitle("enter name of the new location:")
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> model.setLocation(new HealthDiaryLocation(input.getText().toString())))
                .setNegativeButton(android.R.string.cancel, (d,w) -> d.cancel());
        return builder.create();
    }
}
