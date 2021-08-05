package com.example.criminalintent;

import android.app.DatePickerDialog;
import android.app.Dialog;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    protected static final String KEY_DATE = "keyDate";
    protected static final String REQUEST_DATE = "requestDate";
    protected static final String TAG = "DatePickerFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(),this,year,month,dayOfMonth);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Date date = new GregorianCalendar(year,month,dayOfMonth).getTime();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DATE,date);
        getParentFragmentManager().setFragmentResult(REQUEST_DATE,bundle);
    }
}
