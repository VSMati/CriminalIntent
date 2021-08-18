package com.example.criminalintent.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

@Entity
public class Crime {
    @PrimaryKey
    @TypeConverters({IdConverter.class})
    @NonNull
    private UUID mId;

    @ColumnInfo(name = "suspect")
    private String mSuspect;

    @ColumnInfo(name="title")
    private String mTitle;

    @ColumnInfo(name = "date")
    @TypeConverters({DateConverter.class})
    @NonNull
    private Date mDate;

    @ColumnInfo(name = "solved")
    private boolean mSolved;

    @ColumnInfo(name = "number")
    private String mNumber;

    public Crime(){
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public void setId(UUID id) {
        mId = id;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public String getStringDate(Context context){
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
        return dateFormat.format(mDate);
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        mNumber = number;
    }

    @Ignore
    public String getPhotoFileName(){
        return "IMG_" + getId().toString() + ".jpg";
    }
}
