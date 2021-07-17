package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private CrimeLab mCrimeLab;
    private TextView mTitleTextView,mDateTextView;
    private ImageView mSolved;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list,container,false);

        mTitleTextView = v.findViewById(R.id.crime_title);
        mDateTextView = v.findViewById(R.id.crime_date);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCrimeLab = CrimeLab.get(view.getContext());

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setAdapter(new CrimeAdapter(mCrimeLab));
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI(){
        mCrimeLab = CrimeLab.get(getContext());

        if (mAdapter == null){
            mAdapter = new CrimeAdapter(mCrimeLab);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
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
            int itemPosition = mCrimeRecyclerView.getChildLayoutPosition(v);
            Crime currentCrime = mCrimeLab.getCrimes().get(itemPosition);
            Intent intent = CrimeActivity.newIntent(getActivity(),currentCrime.getId());
            startActivity(intent);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{

        private final CrimeLab mCrimes;
        private final View.OnClickListener mClickListener = new CrimeListener();

        public CrimeAdapter(CrimeLab crimes){
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
            Crime crime = mCrimes.getCrimes().get(position);
            holder.getTitleTextView().setText(
                    crime.getTitle());
            holder.getDateTextView().setText(
                    crime.getStringDate());
            mSolved.setVisibility(crime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }


        @Override
        public int getItemCount() {
            return mCrimes.getCrimes().size();
        }
    }
}
