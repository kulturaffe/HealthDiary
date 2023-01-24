package com.example.healthdiary.ui;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.APICaller;
import com.example.healthdiary.dataHandling.HealthDiaryDataDAO;
import com.example.healthdiary.dataTypes.BodyMassReading;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.HealthDiaryLocation;
import com.example.healthdiary.dataTypes.TemperatureReading;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RecordBodyMassActivity extends AppCompatActivity {
    HealthDiaryDataDAO dbDAO;
    CompletableFuture<TemperatureReading> tempFuture;
    private HealthDiaryPatient currentPatient;
    private HealthDiaryLocation currentLocation;
    private long currentTs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_body_mass);

        currentPatient = getIntent().getParcelableExtra(getString(R.string.current_pat));
        currentLocation = getIntent().getParcelableExtra(getString(R.string.current_loc));

        AtomicReference<String> masterKeyAliasRef = new AtomicReference<>(""); // atomic reference instead of string for easier use in lambdas (activity result handler)
        try {
            masterKeyAliasRef.set(MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC));
        } catch (GeneralSecurityException | IOException e) {
            Log.w(getString(R.string.log_tag),"getting masterKeyAlias in BodyMassActivity failed: ",e);
        }

        // chosen date
        AtomicReference<String> dateFallback = new AtomicReference<>(getString(R.string.now));
        currentTs = setChosenDate(masterKeyAliasRef.get(), dateFallback.get());
        //date launcher
        ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if(null != result.getData()){
                    dateFallback.set(result.getData().getStringExtra("DateTime"));
                }
                currentTs = setChosenDate(masterKeyAliasRef.get(),dateFallback.get());
                makeWeatherApiCall();
            }
        });
        makeWeatherApiCall();

        // get writeable database
        dbDAO = new HealthDiaryDataDAO(this, currentPatient.hexSha256());

        EditText massView = findViewById(R.id.editTextWeight);
        // generate rand values
        Random rand = new Random();
        findViewById(R.id.buttonMRand).setOnClickListener(view -> massView.setText(String.format(Locale.ENGLISH, // to avoid ',' as decimal separator, even if phone is set to german
                "%.2f",30+(rand.nextDouble()*150.0))));
        // save
        findViewById(R.id.buttonMSave).setOnClickListener(view -> {
            double bodyMass = -1;
            try {
                bodyMass = Double.parseDouble(massView.getText().toString());
            }catch (NumberFormatException nfe) {
                Toast.makeText(getApplicationContext(),getString(R.string.toast_invalid_dec_number),Toast.LENGTH_SHORT).show();
                return;
            }
            if (null != tempFuture && !tempFuture.isDone()){
                Toast.makeText(getApplicationContext(),getString(R.string.toast_wait_for_api_call),Toast.LENGTH_SHORT).show();
                return;
            }
            if(bodyMass <= 5 || bodyMass >= 300) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_invalid_m_value), Toast.LENGTH_SHORT).show();
                return;
            }
            TextView chosenDate = findViewById(R.id.textViewMChosenDate);
            long rowidM, rowidT;
            BodyMassReading resultM;

            long patientId = currentPatient.getId();
            if(chosenDate.getText().toString().equals(getString(R.string.now))){
                resultM = new BodyMassReading(bodyMass, patientId);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    resultM = new BodyMassReading(bodyMass, patientId, ZonedDateTime.parse(
                            chosenDate.getText().toString(), DateTimeFormatter.ISO_DATE_TIME).toInstant().toEpochMilli());
                else
                    //noinspection deprecation  --to make the IDE stop moaning about Date.parse() deprecation
                    resultM = new BodyMassReading(bodyMass, patientId, Date.parse(chosenDate.getText().toString()));
            }

            if(null == tempFuture || tempFuture.isCompletedExceptionally()) {
                rowidT = Long.MIN_VALUE;
                Toast.makeText(this,getString(R.string.toast_unsuccessful_api_call),Toast.LENGTH_SHORT).show();
            } else
                rowidT = dbDAO.addTemperatureReading(tempFuture.join());

            rowidM = dbDAO.addBodyMassReading(resultM);
            if(rowidM > 0){
                resultM.setId(rowidM);
            }
            if(rowidM > 0 && rowidT > 0){
                setResult(RESULT_OK,new Intent().putExtra(getString(R.string.avg_m),dbDAO.getAverageBodyMass()).
                        putExtra(getString(R.string.latest_m),resultM).
                        putExtra(getString(R.string.latest_t),tempFuture.join().setId(rowidT)));
                finish();
            } else if (rowidM > 0) {
                Log.w(getString(R.string.log_tag),"Temperature could not be saved into database");
                setResult(RESULT_OK,new Intent().putExtra(getString(R.string.avg_m),dbDAO.getAverageBodyMass()).
                        putExtra(getString(R.string.latest_m),resultM));
                finish();
            } else
                Toast.makeText(getApplicationContext(),getString(R.string.toast_db_write_error),Toast.LENGTH_SHORT).show();
        });
        // change date
        findViewById(R.id.buttonMChangeDate).setOnClickListener(view -> mStartForResult.launch(new Intent(this, DateTimePickerActivity.class) /*.putExtra(getString(R.string.master_key_alias),masterKeyAlias)*/ ));
        // show all
        findViewById(R.id.buttonMShowAll).setOnClickListener(view -> startActivity(new Intent(this,DisplayListOfReadingsActivity.class)
                .putParcelableArrayListExtra(getString(R.string.readings_for_list_tag),
                        (ArrayList<? extends Parcelable>) dbDAO.getBodyMassReadings())
        ));
    }

    /**
     * @return currently chosen timestamp in ms
     */
    protected long setChosenDate(String masterKeyAlias, String fallback){
        Log.d("MyTag",String.format("chosen date fallback: %s",fallback));
        String currentDate = getString(R.string.now);
        long ts = -1L;
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
                ts = new Date().getTime();
                Log.d(getString(R.string.log_tag),String.format("Accessing encrypted shared preference in BloodPressureActivity failed, masterKeyAlias = '%s'\n",masterKeyAlias));
            }
            if(getString(R.string.default_value1).equals(currentDate)) {
                currentDate = fallback;
                ts = new Date().getTime();
            }
        } catch (GeneralSecurityException | IOException e){
            currentDate = fallback;
            ts = new Date().getTime();
            Log.w(getString(R.string.log_tag),String.format("Error accessing encrypted shared preference in BloodPressureActivity, masterKeyAlias = '%s'\n",masterKeyAlias), e);
        }
        TextView chosenDate = findViewById(R.id.textViewMChosenDate);
        try{
            Date date = new Date(Long.parseLong(currentDate));
            chosenDate.setText(String.format(Locale.ENGLISH,"%tFT%tT.%tLZ",date,date,date));
            ts = date.getTime();
        } catch (NumberFormatException e){
            chosenDate.setText(currentDate);
        }
        return ts;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void makeWeatherApiCall(){
        if(isNetworkAvailable())
            tempFuture = new APICaller(getApplicationContext()).getHistoricalWeatherForLocation(currentLocation,currentTs);
        else
            Toast.makeText(this, getString(R.string.toast_no_internet), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        dbDAO.close();
        super.onDestroy();
    }
}