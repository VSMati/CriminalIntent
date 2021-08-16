package com.example.criminalintent.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Crime.class},version = 5)
public abstract class CrimeDatabase extends RoomDatabase {
    private static CrimeDatabase sCrimeDb;

    public abstract CrimeDao getCrimeDao();

    public static CrimeDatabase getInstance(Context context) {
        if (sCrimeDb == null){
            sCrimeDb = buildCrimeDatabase(context);
        }
        return sCrimeDb;
    }

    private static CrimeDatabase buildCrimeDatabase(Context context){
        return Room.databaseBuilder(context,CrimeDatabase.class,"database.db")
                .fallbackToDestructiveMigration()
                .build();
    }
}
