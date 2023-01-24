package com.example.healthdiary.dataHandling;

public interface IHealthDiaryUsersDb {
    int DB_VERSION = 2;
    String TABLE_NAME = "health_diary_users",
            KEY_ID = "_id",
            KEY_FIRST = "first_names",
            KEY_LAST = "last_name",
            KEY_SSN = "ssn",
            KEY_ADDRESS = "address_line",
            KEY_ZIP = "zip_code",
            KEY_COUNTRY = "country",
            KEY_DOB = "birthdate_ms";
}
