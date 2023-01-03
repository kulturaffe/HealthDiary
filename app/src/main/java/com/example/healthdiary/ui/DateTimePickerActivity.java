package com.example.healthdiary.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.healthdiary.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DateTimePickerActivity extends AppCompatActivity {
    private final AtomicReference<String> masterKeyAliasRef = new AtomicReference<>("");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_picker);
        Locale locale = getResources().getConfiguration().locale;
        String previousDate = "";
        try {
            masterKeyAliasRef.set(MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC));
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.pref_file_key),
                    masterKeyAliasRef.get(),
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            previousDate = sharedPreferences.getString(getString(R.string.key),"");
        } catch (GeneralSecurityException | IOException e){
            Log.w(getString(R.string.log_tag),String.format("Error accessing encrypted shared preference in DateTimePicker, masterKeyAlias = '%s'\n",masterKeyAliasRef.get()), e);
        }
// the passed date-string
        Calendar cal =  Calendar.getInstance(TimeZone.getDefault(),locale); // now, extra parameters in constructor are probably not really necessary but don't hurt as well

        // DatePicker
        DatePicker datePicker = findViewById(R.id.simpleDatePicker);
        datePicker.setMaxDate(cal.getTimeInMillis());
        Pattern datePattern = Pattern.compile("^([0-9]{4})-(12|11|10|0[1-9])-(31|30|[012][0-9]).*"); // is passed string a useful date f
        Matcher matcher = datePattern.matcher(previousDate);
        matcher.matches(); // necessary to fill the groups
        try { // instead of an if-statement, if there is a NullPointerException the passed string could not be recognized as a useful date (there are not enough groups)
                datePicker.updateDate(Integer.parseInt(matcher.group(1)),Integer.parseInt(matcher.group(2))-1,Integer.parseInt(matcher.group(3)));
            } catch (Exception e){
                // Log.d("MyTag","   Date couldn't be parsed:\""+previousDate+"\"\n",e);
                // set calendar to now
                datePicker.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE));
        }

        // TimePicker
        TimePicker timePicker = findViewById(R.id.simpleTimePicker);
        timePicker.setIs24HourView(true);
        // set to now
        timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(cal.get(Calendar.MINUTE));



        // SAVE-button
        Button buttonSaveDT = findViewById(R.id.buttonSaveDateTime);
        buttonSaveDT.setOnClickListener(view ->{
            cal.set(datePicker.getYear(), (datePicker.getMonth()+1), datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());

            String resultString, timeZoneString;
            // get the right timezone offset iso8601-formatted
            int offsetMinTotal = (int) (TimeZone.getDefault().getOffset(cal.getTimeInMillis()) / 60_000L);
            int offsetMinAbs = Math.abs(offsetMinTotal)%60;
            int offsetHoursAbs = (Math.abs(offsetMinTotal)/60)%14;
            if(offsetMinTotal>=0) timeZoneString = String.format(locale,"+%02d:%02d",offsetHoursAbs,offsetMinAbs);
            else timeZoneString = String.format(locale,"-%02d:%02d",offsetHoursAbs,offsetMinAbs);

            // get current seconds and millis to give a more unified output
            int secNowInMillis = (int) (Calendar.getInstance().getTimeInMillis() % 60_000L);
            int millisNow = secNowInMillis % 1000;
            int secondsNow = (secNowInMillis / 1000);

            // put together a nicely iso8601-formatted string with selected date save
            resultString = String.format(locale,"%04d-%02d-%02dT%02d:%02d:%02d.%03d%s",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), secondsNow, millisNow, timeZoneString);

            // legacy but also fallback: send result back
            setResult(RESULT_OK,new Intent().putExtra("DateTime",resultString));
            //save to sharedpref:
            saveToESP(resultString);
            finish();
        });

        // now button
        findViewById(R.id.buttonNow).setOnClickListener(view -> {
            setResult(RESULT_OK,new Intent().putExtra("DateTime",getString(R.string.now)));
            //save to sharedpref:
            saveToESP(getString(R.string.now));
            finish();
        });
    }

    private void saveToESP(String msg){
        try {
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.pref_file_key),
                    masterKeyAliasRef.get(),
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            sharedPreferences.edit().putString(getString(R.string.key),msg).apply();
            Log.d(getString(R.string.log_tag),String.format("Wrote to encrypted shared preference in DateTimePicker: %s'\n",msg));
            setResult(RESULT_OK);
        } catch (GeneralSecurityException | IOException e){
            Log.w(getString(R.string.log_tag),String.format("Error writing to encrypted shared preference in DateTimePicker, masterKeyAlias = '%s'\n", masterKeyAliasRef.get()), e);
            setResult(RESULT_CANCELED);
        }
    }

    @Override // so upward navigation goes to calling activity
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed(); // returns result code 0 (RESULT_CANCELLED)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}