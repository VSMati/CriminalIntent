package com.example.criminalintent.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

@Dao
public interface CrimeDao {
    @Query("SELECT * FROM crime")
    List<Crime> getAll();

    @Query("SELECT * FROM crime WHERE mId = :id")
    @TypeConverters({IdConverter.class})
    Crime getById(UUID id);

    @Insert
    long insert(Crime crime);

    @Delete
    void delete(Crime crime);

    @Update
    void update(Crime crime);
}
