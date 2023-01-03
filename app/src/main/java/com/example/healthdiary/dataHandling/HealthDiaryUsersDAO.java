package com.example.healthdiary.dataHandling;

import android.content.Context;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteDatabaseCorruptException;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

import java.io.Closeable;

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

    // TODO implement DA


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
