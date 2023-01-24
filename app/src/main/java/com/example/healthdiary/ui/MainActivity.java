package com.example.healthdiary.ui;

import static com.example.healthdiary.dataTypes.HealthDiaryLocation.Status.INVALID;
import static com.example.healthdiary.dataTypes.HealthDiaryLocation.Status.MISSING_COORD;
import static com.example.healthdiary.dataTypes.HealthDiaryLocation.Status.MISSING_NAME;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.APICaller;
import com.example.healthdiary.dataHandling.HealthDiaryDataDAO;
import com.example.healthdiary.dataHandling.HealthDiaryUsersDAO;
import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.BodyMassReading;
import com.example.healthdiary.dataTypes.BloodPressureReading;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.HealthDiaryLocation;
import com.example.healthdiary.dataTypes.TemperatureReading;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.zetetic.database.sqlcipher.SQLiteDatabaseCorruptException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private TextView avgBpView, avgMView, lastBPView, lastMView, lastTempView, lastPromptView, currentPatientView, currentLocationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private HealthDiaryViewModel model;
    private SharedPreferences sharedPreferences;

    private final ActivityResultLauncher<Intent> mStartForAverage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
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
                    else lastBPView.setVisibility(View.INVISIBLE);
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
                HealthDiaryLocation newLocation = result.getData().getParcelableExtra(getString(R.string.current_loc));
                if (null != newLocation){
                    model.setLocation(newLocation);
                    Log.d(getString(R.string.log_tag),String.format("Got location: %s",newLocation));
                }
                // all pressures
                List<BloodPressureReading> allBPs = result.getData().getParcelableArrayListExtra("all_pressures");
                if(null != allBPs){
                    model.setAllBpReadings(allBPs);
                }
                // all masses
                List<BodyMassReading> allMs = result.getData().getParcelableArrayListExtra("all_masses");
                if(null != allMs){
                    model.setAllMReadings(allMs);
                }


            } else if ("".equals(avgBpView.getText().toString()) && "".equals(avgMView.getText().toString()))
                avgBpView.setText(getString(R.string.db_read_error));
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPatientView = findViewById(R.id.textViewCurrentUser);
        currentLocationView = findViewById(R.id.textViewCurrentLocation);
        avgBpView = findViewById(R.id.textViewAvgBp);
        avgMView = findViewById(R.id.textViewAvgM);
        lastBPView = findViewById(R.id.textViewLastBp);
        lastBPView.setVisibility(View.INVISIBLE);
        lastMView = findViewById(R.id.textViewLastM);
        lastTempView = findViewById(R.id.textViewLastTemp);
        lastPromptView = findViewById(R.id.textViewLastPrompt);
        lastPromptView.setVisibility(View.INVISIBLE);

        // Get the ViewModel
        model = new ViewModelProvider(this).get(HealthDiaryViewModel.class);
        model.setCancellable(true);

        // observe current patient
        model.getCurrentPatient().observe(this, patient -> {
            if (patient != null && patient.getId() > -1){
                String txt = getString(R.string.current_user)+ patient.toValueOnlyString();
                currentPatientView.setText(txt);
            }
        });

        // observe current location
        model.getLocation().observe(this, location ->{
            if (location != null){
                String txt = getString(R.string.current_location)+ location.toValueOnlyString();
                currentLocationView.setText(txt);
                if(isNetworkAvailable()){
                    APICaller caller = new APICaller(getApplicationContext());
                    if(location.getStatus() == MISSING_COORD){
                        caller.getDirectGeo(location.getName()).whenComplete((result,exception)->{
                            if (exception != null) {
                                Log.e("MyTag","Exception during direct geo");
                            } else {
                                model.postLocation(result);
                            }
                        });
                    }
                    if(location.getStatus() == MISSING_NAME){
                        caller.getReverseGeo(location.getLat(),location.getLon()).whenComplete((result,exception)->{
                            if (exception != null) {
                                Log.e("MyTag","Exception during direct geo");
                            } else {
                                model.postLocation(result);
                                Log.d("MyTag",String.format("WHEN COMPLETE, result: %s",result));
                            }
                        });
                    }
                } else Toast.makeText(this, getString(R.string.toast_no_internet)+"No weather info can be fetched..", Toast.LENGTH_LONG).show();
            }
        });


        // observe medication name for setting of time and notification
        model.getMedicationName().observe(this, medication ->{
            if (medication != null && !medication.isEmpty()){
                showAddNotificationFragment();
            }
        });
        // observe medication time for usr-notification notification
        model.getMedicationTime().observe(this, time ->{
            if (time != null && time.length == 2){
                Toast.makeText(this, String.format(Locale.ENGLISH,"You will be notified to take %s at %02d:%02d for a month", model.getMedicationName().getValue(),time[0],time[1]),Toast.LENGTH_LONG).show();
                model.setMedicationName("").clearMedicationTime();
            }
        });

        // observe fhir-string to show
        model.getFhirResource().observe(this, res ->{
            if (res != null && !res.isEmpty()){
                showShowBundleFragment();
            }
        });

        // observe status for change of patient-logic
        model.getState().observe(this, state ->{
            switch (state){
                case LOGIN:
                    showListPatientsFragment();
                    break;
                case CREATE_NEW:
                    showCreatePatientFragment();
                    break;
                case NOT_AVAILABLE:
                    Toast.makeText(this, "No saved users, please create a new one", Toast.LENGTH_SHORT).show();
                    model.setState(HealthDiaryViewModel.State.CREATE_NEW);
                    break;
                case ADD:
                    addNewUser();
                    model.setState(HealthDiaryViewModel.State.DONE);
                    break;
                case DONE:
                    if (!loadCurrentUser()) Toast.makeText(this,"could not switch to user\u2026",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
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
                    HealthDiaryLocation loc = model.getLocation().getValue();
                    mStartForAverage.launch(new Intent(getApplicationContext(), RecordBloodPressureActivity.class)
                            .putExtra(getString(R.string.current_pat), model.getCurrentPatient().getValue())
                            .putExtra(getString(R.string.current_loc), Objects.requireNonNull(loc).getStatus() != INVALID ? loc : null));
                }
                else if(id == R.id.body_mass_record) {
                    HealthDiaryLocation loc = model.getLocation().getValue();
                    mStartForAverage.launch(new Intent(getApplicationContext(), RecordBodyMassActivity.class)
                            .putExtra(getString(R.string.current_pat), model.getCurrentPatient().getValue())
                            .putExtra(getString(R.string.current_loc), Objects.requireNonNull(loc).getStatus() != INVALID ? loc : null));
                }
                else if(id == R.id.change_patient){
                    showFirstFragment();
                }
                else if (id == R.id.change_location){
                    showEnterLocationNameFragment();
                }
                else if(id == R.id.add_medication_reminder){
                    showAddMedicationNameFragment();
                }
                else if(id == R.id.export_fhir){
                    model.createFhirResource();
                }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // shared pref.
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

        // continue to login
        mStartForAverage.launch(new Intent(this, LoginActivity.class));
    }


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

    private void showFirstFragment(){
        DialogFragment frag = new ChangePatientFragment();
        frag.show(getSupportFragmentManager(),"proceedHow");
    }

    private void showCreatePatientFragment(){
        DialogFragment frag = new CreatePatientFragment();
        frag.show(getSupportFragmentManager(),"addNew");
    }

    private void showAddMedicationNameFragment(){
        DialogFragment frag = new AddMedicationNameFragment();
        frag.show(getSupportFragmentManager(),"medicationName");
    }

    private void showAddNotificationFragment(){
        DialogFragment frag = new AddMedicationNotificationFragment();
        frag.show(getSupportFragmentManager(),"medicationTime");
    }

    private void showShowBundleFragment(){
        DialogFragment frag = new ShowBundleFragment();
        frag.show(getSupportFragmentManager(),"resource");
    }

    private void showEnterLocationNameFragment(){
        DialogFragment frag = new EnterLocationNameFragment();
        frag.show(getSupportFragmentManager(),"locationName");
    }


    private void showListPatientsFragment(){
        if(null == model.getAllPatients().getValue() || model.getAllPatients().getValue().size() < 1){
            try (HealthDiaryUsersDAO dao = new HealthDiaryUsersDAO(this, sharedPreferences.getString(getString(R.string.key2), ""))) {
                model.setAllPatients(dao.getPatients());
            } catch (SQLiteDatabaseCorruptException wrong_pw_for_existing_db) {
                Toast.makeText(this,"Error with database",Toast.LENGTH_SHORT).show();
                model.setState(HealthDiaryViewModel.State.CANCELLED);
                return;
            }
        }
        DialogFragment frag = new ListAllPatientsFragment();
        frag.show(getSupportFragmentManager(),"listAll");
    }

    void addNewUser(){
        HealthDiaryPatient patient = model.getNewPatient().getValue();
        if(patient == null || patient.getId() >= 0) return;
        try (HealthDiaryUsersDAO dao = new HealthDiaryUsersDAO(this, sharedPreferences.getString(getString(R.string.key2), ""))) {
            patient.setId(dao.addPatient(patient));
            model.setCurrentPatient(patient);
        } catch (SQLiteDatabaseCorruptException wrong_pw_for_existing_db) {
            Log.e(getString(R.string.log_tag),"Something went wrong with db");
        }
    }

    boolean loadCurrentUser(){
        HealthDiaryPatient patient = model.getCurrentPatient().getValue();

        if(patient == null || patient.getId() < 0){
            Toast.makeText(this,"could not confirm user",Toast.LENGTH_SHORT).show();
            return false;
        }
        try (HealthDiaryDataDAO dao = new HealthDiaryDataDAO(this, patient.hexSha256())) {
            BloodPressureReading avgBp = dao.getAverageBloodPressure();
            BodyMassReading avgM = dao.getAverageBodyMass();
            BloodPressureReading latestBP = dao.getLatestBloodPressure();
            BodyMassReading latestM = dao.getLatestBodyMass();
            TemperatureReading lastT = dao.getLatestTemperature();

            model.setAllBpReadings(dao.getBloodPressureReadings());
            model.setAllMReadings(dao.getBodyMassReadings());

            if (null != avgBp.toValueOnlyString())
                avgBpView.setText(avgBp.toValueOnlyString());
            else
                avgBpView.setText(getText(R.string.no_avg));

            if (null != avgM.toValueOnlyString())
                avgMView.setText(avgM.toValueOnlyString());
            else
                avgMView.setText(getText(R.string.no_avg));

            if (null != latestBP.toValueOnlyString()) {
                lastPromptView.setVisibility(View.VISIBLE);
                lastBPView.setVisibility(View.VISIBLE);
                lastBPView.setText(latestBP.toString());
            }
            else
                lastBPView.setText("");

            if (null != latestM.toValueOnlyString()) {
                lastPromptView.setVisibility(View.VISIBLE);
                lastMView.setText(latestM.toString());
            }
            else lastMView.setText("");
            if (null != lastT.toValueOnlyString()) {
                lastPromptView.setVisibility(View.VISIBLE);
                lastTempView.setText(lastT.toValueOnlyString());
            }
            else lastTempView.setText("");
            if(null == lastT.toValueOnlyString() && null == latestBP.toValueOnlyString() && null == latestM.toValueOnlyString())
                lastPromptView.setVisibility(View.INVISIBLE);

        } catch (SQLiteDatabaseCorruptException wrong_pw_for_existing_db) {
            Log.d(getString(R.string.log_tag),"Could not get average for current user\u2026 (MainActivity)");
            return false;
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}