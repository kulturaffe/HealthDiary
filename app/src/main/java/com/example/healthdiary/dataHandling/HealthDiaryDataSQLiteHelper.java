package com.example.healthdiary.dataHandling;

import android.content.Context;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

import java.util.Objects;

/**
 * since it is unfortunately not possible to obtain historic weather data with the given API-key but historic other measurements can be saved,
 * temperature is stored in a separate table with its own timestamp (also accounting for limited refresh-rates of the api and internal caching via OKHttp)
 */
public class HealthDiaryDataSQLiteHelper extends SQLiteOpenHelper implements IHealthDiaryDataDb {
    protected HealthDiaryDataSQLiteHelper(Context ctx, String db_name, String pwd) {
        super(ctx, db_name, pwd,null,DB_VERSION,0,null,null,true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_BP + " (" +
                KEY_ID + " INTEGER PRIMARY KEY ASC," +
                KEY_SYS + " INTEGER," +
                KEY_DIA + " INTEGER,"+
                KEY_UNIT + " TEXT, " +
                KEY_PAT_ID + " INTEGER, " +
                KEY_TS + " INTEGER)");

        database.execSQL("CREATE TABLE " + TABLE_M + " (" +
                KEY_ID + " INTEGER PRIMARY KEY ASC," +
                KEY_MASS + " REAL," +
                KEY_UNIT + " TEXT, " +
                KEY_PAT_ID + " INTEGER, " +
                KEY_TS + " INTEGER)");

        database.execSQL("CREATE TABLE " + TABLE_T + " (" +
                KEY_ID + " INTEGER PRIMARY KEY ASC," +
                KEY_TEMP + " REAL," +
                KEY_UNIT + " TEXT, "+
                KEY_LAT + " REAL,"+
                KEY_LON + " REAL,"+
                KEY_NAME + " TEXT, "+
                KEY_TS + " INTEGER)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_M);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_T);
        onCreate(db);
    }
}
