package com.example.healthdiary.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.healthdiary.R;
import com.example.healthdiary.dataHandling.DataRepository;
import com.example.healthdiary.dataHandling.HealthDiaryUsersDAO;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.Location;

import net.zetetic.database.sqlcipher.SQLiteDatabaseCorruptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etPw = findViewById(R.id.editTextPassword);
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
            Log.w(getString(R.string.log_tag),"Error reading encrypted shared preference Login", e);
        }


        // save button
        findViewById(R.id.buttonLoginSave).setOnClickListener(view -> {
            if (confirm(etPw.getText().toString())) finish();
            else Toast.makeText(this, getString(R.string.toast_wrong_pwd), Toast.LENGTH_SHORT).show();
        });


        // default button
        findViewById(R.id.buttonCreateNewPat).setOnClickListener(view -> {
            // gets a more or less device unique-id, which can reset on factory reset, a little better than hardcoding for all users
            String s = Build.BOARD + Build.BRAND + Build.getRadioVersion() + Build.CPU_ABI + Build.CPU_ABI2 + Build.DEVICE + Build.DISPLAY + Build.HOST + Build.BOOTLOADER +
                    "\u007c>> \u2695 \u2627 \u2623 if I use this, i agree that my soul now belongs to möritz\u1228g.\u263B \u2627 \u2679 <<\u007c" + Build.ID + Build.MANUFACTURER +
                    Build.HARDWARE + Build.MODEL + Build.PRODUCT + Build.TAGS + Build.TYPE + Build.FINGERPRINT + Build.USER + Secure.getString(getContentResolver(), Secure.ANDROID_ID);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                s = byteToHexString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
            } catch (NoSuchAlgorithmException should_not_come_to_this) {
                should_not_come_to_this.printStackTrace();
            }
            Log.i(getString(R.string.log_tag),"default pw is: " + s);
            if (confirm(s)) finish();
            else Toast.makeText(this, getString(R.string.toast_wrong_pwd), Toast.LENGTH_SHORT).show();
        });

        // get button
        findViewById(R.id.buttonLoginAsPat).setOnClickListener(view -> {
            String result = null!=sharedPreferences ? sharedPreferences.getString(getString(R.string.key2),"") : "";
            if (!"".equals(result) && confirm(result)) finish();
            else Toast.makeText(this, getString(R.string.toast_no_saved_pwd), Toast.LENGTH_SHORT).show();
        });
    }

    protected boolean confirm(String pwd) {
        String savedPwd = null != sharedPreferences ? sharedPreferences.getString(getString(R.string.key2), "") : null;
        // if getting pwd from sharedPrefs worked but doesn't match input, reject
        if (null != savedPwd && !"".equals(savedPwd) && !pwd.equals(savedPwd)) return false;
        // try opening db with supplied pw
        try (HealthDiaryUsersDAO dao = new HealthDiaryUsersDAO(this, pwd)) {// try-with to auto-close dao after use
            HealthDiaryPatient patient = new HealthDiaryPatient();
            setResult(RESULT_OK, new Intent().
                    putExtra(getString(R.string.current_pat), patient).
                    putExtra(getString(R.string.current_loc), new Location())/*.
                    putExtra(getString(R.string.avg_bp), dao.getAverageBloodPressure()).
                    putExtra(getString(R.string.avg_m), dao.getAverageBodyMass()).
                    putExtra(getString(R.string.latest_bp),dao.getLatestBloodPressure()).
                    putExtra(getString(R.string.latest_m),dao.getLatestBodyMass()).
                    putExtra(getString(R.string.latest_t),dao.getLatestTemperature()))*/ );
        } catch (SQLiteDatabaseCorruptException wrong_pw_for_existing_db) {
            return false;
        }
        // if database could be opened (and the pw is not already saved) save the supplied pw
        if (null != sharedPreferences && !pwd.equals(savedPwd)) {
            sharedPreferences.edit().putString(getString(R.string.key2), pwd).apply();
        }
        // debug random pat:
        DataRepository.getInstance().getPatient().setValue(new HealthDiaryPatient());
        return true;
    }

    /**
     * @param payload byte array
     * @return big endian hex representation or empty string if null
     */
    private String byteToHexString(byte[] payload) {
        if (payload == null) return "";
        StringBuilder stringBuilder = new StringBuilder(payload.length);
        for (byte byteChar : payload)
            stringBuilder.append(String.format("%02x", byteChar));
        return stringBuilder.toString();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, getString(R.string.toast_no_escape), Toast.LENGTH_LONG).show();
    }
}