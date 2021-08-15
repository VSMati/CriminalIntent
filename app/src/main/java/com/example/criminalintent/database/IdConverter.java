package com.example.criminalintent.database;

import androidx.room.TypeConverter;

import java.util.UUID;

public class IdConverter {
    @TypeConverter
    public String fromId(UUID id){
        return id.toString();
    }

    @TypeConverter
    public UUID toId(String id){
        return UUID.fromString(id);
    }
}
