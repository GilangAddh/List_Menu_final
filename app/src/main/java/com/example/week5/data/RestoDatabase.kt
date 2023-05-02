package com.example.week5.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.week5.data.makanan.Makanan
import com.example.week5.data.makanan.MakananDao

@Database(entities = [Makanan::class], version = 1)
abstract class RestoDatabase : RoomDatabase() {
    abstract fun getMakananDao(): MakananDao

    companion object{
        @Volatile
        private var instance: RestoDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            RestoDatabase::class.java,
            "resto-db"
        ).build()
    }
}