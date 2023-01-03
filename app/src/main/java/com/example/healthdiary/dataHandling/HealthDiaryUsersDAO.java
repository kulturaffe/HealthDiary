package com.example.healthdiary.dataHandling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.healthdiary.dataTypes.BloodPressureReading;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteDatabaseCorruptException;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class HealthDiaryUsersDAO implements Closeable, IHealthDiaryUsersDb {
    private final SQLiteDatabase db;
    private final HealthDiaryUsersSQLiteHelper dbHelper;

    /*
    public HealthDiaryUsersDAO(Context ctx) throws SQLiteDatabaseCorruptException {
        System.loadLibrary("sqlcipher");
        dbHelper = new HealthDiaryUsersSQLiteHelper(ctx, getPref(ctx));
        db = dbHelper.getWritableDatabase();
    }
    */

    public HealthDiaryUsersDAO(Context ctx, String pw) throws SQLiteDatabaseCorruptException{
        System.loadLibrary("sqlcipher");
        dbHelper = new HealthDiaryUsersSQLiteHelper(ctx, pw);
        db = dbHelper.getWritableDatabase();
    }

    @Override
    public void close(){
        dbHelper.close();
    }


    /**
     * @param patient the patient to be saved in the db
     * @return _id (rowid) of new entry
     */
    public long addPatient(HealthDiaryPatient patient){
        if (null != patient){
            ContentValues content = new ContentValues();
            content.put(KEY_FIRST, patient.getFirstNames());
            content.put(KEY_LAST, patient.getLastName());
            content.put(KEY_SSN, patient.getSsn());
            content.put(KEY_ADDRESS, patient.getAddressLine());
            content.put(KEY_ZIP, patient.getZip());
            content.put(KEY_COUNTRY, patient.getCountry());
            content.put(KEY_DOB, patient.getDobTs());
            return db.insert(TABLE_NAME ,null,content);
        }
        return -1L;
    }

    public List<HealthDiaryPatient> getPatients(){
        List<HealthDiaryPatient> resultList = new ArrayList<>();
        String[] table_columns = new String[] {KEY_ID, KEY_FIRST, KEY_LAST, KEY_SSN, KEY_ADDRESS, KEY_ZIP, KEY_COUNTRY, KEY_DOB};
        Cursor cursor = db.query(TABLE_NAME,table_columns,null,null,null,null,KEY_ID);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            resultList.add(new HealthDiaryPatient(cursor.getLong(0),cursor.getString(1),
                    cursor.getString(2),cursor.getString(3),cursor.getString(4),
                    cursor.getString(5),cursor.getString(6),cursor.getLong(7)));
            cursor.moveToNext();
        }
        cursor.close();
        return resultList;
    }

    public static class HealthDiaryUsersSQLiteHelper extends SQLiteOpenHelper implements IHealthDiaryUsersDb {
        private static final String DB_NAME = "abf2da6491a3bb6a2ece15a6daa5cf5e9724e8cd8a013e413d9f5772968e379c"; // SHA-256 of "health_diary_users.db"

        public HealthDiaryUsersSQLiteHelper(Context context, String pwd) {
            super(context, DB_NAME, pwd, null, DB_VERSION, 0,null,null,true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY ASC," +
                    KEY_FIRST + " TEXT," +
                    KEY_LAST + " TEXT,"+
                    KEY_SSN + " TEXT, " +
                    KEY_ADDRESS + " TEXT, " +
                    KEY_ZIP + " TEXT, " +
                    KEY_COUNTRY + " TEXT, " +
                    KEY_DOB + " INTEGER)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
