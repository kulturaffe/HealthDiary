package com.example.healthdiary.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthdiary.R;

import java.util.List;

public class DisplayListOfReadingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list_of_readings);
        List<? extends Parcelable> readings = getIntent().getParcelableArrayListExtra(getString(R.string.readings_for_list_tag));
        ListViewAdapter listViewAdapter = new ListViewAdapter(readings);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewAll);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(listViewAdapter);
    }

    @Override // so upward navigation goes to calling activity
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}