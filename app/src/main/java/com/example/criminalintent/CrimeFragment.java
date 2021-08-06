package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;

    private static final String ARG_CRIME_ID = "crime_id";

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_crime) {
            CrimeLab crimeLab = CrimeLab.get(getContext());
            crimeLab.deleteCrime(mCrime);
            Intent intent = CrimePagerActivity.newIntent(getContext(), mCrime.getId());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        assert getArguments() != null;
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        mCrime = crimeLab.getCrime(crimeId);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime,container,false);

        mTitleField = v.findViewById(R.id.etTitleField);
        mDateButton = v.findViewById(R.id.btnCrimeData);
        mSolvedCheckBox = v.findViewById(R.id.cbSolved);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateDate(mCrime.getDate());

        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> mCrime.setSolved(isChecked));

        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton.setOnClickListener(v -> {
            DatePickerFragment dpf = new DatePickerFragment();
            dpf.show(getParentFragmentManager(),DatePickerFragment.TAG);
        });

        getParentFragmentManager().setFragmentResultListener(DatePickerFragment.REQUEST_DATE, this, (requestKey, result) -> {
            Date date = (Date) result.getSerializable(DatePickerFragment.KEY_DATE);
            mCrime.setDate(date);
            updateDate(date);
        });
    }

    public static CrimeFragment newInstance(UUID id){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID,id);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void updateDate(Date date){
        DateFormat dateFormat = DateFormat.getDateInstance();
        mDateButton.setText(dateFormat.format(date));
    }
}
