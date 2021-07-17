package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.UUID;

public class CrimeActivity extends AppCompatActivity {
    public static final String EXTRA_CRIME_ID = "com.example.android.criminalIntent.crime_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        UUID crimeId = (UUID) getIntent()
                .getSerializableExtra(CrimeActivity.EXTRA_CRIME_ID);

        FragmentManager fm = getSupportFragmentManager();
        CrimeFragment fragment = CrimeFragment.newInstance(crimeId);
        FragmentTransaction ft = fm.beginTransaction();

        ft.add(R.id.fragment_container, fragment).commit();
    }

    public static Intent newIntent(Context packageContext, UUID crimeId){
        Intent intent = new Intent(packageContext,CrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_ID,crimeId);
        return intent;
    }
}