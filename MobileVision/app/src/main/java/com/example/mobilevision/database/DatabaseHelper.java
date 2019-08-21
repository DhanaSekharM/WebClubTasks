package com.example.mobilevision.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseHelper {

    private static DatabaseHelper instance;
    private Database database;

    private DatabaseHelper(Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(),
                Database.class,
                "MobileVision")
                .allowMainThreadQueries()
                .build();
    }

    public static DatabaseHelper getInstance(Context context) {
        if(instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    public Database getDatabase() {
        return database;
    }
}
