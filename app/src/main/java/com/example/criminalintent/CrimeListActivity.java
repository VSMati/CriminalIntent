package com.example.criminalintent;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CrimeListActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_list);

        FragmentManager fm = getSupportFragmentManager();
        CrimeListFragment fragment = new CrimeListFragment();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragment_container_list, fragment).commit();
    }
}
