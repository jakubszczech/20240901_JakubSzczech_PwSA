package com.example.a20240901_pwsa

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MeasurementEntity::class], version = 1)
abstract class MeasurementDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: MeasurementDatabase? = null

        fun getDatabase(context: Context): MeasurementDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeasurementDatabase::class.java,
                    "measurement_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
