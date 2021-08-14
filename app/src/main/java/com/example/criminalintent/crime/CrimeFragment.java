package com.example.criminalintent.crime;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;

import com.example.criminalintent.R;
import com.example.criminalintent.database.Crime;
import com.example.criminalintent.database.CrimeLab;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton, mSendButton, mSuspectButton;
    private CheckBox mSolvedCheckBox;
    private CrimeLab mCrimeLab;

    private static final String ARG_CRIME_ID = "crime_id";

    final Intent pickContact = new Intent(Intent.ACTION_PICK,
            ContactsContract.Contacts.CONTENT_URI);

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
            NavUtils.navigateUpFromSameTask(requireActivity());
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
        mCrimeLab = CrimeLab.get(getActivity());
        mCrime = mCrimeLab.getCrime(crimeId);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime,container,false);

        mTitleField = v.findViewById(R.id.etTitleField);
        mDateButton = v.findViewById(R.id.btnCrimeData);
        mSolvedCheckBox = v.findViewById(R.id.cbSolved);
        mSendButton = v.findViewById(R.id.btnReport);
        mSuspectButton = v.findViewById(R.id.btnSuspect);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PackageManager pm = getActivity().getPackageManager();
        if (pm.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }


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

        mSendButton.setOnClickListener( v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
            intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.crime_report_subject));
            intent = Intent.createChooser(intent,getString(R.string.send_report));
            startActivity(intent);
        });

        mSuspectButton.setOnClickListener( v ->{
            launchSomeActivity.launch(pickContact);
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        getParentFragmentManager().setFragmentResultListener(DatePickerFragment.REQUEST_DATE, this, (requestKey, result) -> {
            Date date = (Date) result.getSerializable(DatePickerFragment.KEY_DATE);
            updateDate(date);
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        mCrimeLab.updateCrime(mCrime);
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
        mCrime.setDate(date);
        mCrimeLab.updateCrime(mCrime);
    }

    public String getCrimeReport(){
        String solvedString = null;
        if (mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else{
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = mCrime.getStringDate();

        String suspect;
        if (mCrime.getSuspect() == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else{
            suspect = getString(R.string.crime_report_suspect,mCrime.getSuspect());
        }

        String report = getString(R.string.crime_report
                ,mCrime.getTitle(),dateString,solvedString,suspect);

        return report;
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri contactUri = data.getData();
                        String[] queryFields = new String[]{
                                ContactsContract.Contacts.DISPLAY_NAME
                        };
                        Cursor c = requireActivity().getContentResolver()
                                .query(contactUri,queryFields,
                                        null,null,null);
                        try {
                            if (c.getCount() == 0){
                                return ;
                            }
                            c.moveToFirst();
                            String suspect = c.getString(0);
                            mCrime.setSuspect(suspect);
                            mSuspectButton.setText(suspect);
                        }finally {
                            c.close();
                        }
                    }
                }
            });
}
