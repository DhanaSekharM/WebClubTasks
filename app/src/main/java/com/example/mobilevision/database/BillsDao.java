package com.example.mobilevision.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BillsDao {

    @Query("SELECT * FROM Bill")
    List<Bills> getAll();

    @Insert
    void insert(Bills bill);

    @Delete
    void delete(Bills bill);

    @Query("SELECT * FROM Bill WHERE id=:id")
    Bills get(int id);

}
