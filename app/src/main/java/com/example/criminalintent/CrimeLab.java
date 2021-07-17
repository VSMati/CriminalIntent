package com.example.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private List<Crime> mCrimes;

    public static CrimeLab get(Context context){
        if (sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mCrimes = new ArrayList<>();
        for (int i = 0; i<100; i++){
            Crime crime = new Crime();
            crime.setTitle(String.format("Преступление № %d",i));
            crime.setSolved(i % 2 == 0);
            mCrimes.add(crime);
        }
    }

    public List<Crime> getCrimes(){
        return mCrimes;
    }

    public Crime getCrime(UUID id){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Optional<Crime> matchingObject = getCrimes().stream().
                    filter(p -> p.getId().equals(id))
                    .findFirst();
            Crime crime = matchingObject.get();
            return crime;
        }else{
            for (Crime crime: mCrimes){
            if (crime.getId() == id){
                return crime;
            }
        }
        return null;
        }
    }
}
