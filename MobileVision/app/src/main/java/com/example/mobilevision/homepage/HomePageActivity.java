package com.example.mobilevision.homepage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mobilevision.R;
import com.example.mobilevision.StillOcrActivity;
import com.example.mobilevision.database.Bills;
import com.example.mobilevision.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Homepage class
 */
public class HomePageActivity extends AppCompatActivity {

    private FloatingActionButton openCameraFab;
    private RecyclerView recyclerView;
    private ArrayList<Bills> prices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        openCameraFab = findViewById(R.id.homepage_fab_camera);
        recyclerView = findViewById(R.id.homepage_rv_bills);
        openCameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                startActivity(new Intent(HomePageActivity.this, StillOcrActivity.class));
            }
        });

        //get all the data stored in the database
        prices = (ArrayList<Bills>)DatabaseHelper.getInstance(this)
                .getDatabase()
                .billsDao()
                .getAll();

        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        HomepageAdapter adapter = new HomepageAdapter(recyclerView, prices, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //get data(including any new ones, if any) from database coming back
        prices = (ArrayList<Bills>)DatabaseHelper.getInstance(this)
                .getDatabase()
                .billsDao()
                .getAll();
        setUpRecyclerView();
    }
}
