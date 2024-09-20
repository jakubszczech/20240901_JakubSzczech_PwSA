package com.example.a20240901_pwsa
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Insert
    fun insert(measurement: MeasurementEntity)

    // Użyj List<MeasurementEntity>, jeśli chcesz pobrać wszystkie wpisy jednorazowo
    @Query("SELECT * FROM measurement_table ORDER BY id DESC")
   fun getAllMeasurements(): List<MeasurementEntity>

    // Alternatywnie, możesz użyć Flow, jeśli chcesz nasłuchiwać zmian w czasie rzeczywistym
    @Query("SELECT * FROM measurement_table ORDER BY id DESC")
    fun getAllMeasurementsFlow(): Flow<List<MeasurementEntity>>
}

