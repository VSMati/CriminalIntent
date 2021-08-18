package com.example.criminalintent.crime;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.criminalintent.R;
import com.example.criminalintent.database.Crime;
import com.example.criminalintent.database.CrimeDatabase;
import com.example.criminalintent.pictures.PictureUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton, mSendButton, mSuspectButton, mDialButton, mCameraButton;
    private CheckBox mSolvedCheckBox;
    private CrimeDatabase mCrimeDatabase;
    private ImageView mPhoto;

    private File mPhotoFile;

    private static final String ARG_CRIME_ID = "crime_id";

    private final ActivityResultLauncher<Intent> mRetrieveLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                getContactName(result);
                getContactNumber(result);
            });

    final Intent pickContact = new Intent(Intent.ACTION_PICK,
            ContactsContract.Contacts.CONTENT_URI);


    private ActivityResultLauncher mSuspectLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    result -> {
                        if (result){
                            //TODO: continue workflow
                            mRetrieveLauncher.launch(pickContact);
                        }else{
                            //TODO: add dialog-explanation
                        }
                    });
    final Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //todo: can't seem to set a photo to imageview
    final ActivityResultLauncher<Intent> mPhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                mPhotoFile = new File(directory,mCrime.getPhotoFileName());
                Uri uri = FileProvider.getUriForFile(requireContext(),
                        "com.example.criminalintent.fileprovider",mPhotoFile);
                takePhoto.putExtra(MediaStore.EXTRA_OUTPUT,uri);

                Uri imageUri = result.getData().getData();
                ContentResolver cr = requireActivity().getContentResolver();
                Bitmap bitmap = PictureUtils.getBitmapFromUri(imageUri,cr);
                PictureUtils.saveToInternalStorage(bitmap,mPhotoFile);
            });


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_crime) {
            mCrimeDatabase = CrimeDatabase.getInstance(getContext());
            new DeleteTask(CrimeFragment.this,mCrime).execute();
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
        mCrimeDatabase = CrimeDatabase.getInstance(getActivity());
        try {
            mCrime = new RetrieveTask(CrimeFragment.this, crimeId)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        mPhotoFile = mCrimeDatabase.getPhotoFile(mCrime,getContext());
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
        mDialButton = v.findViewById(R.id.btnDial);
        mCameraButton = v.findViewById(R.id.btnCamera);
        mPhoto = v.findViewById(R.id.ivPhoto);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PackageManager pm = requireActivity().getPackageManager();
        if (pm.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }

        updateDate(mCrime.getDate());
        updatePhotoView();

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

        mSuspectButton.setOnClickListener( v -> {
           getPermissionToReadUserContacts();
        });

        mDialButton.setOnClickListener(v -> {
            Intent dialContact = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+mCrime.getNumber()));
            startActivity(dialContact);
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }

        getParentFragmentManager().setFragmentResultListener(DatePickerFragment.REQUEST_DATE, this, (requestKey, result) -> {
            Date date = (Date) result.getSerializable(DatePickerFragment.KEY_DATE);
            updateDate(date);
        });

        boolean canTakePhoto = mPhotoFile != null &&
                takePhoto.resolveActivity(getActivity().getPackageManager()) != null;
        mCameraButton.setEnabled(canTakePhoto);

        mCameraButton.setOnClickListener( v -> {
            mPhotoLauncher.launch(takePhoto);
            updatePhotoView();
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        new UpdateTask(CrimeFragment.this,mCrime)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static CrimeFragment newInstance(UUID id){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID,id);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void updateDate(Date date){
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(getActivity().getApplicationContext());
        mDateButton.setText(dateFormat.format(date));
        mCrime.setDate(date);
        new UpdateTask(CrimeFragment.this,mCrime)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void updatePhotoView(){
        if (mPhotoFile == null || !mPhotoFile.exists()){
            mPhoto.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        } else {
            Bitmap bitmap = PictureUtils.getFromInternalStorage(mPhotoFile);
            mPhoto.setImageBitmap(bitmap);
        }
    }

    public String getCrimeReport(){
        String solvedString;
        if (mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else{
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = mCrime.getStringDate(requireActivity().getApplicationContext());

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

    private void getPermissionToReadUserContacts(){
        mSuspectLauncher.launch(Manifest.permission.READ_CONTACTS);
    }

    private static class RetrieveTask extends AsyncTask< Void,Void,Crime >{
        private final WeakReference<CrimeFragment> activityReference;
        private final UUID mUUID;

        public RetrieveTask(CrimeFragment context, UUID id) {
            this.activityReference = new WeakReference<>(context);
            this.mUUID = id;
        }
        @Override
        protected Crime doInBackground(Void... voids) {
            return activityReference.get().mCrimeDatabase.getCrimeDao().getById(mUUID);
        }
    }

    private static class UpdateTask extends AsyncTask< Void,Void,Void>{
        private final WeakReference<CrimeFragment> activityReference;
        private final Crime crime;

        public UpdateTask(CrimeFragment context, Crime crime) {
            this.activityReference = new WeakReference<>(context);
            this.crime = crime;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            activityReference.get().mCrimeDatabase.getCrimeDao().update(crime);
            return null;
        }
    }

    private static class DeleteTask extends AsyncTask< Void,Void,Void>{
        private final WeakReference<CrimeFragment> activityReference;
        private final Crime crime;

        public DeleteTask(CrimeFragment context, Crime crime) {
            this.activityReference = new WeakReference<>(context);
            this.crime = crime;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            activityReference.get().mCrimeDatabase.getCrimeDao().delete(crime);
            return null;
        }
    }

    private void getContactName(ActivityResult result){
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            try (Cursor c = requireActivity().getContentResolver()
                    .query(contactUri, queryFields,
                            null, null, null)) {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            }
        }
    }
    private void getContactNumber(ActivityResult result){
        if (result.getResultCode() == Activity.RESULT_OK){
            Intent data = result.getData();
            Uri contactUri = data.getData();
            Cursor cursorId = requireActivity().getContentResolver()
                    .query(contactUri,new String[]{ContactsContract.Contacts._ID},
                            null,null,null);
            if (cursorId.getCount() == 0){
                return ;
            }
            cursorId.moveToFirst();
            String id = cursorId.getString(0);
            cursorId.close();

            Cursor cursorPhone = requireActivity().getContentResolver()
                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                            new String[]{id},
                            null);
            if (cursorPhone.getCount() == 0){
                return ;
            }
            cursorPhone.moveToFirst();
            String number = cursorPhone.getString(0);
            mCrime.setNumber(number);
        }
    }

    private void setPic(){
        int targetW = mPhoto.getWidth();
        int targetH = mPhoto.getHeight();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mPhotoFile.getPath(),options);
        int photoW = options.outWidth;
        int photoH = options.outHeight;

        int scaleFactor = Math.max(1,Math.min(photoW/targetW,photoH/targetH));

        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoFile.getPath(),options);
        mPhoto.setImageBitmap(bitmap);

    }
}


