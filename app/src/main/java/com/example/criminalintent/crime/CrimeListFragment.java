package com.example.criminalintent.crime;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.criminalintent.R;
import com.example.criminalintent.database.Crime;
import com.example.criminalintent.database.CrimeDatabase;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private CrimeDatabase mCrimeDatabase;
    private TextView mTitleTextView,mDateTextView;
    private ImageView mSolved;
    private boolean mSubtitleVisible;

    private static String SAVED_SUBTITLE_VISIBLE = "subtitle_visible";

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        }else{
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                mCrimeDatabase.getInstance(getActivity());
                try {
                    new AddTask(CrimeListFragment.this,crime).execute().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = CrimePagerActivity.newIntent(getActivity(),crime.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                requireActivity().invalidateOptionsMenu();
                try {
                    updateSubtitle();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list,container,false);

        if (savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        mTitleTextView = v.findViewById(R.id.crime_title);
        mDateTextView = v.findViewById(R.id.crime_date);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCrimeDatabase = mCrimeDatabase.getInstance(view.getContext());

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        try {
            List<Crime> crimeList = new RetrieveTask(CrimeListFragment.this)
                    .execute().get();
            mCrimeRecyclerView.setAdapter(new CrimeAdapter(crimeList));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        try {
            updateUI();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            updateUI();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }



    public class CrimeHolder extends RecyclerView.ViewHolder{
        private final TextView mTitleTextView;
        private final TextView mDateTextView;

        public CrimeHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolved = itemView.findViewById(R.id.crime_solved);
        }


        public TextView getTitleTextView() {
            return mTitleTextView;
        }

        public TextView getDateTextView() {
            return mDateTextView;
        }
    }

    class CrimeListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            int itemPosition = mCrimeRecyclerView.getChildAdapterPosition(v);
            try {
                List<Crime> crimeList = new RetrieveTask(CrimeListFragment.this)
                        .execute().get();
                Crime currentCrime = crimeList.get(itemPosition);
                Intent intent = CrimePagerActivity.newIntent(getActivity(),currentCrime.getId());
                startActivity(intent);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private List<Crime> mCrimes;
        private final View.OnClickListener mClickListener = new CrimeListener();

        public CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder( ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_crime,parent,false);
            v.setOnClickListener(mClickListener);
            return new CrimeHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeListFragment.CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.getTitleTextView().setText(
                    crime.getTitle());
            holder.getDateTextView().setText(
                    crime.getStringDate(requireActivity().getApplicationContext()));
            mSolved.setVisibility(crime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }


        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }
    }

    private void updateUI() throws ExecutionException, InterruptedException {
        mCrimeDatabase = CrimeDatabase.getInstance(getContext());
        List<Crime> crimeList = new RetrieveTask(CrimeListFragment.this)
                .execute().get();
        if (mAdapter == null){
            mAdapter = new CrimeAdapter(crimeList);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setCrimes(crimeList);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    private void updateSubtitle() throws ExecutionException, InterruptedException {
        mCrimeDatabase = CrimeDatabase.getInstance(getActivity());
        List<Crime> crimeList = new RetrieveTask(CrimeListFragment.this)
                .execute().get();
        int crimeCount = crimeList.size();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_plural,crimeCount,crimeCount);

        if (!mSubtitleVisible){
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE,mSubtitleVisible);
    }

    private static class RetrieveTask extends AsyncTask< Void,Void,List<Crime> > {
        private final WeakReference<CrimeListFragment> activityReference;

        public RetrieveTask(CrimeListFragment context) {
            this.activityReference = new WeakReference<>(context);
        }
        @Override
        protected List<Crime> doInBackground(Void... voids) {
            return activityReference.get().mCrimeDatabase.getCrimeDao().getAll();
        }
    }

    private static class DeleteTask extends AsyncTask< Void,Void,Void > {
        private final WeakReference<CrimeListFragment> activityReference;
        private final Crime crime;

        public DeleteTask(CrimeListFragment context, Crime crime) {
            this.activityReference = new WeakReference<>(context);
            this.crime = crime;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            activityReference.get().mCrimeDatabase.getCrimeDao().delete(crime);
            return null;
        }
    }

    private static class AddTask extends AsyncTask<Void, Void, Long> {
        private final WeakReference<CrimeListFragment> activityReference;
        private final Crime crime;

        public AddTask(CrimeListFragment context, Crime crime) {
            this.activityReference = new WeakReference<>(context);
            this.crime = crime;
        }
        @Override
        protected Long doInBackground(Void... voids) {
            return activityReference.get().mCrimeDatabase.getCrimeDao().insert(crime);
        }
    }

}
