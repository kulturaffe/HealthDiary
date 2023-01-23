package com.example.healthdiary.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.allyants.notifyme.NotifyMe;
import com.example.healthdiary.dataHandling.HealthDiaryViewModel;

import java.util.Calendar;
import java.util.Objects;

public class AddMedicationNotificationFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    HealthDiaryViewModel model;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        model = new ViewModelProvider(requireActivity()).get(HealthDiaryViewModel.class);
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true/*always 24-hr-format, otherwise: DateFormat.is24HourFormat(getActivity())*/);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String name = model.getMedicationName().getValue();
        // build the notification
        NotifyMe.Builder notifyMe = new NotifyMe.Builder(requireActivity().getApplicationContext());
        // TODO i think this will not work with androidx :/
        model.setMedicationTime(hourOfDay, minute);
    }
}
