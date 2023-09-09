package com.mahsunsayak.myapplication.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Art (

    @ColumnInfo(name = "name")
    var name : String,

    @ColumnInfo(name = "artistname")
    var artistName: String?,

    @ColumnInfo(name = "year")
    var year : String?,

    @ColumnInfo(name = "image")
    var image : ByteArray?

    ) {
        @PrimaryKey(autoGenerate = true)
        var id = 0 //Bu alan otomatik olarak oluşturulan birincil anahtar (primary key) olarak kullanılır.
}