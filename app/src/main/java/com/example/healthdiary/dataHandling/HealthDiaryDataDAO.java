package com.example.healthdiary.dataHandling;

import com.example.healthdiary.R;
import com.example.healthdiary.dataTypes.BloodPressureReading;
import com.example.healthdiary.dataTypes.BodyMassReading;
import com.example.healthdiary.dataTypes.TemperatureReading;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteDatabaseCorruptException;

import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class HealthDiaryDataDAO implements IHealthDiaryDataDb, Closeable {
    private final SQLiteDatabase db;
    private final HealthDiaryDataSQLiteHelper dbHelper;

     public HealthDiaryDataDAO(Context ctx, String patientHash) throws SQLiteDatabaseCorruptException{
         System.loadLibrary("sqlcipher");
         String db_name = String.format(DB_NAME_BASE, patientHash);
         dbHelper = new HealthDiaryDataSQLiteHelper(ctx, db_name, getPref(ctx));
         db = dbHelper.getWritableDatabase();
     }

    public HealthDiaryDataDAO(Context ctx, String patientHash, String pw) throws SQLiteDatabaseCorruptException/* if wrong pw or db corrupt*/, NullPointerException /*if no current patient*/ {
        System.loadLibrary("sqlcipher");
        String db_name = String.format(DB_NAME_BASE, patientHash);
        dbHelper = new HealthDiaryDataSQLiteHelper(ctx, db_name, pw);
        db = dbHelper.getWritableDatabase();
    }

    @Override
    public void close(){
        dbHelper.close();
    }

    /**
     * @param bpReading the reading to be saved in the db
     * @return _id (rowid) of new entry
     */
    public long addBloodPressureReading(BloodPressureReading bpReading){
        if (null != bpReading && bpReading.getSys() > 0 && bpReading.getDia() > 0){
            ContentValues content = new ContentValues();
            content.put(KEY_SYS,bpReading.getSys());
            content.put(KEY_DIA, bpReading.getDia());
            content.put(KEY_UNIT, bpReading.getUnit());
            content.put(KEY_PAT_ID, bpReading.getPatientId());
            content.put(KEY_TS,bpReading.getTimeStamp());
            return db.insert(TABLE_BP,null,content);
        }
        return -1L;
    }

    /**
     * @param bmReading the reading to be saved in the db
     * @return _id (rowid) of new entry, -1 if unsuccessful
     */
    public long addBodyMassReading(BodyMassReading bmReading){
        if (null != bmReading && !Double.isNaN(bmReading.getMass())){
            ContentValues content = new ContentValues();
            content.put(KEY_MASS,bmReading.getMass());
            content.put(KEY_UNIT, bmReading.getUnit());
            content.put(KEY_PAT_ID, bmReading.getPatientId());
            content.put(KEY_TS,bmReading.getTimeStamp());
            return db.insert(TABLE_M,null,content);
        }
        return -1L;
    }

    /**
     * @param tReading the reading to be saved in the db
     * @return _id (rowid) of new entry, -1 if unsuccessful
     */
    public long addTemperatureReading(TemperatureReading tReading){
        if(null != tReading && !Double.isNaN(tReading.getTemperature())){
            double[] coord = tReading.getCoordinates();
            ContentValues content = new ContentValues();
            content.put(KEY_TEMP, tReading.getTemperature());
            content.put(KEY_UNIT, tReading.getUnit());
            content.put(KEY_LAT, coord[0]);
            content.put(KEY_LON, coord[1]);
            content.put(KEY_NAME, tReading.getLocationName());
            content.put(KEY_TS, tReading.getTimeStamp());
            return db.insert(TABLE_T,null,content);
        }
        return -1L;
    }

    /**
     * @return average blood pressure with timestamp of calculation <br>
     * only takes the unit of the first entry into account so do not change the units on an existing db
     */
    public BloodPressureReading getAverageBloodPressure(){
        Cursor cursor = db.rawQuery("SELECT CAST(AVG("+KEY_SYS+")+0.5 AS INTEGER), CAST(AVG("+KEY_DIA+")+0.5 AS INTEGER), " + KEY_UNIT + ", " + KEY_PAT_ID + " FROM "+TABLE_BP+" LIMIT 1",null);
        cursor.moveToFirst();
        BloodPressureReading result = new BloodPressureReading(cursor.getInt(0),cursor.getInt(1),cursor.getString(2), cursor.getInt(3));
        Log.d("MyTag",String.format("Got average pressure values from db: %d/%d %s",cursor.getInt(0),cursor.getInt(1),cursor.getString(2)));
        cursor.close();
        return result;
    }
    /**
     * @return average body mass with timestamp of calculation <br>
     * only takes the unit of the first entry into account so do not change the units on an existing db
     */
    public BodyMassReading getAverageBodyMass(){
        Cursor cursor = db.rawQuery("SELECT ROUND(AVG("+KEY_MASS+"),2), "+KEY_UNIT+", " + KEY_PAT_ID +" FROM "+TABLE_M+ " LIMIT 1",null);
        cursor.moveToFirst();
        BodyMassReading result = new BodyMassReading(cursor.getDouble(0),cursor.getString(1),cursor.getInt(2));
        cursor.close();
        return result;
    }

    public List<BloodPressureReading> getBloodPressureReadings(){
        List<BloodPressureReading> resultList = new ArrayList<>();
        String[] table_columns = new String[] {KEY_SYS, KEY_DIA, KEY_UNIT, KEY_PAT_ID , KEY_TS};
        Cursor cursor = db.query(TABLE_BP,table_columns,null,null,null,null,KEY_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            resultList.add(new BloodPressureReading(cursor.getInt(0),cursor.getInt(1),cursor.getString(2),cursor.getInt(3),cursor.getLong(4)));
            cursor.moveToNext();
        }
        cursor.close();
        return resultList;
    }

    public List<BodyMassReading> getBodyMassReadings(){
        List<BodyMassReading> resultList = new ArrayList<>();
        String[] tableColumns = new String[]{KEY_MASS, KEY_UNIT,KEY_PAT_ID, KEY_TS};
        Cursor cursor = db.query(TABLE_M,tableColumns,null,null,null,null,KEY_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            resultList.add(new BodyMassReading(cursor.getDouble(0),cursor.getString(1),cursor.getInt(2),cursor.getLong(3)));
            cursor.moveToNext();
        }
        cursor.close();
        return resultList;
    }

    public BloodPressureReading getLatestBloodPressure() {
        String[] tableColumns = new String[]{KEY_SYS, KEY_DIA, KEY_UNIT,KEY_PAT_ID, KEY_TS};
        try {
            Cursor cursor = db.query(TABLE_BP, tableColumns, null, null, null, null, KEY_ID +" DESC", "1");
            cursor.moveToFirst();
            BloodPressureReading result = new BloodPressureReading(cursor.getInt(0),cursor.getInt(1),cursor.getString(2),cursor.getInt(3),cursor.getLong(4));
            cursor.close();
            return result;
        } catch (android.database.CursorIndexOutOfBoundsException | net.zetetic.database.sqlcipher.SQLiteException e) {
            return new BloodPressureReading(false);
        }
    }

    public BodyMassReading getLatestBodyMass() {
        String[] tableColumns = new String[]{KEY_MASS, KEY_UNIT,KEY_PAT_ID, KEY_TS};
        try{
            Cursor cursor = db.query(TABLE_M, tableColumns, null, null, null, null, KEY_ID +" DESC", "1");
            cursor.moveToFirst();
            BodyMassReading result = new BodyMassReading(cursor.getDouble(0),cursor.getString(1),cursor.getInt(2),cursor.getLong(3));
            cursor.close();
            return result;
        } catch (android.database.CursorIndexOutOfBoundsException | net.zetetic.database.sqlcipher.SQLiteException e) {
            return new BodyMassReading(Double.NaN);
        }

    }

    public TemperatureReading getLatestTemperature() {
        String[] tableColumns = new String[]{KEY_TEMP, KEY_UNIT, KEY_LAT, KEY_LON, KEY_NAME, KEY_TS};
        try{
            Cursor cursor = db.query(TABLE_T, tableColumns, null, null, null, null, KEY_ID +" DESC", "1");
            cursor.moveToFirst();
            TemperatureReading result = new TemperatureReading(cursor.getDouble(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getLong(5));
            cursor.close();
            return result;
        } catch (android.database.CursorIndexOutOfBoundsException | net.zetetic.database.sqlcipher.SQLiteException e) {
            return new TemperatureReading(Double.NaN);
        }

    }

    private String getPref(Context ctx){
        String result;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    ctx.getString(R.string.pref_file_key),
                    masterKeyAlias,
                    ctx.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            // takes master-password for ease of use, can e changed to key3 for separate pw
            result = sharedPreferences.getString(ctx.getString(R.string.key2),ctx.getString(R.string.default_value2)) ;
            if(ctx.getString(R.string.default_value2).equals(result)) Log.d(ctx.getString(R.string.log_tag),"Could not read encrypted shared pref in DataDAO");
            // else if(ctx.getString(R.string.default_value3).equals(result)) Log.d(ctx.getString(R.string.log_tag),"User-pw set to default\u2026 (dataDAO)");
            else Log.d(ctx.getString(R.string.log_tag),"Read encrypted shared preferences in DataDAO");
            return result;
        } catch (GeneralSecurityException | IOException e){
            Log.w(ctx.getString(R.string.log_tag),"Error reading encrypted shared preference in DataDAO: ", e);
            return ctx.getString(R.string.default_value2);
        }
    }
}
