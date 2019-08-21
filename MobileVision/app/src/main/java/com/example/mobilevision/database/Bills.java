package com.example.mobilevision.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * Bill table that stores detected price and date
 */
@Entity(tableName = "Bill")
public class Bills {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    private int id;

    @ColumnInfo
    private String price;

    @ColumnInfo
    private String date;



    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
