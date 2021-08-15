package com.example.criminalintent.database;

import android.icu.util.Calendar;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DateConverter {
    @TypeConverter
    public String fromDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat();
        return sdf.format(date);
    }

    @TypeConverter
    public Date toDate(String date){
        SimpleDateFormat sdf = new SimpleDateFormat();
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
