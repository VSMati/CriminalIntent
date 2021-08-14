package com.example.criminalintent.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private final Context mContext;
    private final SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context){
        if (sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public void addCrime(Crime c){
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeDbSchema.CrimeTable.NAME,null,values);
    }

    public List<Crime> getCrimes(){
        List<Crime> list = new ArrayList<>();

        try (CrimeCursorWrapper cursor = queryCrimes(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                list.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }
        return list;
    }

    public Crime getCrime(UUID id){
        try (CrimeCursorWrapper cursor = queryCrimes(CrimeDbSchema.CrimeTable.Cols.UUID + "=?"
                , new String[]{id.toString()})) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        }
    }

    public void deleteCrime(Crime crime){
        mDatabase.delete(CrimeDbSchema.CrimeTable.NAME
                , CrimeDbSchema.CrimeTable.Cols.UUID + "=?",
                new String[]{crime.getId().toString()});
    }

    public void updateCrime(Crime crime){
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeDbSchema.CrimeTable.NAME, values,
                CrimeDbSchema.CrimeTable.Cols.UUID + "=?",
                new String[]{uuidString});
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeDbSchema.CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new CrimeCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Crime crime){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CrimeDbSchema.CrimeTable.Cols.UUID, String.valueOf(crime.getId()));
        contentValues.put(CrimeDbSchema.CrimeTable.Cols.TITLE, crime.getTitle());
        contentValues.put(CrimeDbSchema.CrimeTable.Cols.DATE, crime.getDate().toString());
        contentValues.put(CrimeDbSchema.CrimeTable.Cols.SOLVED, crime.isSolved() ? 1:0);
        contentValues.put(CrimeDbSchema.CrimeTable.Cols.SUSPECT,crime.getSuspect());

        return contentValues;
    }
}
