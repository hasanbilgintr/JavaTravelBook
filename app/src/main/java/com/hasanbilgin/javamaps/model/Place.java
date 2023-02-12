package com.hasanbilgin.javamaps.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;


//room databse kullancağımız için Entity Annotation kullanmalıyız
//@Entity(tableName = "place") de yazılabilir yazılmazsa tablo ismi Place olur yani sınıf ismidir
@Entity
//adapterdeki alındığı için eklendi implements Serializable
public class Place implements Serializable {

    //(autoGenerate = true) id otomatik sıralı bi şekilde olucaktır ondan dolayı consructora katmadık
    @PrimaryKey(autoGenerate = true)
    public int id;

    //"name" kolon adı olucaktır dikkat edelim
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    public Place(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }


}
