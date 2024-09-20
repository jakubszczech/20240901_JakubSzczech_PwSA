package com.example.a20240901_pwsa

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_table")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val temperature: Float,
    val pressure: Float,
    val timestamp: String
)
