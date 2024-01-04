package com.hasanbilgin.javamaps.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.hasanbilgin.javamaps.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

//room database kullancağımız için Dao Annotation kullanmalıyız
@Dao
public interface PlaceDao {


    @Query("Select * from Place")
        //List<Place> getAll();
        //Flowable getirildi rxjava için
    Flowable<List<Place>> getAll();

    //parametreli//liste döndürür
//    @Query("Select *from Place where name= :nameinput")
//    List<Place> getSearch(String nameinput);

    //parametereli// dizi entity yani model döndürür //dizi diyebiliriz
//    @Query("select *from Place where id= :idinput")
//    Place findByName(int idinput);

//    dizi parametereli//list döndürmeli
//    @Query("select *from Place where name like :nameinputs")
//    List<Place> loadAllNames(String[] nameinputs);

    //tabi id otomatik oluşçak name gelcek başka kolon varsa paramtereye eklenmesi şart
    @Insert
//    void insert(Place place);
    //rxjava için Completable getirildi
    Completable insert(Place place);

    @Delete
//    void delete(Place place);
        //rxjava için Completable getirildi
    Completable delete(Place place);


}
