package com.example.healthdiary.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.APICaller;
import com.example.healthdiary.dataHandling.HealthDiaryDataDAO;
import com.example.healthdiary.dataTypes.BloodPressureReading;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.Location;
import com.example.healthdiary.dataTypes.TemperatureReading;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RecordBloodPressureActivity extends AppCompatActivity {
    private HealthDiaryDataDAO dbDAO;
    private CompletableFuture<TemperatureReading> tempFuture;
    private HealthDiaryPatient currentPatient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_blood_pressure);

        currentPatient = getIntent().getParcelableExtra(getString(R.string.current_pat));
        Log.d(getString(R.string.log_tag),"Got Patient in BloodPressureActivity: " + currentPatient);
        currentLocation = getIntent().getParcelableExtra(getString(R.string.current_loc));
        Log.d(getString(R.string.log_tag),"Got Location in BloodPressureActivity: " + currentLocation);

        // start API-call for weather
        if(isNetworkAvailable())
            tempFuture = new APICaller(getApplicationContext()).getCurrentWeatherFromStr("Vienna");
        else
            Toast.makeText(this, getString(R.string.toast_no_internet), Toast.LENGTH_SHORT).show();

        // get writable db
        dbDAO = new HealthDiaryDataDAO(this, currentPatient.hexSha256());

        AtomicReference<String> masterKeyAliasRef = new AtomicReference<>(""); // to make string accessible inside lambda
        try {
            masterKeyAliasRef.set(MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC));
        } catch (GeneralSecurityException | IOException e) {
            Log.w(getString(R.string.log_tag),"getting masterKeyAlias in BloodPressureActivity failed: ",e);
        }

        // chosen date
        AtomicReference<String> dateFallback = new AtomicReference<>(getString(R.string.now));
        setChosenDate(masterKeyAliasRef.get(),dateFallback.get());
        ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if(null != result.getData()){
                    dateFallback.set(result.getData().getStringExtra("DateTime"));
                }
                setChosenDate(masterKeyAliasRef.get(),dateFallback.get());
            }
        });

        // button generate rand values
        EditText sysView = findViewById(R.id.editTextSys);
        EditText diaView = findViewById(R.id.editTextDia);
        Random rand = new Random();
        Button buttonRand = findViewById(R.id.buttonBPRand);
        buttonRand.setOnClickListener(view -> {
            sysView.setText(String.valueOf(rand.nextInt(81)+100));
            diaView.setText(String.valueOf(rand.nextInt(46)+60));
        });

        // button save
        Button buttonSave = findViewById(R.id.buttonBPSave);
        buttonSave.setOnClickListener(view -> {
            int sys = -1, dia = -1;
            // get and validate input:
            try {
                sys = Integer.parseInt(sysView.getText().toString());
                dia = Integer.parseInt(diaView.getText().toString());
            }catch (NumberFormatException nfe){
                    Toast.makeText(getApplicationContext(),getString(R.string.toast_invalid_number ),Toast.LENGTH_SHORT).show();
                    return;
            }
            if (null != tempFuture && !tempFuture.isDone()){
                Toast.makeText(getApplicationContext(),getString(R.string.toast_wait_for_api_call),Toast.LENGTH_SHORT).show();
                return;
            }
            if(sys > 300 || sys < 50 || dia > 250 || dia < 20){
                Toast.makeText(getApplicationContext(),getString(R.string.toast_invalid_bp_values),Toast.LENGTH_SHORT).show();
                return;
            }
            if(sys <= dia) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_dia_bigger_than_sys), Toast.LENGTH_SHORT).show();
                return;
            }
            TextView chosenDate = findViewById(R.id.textViewBpChosenDate);
            long rowidBp, rowidT;
            BloodPressureReading resultBp;
            long patientId = currentPatient.getId();
            if(chosenDate.getText().toString().equals(getString(R.string.now))){
                resultBp =new BloodPressureReading(sys,dia, patientId);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    resultBp = new BloodPressureReading(sys, dia, patientId,
                            ZonedDateTime.parse(chosenDate.getText().toString(), DateTimeFormatter.ISO_DATE_TIME).toInstant().toEpochMilli());
                } else {
                    //noinspection deprecation -- to make the IDE stop moaning about Date.parse() deprecation
                    resultBp = new BloodPressureReading(sys,dia, patientId,
                            Date.parse(chosenDate.getText().toString())
                    );
                }
            }
            rowidBp = dbDAO.addBloodPressureReading(resultBp);
            if(null == tempFuture || tempFuture.isCompletedExceptionally()){
                rowidT = Long.MIN_VALUE;
                Toast.makeText(this,getString(R.string.toast_unsuccessful_api_call),Toast.LENGTH_SHORT).show();
            } else
                rowidT = dbDAO.addTemperatureReading(tempFuture.join());

            if(rowidBp > 0){
                resultBp.setId(rowidBp);
            }

            if(rowidBp > 0 && rowidT > 0){
                setResult(RESULT_OK,new Intent().putExtra(getString(R.string.avg_bp),dbDAO.getAverageBloodPressure())
                        .putExtra(getString(R.string.latest_bp), resultBp)
                        .putExtra(getString(R.string.latest_t), tempFuture.join().setId(rowidT)));
                finish();
            } else if (rowidBp > 0) {
                Log.w(getString(R.string.log_tag),"Temperature could not be saved into database");
                setResult(RESULT_OK,new Intent().putExtra(getString(R.string.avg_bp),dbDAO.getAverageBloodPressure())
                        .putExtra(getString(R.string.latest_bp), resultBp));
                finish();
            } else
                Toast.makeText(getApplicationContext(),getString(R.string.toast_db_write_error),Toast.LENGTH_SHORT).show();
        });

        // change date
        findViewById(R.id.buttonBpChangeDate).setOnClickListener(view -> mStartForResult.launch(new Intent(this, DateTimePickerActivity.class)));

        // list all
        findViewById(R.id.buttonBpShowAll).setOnClickListener(view -> startActivity(new Intent(this, DisplayListOfReadingsActivity.class)
                 .putParcelableArrayListExtra(getString(R.string.readings_for_list_tag),
                        (ArrayList<? extends Parcelable>) dbDAO.getBloodPressureReadings())
        ));

    }

    protected void setChosenDate(String masterKeyAlias, String fallback){
        String currentDate = getString(R.string.now);
        try {
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.pref_file_key),
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            currentDate = sharedPreferences.getString(getString(R.string.key1),currentDate);
            if(getString(R.string.now).equals(currentDate)) {
                currentDate = fallback;
                Log.d(getString(R.string.log_tag),String.format("Accessing encrypted shared preference in BloodPressureActivity failed, masterKeyAlias = '%s'\n",masterKeyAlias));
            }
            if(getString(R.string.default_value1).equals(currentDate)) currentDate = fallback;
        } catch (GeneralSecurityException | IOException e){
            currentDate = fallback;
            Log.w(getString(R.string.log_tag),String.format("Error accessing encrypted shared preference in BloodPressureActivity, masterKeyAlias = '%s'\n",masterKeyAlias), e);
        }
        TextView chosenDate = findViewById(R.id.textViewBpChosenDate);
        chosenDate.setText(currentDate);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onDestroy() {
        dbDAO.close();
        super.onDestroy();
    }

}