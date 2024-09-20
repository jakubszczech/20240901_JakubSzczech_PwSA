package com.example.a20240901_pwsa

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.a20240901_pwsa.ui.theme._20240901_PwSATheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var temperatureSensor: Sensor? = null
    private var pressureSensor: Sensor? = null

    private lateinit var db: MeasurementDatabase

    // Te zmienne będą przechowywać aktualne dane z sensorów
    private var currentTemperature by mutableStateOf(0f)
    private var currentPressure by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicjalizacja sensorów
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        db = MeasurementDatabase.getDatabase(this)

        setContent {
            _20240901_PwSATheme {
                MainScreen(db, currentTemperature, currentPressure)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Rejestrujemy nasłuchiwanie sensorów
        temperatureSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        pressureSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Wyrejestrowanie nasłuchiwania sensorów, aby oszczędzać baterię
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Obsługa zmian danych z sensorów
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                    currentTemperature = it.values[0]
                }
                Sensor.TYPE_PRESSURE -> {
                    currentPressure = it.values[0]
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nie musisz nic tutaj implementować, chyba że chcesz monitorować zmianę dokładności
    }
}


@Composable
fun MainScreen(db: MeasurementDatabase, currentTemperature: Float, currentPressure: Float) {
    var measurements by remember { mutableStateOf(listOf<MeasurementEntity>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Ładowanie zapisanych pomiarów z bazy danych
        withContext(Dispatchers.IO) {
            measurements = db.measurementDao().getAllMeasurements()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Temperatura: ${"%.2f".format(currentTemperature)}°C", style = MaterialTheme.typography.titleLarge)
        Text(text = "Ciśnienie: ${"%.2f".format(currentPressure)} hPa", style = MaterialTheme.typography.titleLarge)

        Button(onClick = {
            coroutineScope.launch {
                // Zapisz pomiar do bazy danych
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentTime = sdf.format(Date())
                val newMeasurement = MeasurementEntity(
                    temperature = currentTemperature,
                    pressure = currentPressure,
                    timestamp = currentTime
                )
                withContext(Dispatchers.IO) {
                    db.measurementDao().insert(newMeasurement)
                    measurements = db.measurementDao().getAllMeasurements()
                }
            }
        }) {
            Text(text = "Zapisz")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(measurements.size) { index ->
                val measurement = measurements[index]
                Text(
                    text = "Temperatura: ${measurement.temperature}°C, Ciśnienie: ${measurement.pressure} hPa, Czas: ${measurement.timestamp}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}