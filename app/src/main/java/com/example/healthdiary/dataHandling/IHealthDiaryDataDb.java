package com.example.healthdiary.dataHandling;

interface IHealthDiaryDataDb {
    int DB_VERSION = 1;
    String DB_NAME_BASE = "health_diary_%s.db",
            KEY_ID = "_id",
            KEY_UNIT = "unit",
            KEY_TS = "time_stamp",
            TABLE_BP = "blood_pressure_recordings",
            KEY_SYS = "systolic",
            KEY_DIA = "diastolic",
            TABLE_M = "body_mass_recordings",
            KEY_MASS = "body_mass",
            TABLE_T = "temperature_recordings",
            KEY_TEMP = "temperature",
            KEY_LAT = "laditude_degrees",
            KEY_LON = "longitude_degrees",
            KEY_NAME = "location_name",
            KEY_PAT_ID = "patientId";
}