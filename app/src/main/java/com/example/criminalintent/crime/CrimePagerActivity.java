package com.example.criminalintent.crime;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.criminalintent.R;
import com.example.criminalintent.database.Crime;
import com.example.criminalintent.database.CrimeDatabase;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

public class CrimePagerActivity extends AppCompatActivity {
    /*TODO:let me explain you this bug.
    *  So, when i set up my date in CrimeFragment it sets to another CrimeFragment
    * which id is less than this fragment by 1
    * hmm, and results are shown only in CrimePagerActivity
    * CrimeListFragment seems to turn blind eye on this problem */

    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private CrimeDatabase mDatabase;
    private static final String EXTRA_CRIME_ID = "com.example.android.criminalIntent.crime_id";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);
        mDatabase = CrimeDatabase.getInstance(getApplicationContext());
        try {
            mCrimes = new RetrieveTask(CrimePagerActivity.this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        mViewPager = findViewById(R.id.crime_view_pager);
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        for (int i = 0; i < mCrimes.size(); i++){
            if (mCrimes.get(i).getId().equals(crimeId)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }

    public static Intent newIntent(Context packageContext, UUID id){
        Intent intent = new Intent(packageContext,CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID,id);
        return intent;
    }

    private static class RetrieveTask extends AsyncTask<Void, Void, List<Crime>>{
        private WeakReference<CrimePagerActivity> activityReference;

        public RetrieveTask(CrimePagerActivity context) {
            this.activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<Crime> doInBackground(Void... voids) {
            return activityReference.get().mDatabase.getCrimeDao().getAll();
        }
    }
}
