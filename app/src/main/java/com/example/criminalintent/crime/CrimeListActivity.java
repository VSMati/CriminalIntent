package com.example.criminalintent.crime;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.criminalintent.R;

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
