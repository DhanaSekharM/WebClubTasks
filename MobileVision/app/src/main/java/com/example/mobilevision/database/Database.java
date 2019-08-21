package com.example.mobilevision.database;

import androidx.room.Entity;
import androidx.room.RoomDatabase;

/**
 * Table definition
 */
@androidx.room.Database(entities = Bills.class, version = 1)
public abstract class Database extends RoomDatabase {
    public abstract BillsDao billsDao();
}
