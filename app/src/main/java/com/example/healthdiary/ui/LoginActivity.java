package com.example.healthdiary.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.HealthDiaryDataDAO;
import com.example.healthdiary.dataHandling.HealthDiaryUsersDAO;
import com.example.healthdiary.dataHandling.HealthDiaryViewModel;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.HealthDiaryLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import net.zetetic.database.sqlcipher.SQLiteDatabaseCorruptException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPreferences = null;
    private EditText etPw;
    private HealthDiaryViewModel model;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> mRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    model.setState(HealthDiaryViewModel.State.PERMISSION_GRANTED);
                } else {
                    model.setLocation(new HealthDiaryLocation());
                    Toast.makeText(this, "Not granted, "+getString(R.string.default_location), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPw = findViewById(R.id.editTextPassword);
        // Get the ViewModel
        model = new ViewModelProvider(this).get(HealthDiaryViewModel.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // check if location permission is already granted, otherwise obtain it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            model.setState(HealthDiaryViewModel.State.PERMISSION_GRANTED);
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            showPermissionRationaleFragment();
        } else {
            model.setState(HealthDiaryViewModel.State.RATIONALE_SEEN);
        }


        // observe selection state for "business logic":
        model.getState().observe(this, selection -> {
            switch (selection) {
                case LOGIN:
                    showListPatientsFragment();
                    break;
                case CREATE_NEW:
                    showCreatePatientFragment();
                    break;
                case CANCELLED: // should not be allowed to happen, but to be safe just show again
                    showFirstFragment();
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
                    if (confirmCurrentUser()) finish();
                    break;
                case RATIONALE_SEEN:
                    mRequestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
                    break;
                case PERMISSION_GRANTED: // try last location
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    // if network is available
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                            if (location != null) {
                                model.setLocation(new HealthDiaryLocation(/*location.getLatitude(),location.getLongitude() lol*/));
                                Log.d(getString(R.string.log_tag),String.format(Locale.ENGLISH,"Got last Location: lat %.2f, lon %.2f",location.getLatitude(),location.getLongitude()));
                            }
                            else // if no last, get current
                                model.setState(HealthDiaryViewModel.State.NO_LAST_LOCATION);
                        });
                    }
                    // without network, set to default and inform
                    else {
                        model.setLocation(new HealthDiaryLocation());
                        Toast.makeText(this,"Location services seem to be turned off.\n"+ getString(R.string.default_location)+ ".\nTo automatically get current location, please enable location and restart app.",Toast.LENGTH_LONG).show();
                    }
                    break;
                case NO_LAST_LOCATION: // get current location
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null).addOnSuccessListener(location -> {
                        if (location != null) {
                            model.setLocation(new HealthDiaryLocation(/*location.getLatitude(),location.getLongitude()*/));
                            Log.d(getString(R.string.log_tag),String.format(Locale.ENGLISH,"Got current Location: lat %.2f, lon %.2f",location.getLatitude(),location.getLongitude()));
                        }
                        else {   // if something went wrong, set to default and inform:
                            model.setLocation(new HealthDiaryLocation());
                            Toast.makeText(this,"Something went wrong\u2026 "+ getString(R.string.default_location),Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                default:
                    break;
            }
        });


        // get shared pref for saved pw
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    getString(R.string.pref_file_key),
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e){
            Log.w(getString(R.string.log_tag),"Error reading encrypted shared preference (loginActivity)", e);
        }

        // save button
        findViewById(R.id.buttonLoginSave).setOnClickListener(this);

        // default button
        findViewById(R.id.buttonDefaultMasterPw).setOnClickListener(this);

        // get button
        findViewById(R.id.buttonLoginAsPat).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        boolean confirmed = false, tried = false;
        // save
        if(v.getId() == R.id.buttonLoginSave){
            tried = true;
            confirmed = confirmMaster(etPw.getText().toString());
        }
        // default
        if(v.getId() == R.id.buttonDefaultMasterPw){
            // gets a more or less device unique-id, which can reset on factory reset, a little better than hardcoding for all users
            String s = Build.BOARD + Build.BRAND + Build.getRadioVersion() + Build.CPU_ABI + Build.CPU_ABI2 + Build.DEVICE + Build.DISPLAY + Build.HOST + Build.BOOTLOADER +
                    "\u007c>> \u2695 \u2627 \u2623 if I use this, i agree that my soul now belongs to möritz\u1228g.\u263B \u2627 \u2679 <<\u007c" + Build.ID + Build.MANUFACTURER +
                    Build.HARDWARE + Build.MODEL + Build.PRODUCT + Build.TAGS + Build.TYPE + Build.FINGERPRINT + Build.USER + Secure.getString(getContentResolver(), Secure.ANDROID_ID);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                s = HealthDiaryPatient.byteToHexString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
                tried = true;
            } catch (NoSuchAlgorithmException should_not_come_to_this) {
                should_not_come_to_this.printStackTrace();
            }
            if(tried) {
                confirmed = confirmMaster(s);
            }
        }
        // get
        if(v.getId() == R.id.buttonLoginAsPat){
            String result = (null != sharedPreferences) ? sharedPreferences.getString(getString(R.string.key2),"") : "";
            tried = !"".equals(result);
            if (tried){
                confirmed = confirmMaster(result);
            }
        }
        // the same for all
        if (!tried && !confirmed) {
            Toast.makeText(this, getString(R.string.toast_no_saved_pwd), Toast.LENGTH_SHORT).show();
            return;
        }
        if (tried && !confirmed){
            Toast.makeText(this, getString(R.string.toast_wrong_pwd), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!tried){
            Toast.makeText(this, "??", Toast.LENGTH_SHORT).show();
            return;
        }
        // was confirmed, so ask how to proceed:
        showFirstFragment();
        // leave the rest of the logic to status-observer
    }

    private void showFirstFragment(){
        DialogFragment frag = new ChangePatientFragment();
        frag.show(getSupportFragmentManager(),"proceedHow");
    }

    private void showCreatePatientFragment(){
        DialogFragment frag = new CreatePatientFragment();
        frag.show(getSupportFragmentManager(),"addNew");
    }

    private void showListPatientsFragment(){
        DialogFragment frag = new ListAllPatientsFragment();
        frag.show(getSupportFragmentManager(),"listAll");
    }

    private void showPermissionRationaleFragment(){
        DialogFragment frag = new PermissionRationaleFragment();
        frag.show(getSupportFragmentManager(),"permission");
    }

    boolean confirmMaster(String pwd) {
        String savedPwd = null != sharedPreferences ? sharedPreferences.getString(getString(R.string.key2), "") : null;
        // if getting pwd from sharedPrefs worked but doesn't match input, reject
        if (null != savedPwd && !"".equals(savedPwd) && !pwd.equals(savedPwd)) return false;
        // try opening db with supplied pw
        try (HealthDiaryUsersDAO dao = new HealthDiaryUsersDAO(this, pwd)) {// try-with to auto-close dao after use
            model.setAllPatients(dao.getPatients());
        } catch (SQLiteDatabaseCorruptException wrong_pw_for_existing_db) {
            return false;
        }
        // if database could be opened (and the pw is not already saved) save the supplied
        // master-pwd (key2)
        if (null != sharedPreferences && !pwd.equals(savedPwd)) {
            sharedPreferences.edit().putString(getString(R.string.key2), pwd).apply();
        }
        return true;
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

     boolean confirmCurrentUser(){
        HealthDiaryPatient patient = model.getCurrentPatient().getValue();
        HealthDiaryLocation location = model.getLocation().getValue();
        if(location == null){
            location = new HealthDiaryLocation();
            Toast.makeText(this, R.string.default_location, Toast.LENGTH_SHORT).show();
        }
        if(patient == null || patient.getId() < 0){
            Toast.makeText(this,"could not confirm user",Toast.LENGTH_SHORT).show();
            return false;
        }
        try (HealthDiaryDataDAO dao = new HealthDiaryDataDAO(this, patient.hexSha256())) {
            setResult(RESULT_OK, new Intent().
                    putExtra(getString(R.string.current_pat), patient).
                    putExtra(getString(R.string.current_loc), location).
                    putExtra(getString(R.string.avg_bp), dao.getAverageBloodPressure()).
                    putExtra(getString(R.string.avg_m), dao.getAverageBodyMass()).
                    putExtra(getString(R.string.latest_bp),dao.getLatestBloodPressure()).
                    putExtra(getString(R.string.latest_m),dao.getLatestBodyMass()).
                    putExtra(getString(R.string.latest_t),dao.getLatestTemperature()));
        } catch (SQLiteDatabaseCorruptException wrong_pw_for_existing_db) {
            Log.d(getString(R.string.log_tag),"Could not get average for current user\u2026 (loginActivity)");
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, getString(R.string.toast_no_escape), Toast.LENGTH_LONG).show();
    }

}