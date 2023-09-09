package com.mahsunsayak.myapplication.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mahsunsayak.myapplication.model.Art

@Database(entities = [Art::class], version = 1)
abstract class ArtDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
}