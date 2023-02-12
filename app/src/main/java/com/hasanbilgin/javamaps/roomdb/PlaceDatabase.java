package com.hasanbilgin.javamaps.roomdb;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.hasanbilgin.javamaps.model.Place;
//birden fazla tablo varsa eklenmeli buraya
@Database(entities = {Place.class},version =1 )
public abstract class PlaceDatabase extends RoomDatabase {
    public abstract PlaceDao placeDao();
}
