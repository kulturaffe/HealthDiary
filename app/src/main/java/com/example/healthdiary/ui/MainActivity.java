package com.example.healthdiary.ui;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.BodyMassReading;
import com.example.healthdiary.dataTypes.BloodPressureReading;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.Location;
import com.example.healthdiary.dataTypes.TemperatureReading;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private TextView avgBpView, avgMView, lastBPView, lastMView, lastTempView, lastPromptView, currentPatientView, currentLocationView;
    private ActivityResultLauncher<Intent> mStartForAverage;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private HealthDiaryViewModel model;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Get the ViewModel and observe
        model = new ViewModelProvider(this).get(HealthDiaryViewModel.class);
        model.setCancellable(true);

        currentPatientView = findViewById(R.id.textViewCurrentUser);
        model.getCurrentPatient().observe(this, patient -> {
            if (patient != null){
                String txt = getString(R.string.current_user)+ patient.toValueOnlyString();
                currentPatientView.setText(txt);
            }
        });

        currentLocationView = findViewById(R.id.textViewCurrentLocation);
        model.getLocation().observe(this, location ->{
            if (location != null){
                String txt = getString(R.string.current_location)+ location.toValueOnlyString();
                currentLocationView.setText(txt);
            }
        });

        // nav-bar:
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        // pass the Open and Close toggle for the drawer layout listener to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // since mainActivity uses the default theme, getSupportActionBar() should never return null
        NavigationView navView = findViewById(R.id.navigationView);
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
                if (id == R.id.blood_pressure_record)  {
                    mStartForAverage.launch(new Intent(getApplicationContext(), RecordBloodPressureActivity.class)
                            .putExtra(getString(R.string.current_pat), model.getCurrentPatient().getValue())
                            .putExtra(getString(R.string.current_loc), model.getLocation().getValue()));
                }
                else if(id == R.id.body_mass_record) {
                    mStartForAverage.launch(new Intent(getApplicationContext(), RecordBodyMassActivity.class)
                            .putExtra(getString(R.string.current_pat), model.getCurrentPatient().getValue())
                            .putExtra(getString(R.string.current_loc), model.getLocation().getValue()));
                }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        avgBpView = findViewById(R.id.textViewAvgBp);
        avgMView = findViewById(R.id.textViewAvgM);
        lastBPView = findViewById(R.id.textViewLastBp);
        lastBPView.setVisibility(View.INVISIBLE);
        lastMView = findViewById(R.id.textViewLastM);
        lastTempView = findViewById(R.id.textViewLastTemp);
        lastPromptView = findViewById(R.id.textViewLastPrompt);
        lastPromptView.setVisibility(View.INVISIBLE);


        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.pref_file_key),
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            // set default date (key1), default (empty) user (key3)
            sharedPreferences.edit().putString(getString(R.string.key1),getString(R.string.default_value1))
                    .putString(getString(R.string.key3),getString(R.string.default_value3)).apply();
            Log.d(getString(R.string.log_tag),String.format("Created/opened encrypted shared preferences in MainActivity, masterKeyAlias= '%s'\n",masterKeyAlias));
        } catch (GeneralSecurityException | IOException e){
            Log.w(getString(R.string.log_tag),"Error creating/opening encrypted shared preference in MainActivity ", e);
        }

        mStartForAverage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(null!=result.getData()){
                if (result.getResultCode() == RESULT_OK) {
                    // avg bp
                    if(null != result.getData().getParcelableExtra(getString(R.string.avg_bp))){
                        BloodPressureReading avgBp = result.getData().getParcelableExtra(getString(R.string.avg_bp));
                        if (null != avgBp.toValueOnlyString())
                            avgBpView.setText(avgBp.toValueOnlyString());
                        else
                            avgBpView.setText(getText(R.string.no_avg));
                    }
                    // avg m
                    if(null != result.getData().getParcelableExtra(getString(R.string.avg_m))){
                        BodyMassReading avgM = result.getData().getParcelableExtra(getString(R.string.avg_m));
                        if (null != avgM.toValueOnlyString())
                            avgMView.setText(avgM.toValueOnlyString());
                        else
                            avgMView.setText(getText(R.string.no_avg));
                    }
                    // latest bp
                    if(null != result.getData().getParcelableExtra(getString(R.string.latest_bp))){
                        BloodPressureReading latestBP = result.getData().getParcelableExtra(getString(R.string.latest_bp));
                        if (null != latestBP.toValueOnlyString()) {
                            lastPromptView.setVisibility(View.VISIBLE);
                            lastBPView.setText(latestBP.toString());
                            lastBPView.setVisibility(View.VISIBLE);
                        }
                        else lastBPView.setVisibility(View.INVISIBLE);;
                    }
                    // latest m
                    if(null != result.getData().getParcelableExtra(getString(R.string.latest_m))){
                        BodyMassReading latestM = result.getData().getParcelableExtra(getString(R.string.latest_m));
                        Log.d(getString(R.string.log_tag),"latest mass: " + latestM);
                        if (null != latestM.toValueOnlyString()) {
                            lastPromptView.setVisibility(View.VISIBLE);
                            lastMView.setText(latestM.toString());
                        }
                        else lastMView.setText("");
                    }
                    // latest temp
                    if(null != result.getData().getParcelableExtra(getString(R.string.latest_t))){
                        TemperatureReading lastT = result.getData().getParcelableExtra(getString(R.string.latest_t));
                        if (null != lastT.toValueOnlyString()) {
                            lastPromptView.setVisibility(View.VISIBLE);
                            lastTempView.setText(lastT.toValueOnlyString());
                        }
                        else lastTempView.setText("");
                    }

                    // current patient
                    HealthDiaryPatient newCurrentPat = result.getData().getParcelableExtra(getString(R.string.current_pat));
                    if(null != newCurrentPat){
                        model.setCurrentPatient(newCurrentPat);
                    }

                    // current location
                    Location newLocation = result.getData().getParcelableExtra(getString(R.string.current_loc));
                    if (null != newLocation){
                        model.setLocation(newLocation);
                    }


                } else if ("".equals(avgBpView.getText().toString()) && "".equals(avgMView.getText().toString()))
                    avgBpView.setText(getString(R.string.db_read_error));
            }
        });


        // ask for db-pw
        mStartForAverage.launch(new Intent(this, LoginActivity.class));
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        Log.d(getString(R.string.log_tag),"onResume entered");
        super.onResume();
    }
}