package com.example.howlongsince

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext

import com.example.howlongsince.ui.theme.HowLongSinceTheme

data class Counter(
    val title: String,
    val startDate: LocalDateTime
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HowLongSinceTheme {
                val navController = rememberNavController()
                val counterViewModel: CounterViewModel = viewModel()
                NavHost(
                    navController = navController,
                    startDestination = "counterScreen"
                ) {
                    composable("counterScreen") {
                        CounterScreen(
                            viewModel = counterViewModel,
                            onAddCounterClick = { navController.navigate("addCounterScreen") }
                        )
                    }
                    composable("addCounterScreen") {
                        AddCounterScreen(onSave = { title, startDate ->
                            // Add the new counter to the ViewModel
                            counterViewModel.addCounter(Counter(title, startDate))
                            navController.popBackStack()
                        }, onCancel = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(viewModel: CounterViewModel, onAddCounterClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How Long Since") },
                actions = {
                    IconButton(onClick = onAddCounterClick) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Counter")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(viewModel.counters) { counter ->
                CounterCard(counter.title, counter.startDate)
            }
        }

    }
}

@Composable
fun CounterCard(title: String, startDate: LocalDateTime) {
    val (roundedTime, exactTime) = calculateTimeSince(startDate)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.LightGray,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = roundedTime, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = exactTime,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun AddCounterScreen(onSave: (String, LocalDateTime) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var context = LocalContext.current

    // Format for displaying date and time
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dateTime = dateTime.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth)
        },
        dateTime.year,
        dateTime.monthValue - 1,
        dateTime.dayOfMonth
    )

    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            dateTime = dateTime.withHour(hour).withMinute(minute)
        },
        dateTime.hour,
        dateTime.minute,
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add New Counter", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        // Date Button
        Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Select Date: ${dateTime.format(dateFormatter)}")
        }

        // Time Button
        Button(onClick = { timePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Select Time: ${dateTime.format(timeFormatter)}")
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { onCancel() }) {
                Text("Cancel")
            }
            Button(onClick = { onSave(title, dateTime) }) {
                Text("Save")
            }
        }
    }
}

class CounterViewModel : ViewModel() {

    // A list that holds all counters
    private val _counters = mutableStateListOf<Counter>()
    val counters: SnapshotStateList<Counter> = _counters

    // Function to add a new counter
    fun addCounter(counter: Counter) {
        _counters.add(counter)
    }
}

fun calculateTimeSince(startDate: LocalDateTime): Pair<String, String> {
    val now = LocalDateTime.now()
    val duration = Duration.between(startDate, now)

    val years = duration.toDays() / 365
    val months = (duration.toDays() % 365) / 30
    val days = duration.toDays() % 30
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60

    // Rounded time (e.g., "4 months")
    val roundedTime = when {
        years > 0 -> "$years years"
        months > 0 -> "$months months"
        days > 0 -> "$days days"
        else -> "Just now"
    }

    // Exact time (e.g., "147d 8h 11m 22s")
    val exactTime = "${years}y ${months}m ${days}d ${hours}h ${minutes}m ${seconds}s"

    return Pair(roundedTime, exactTime)
}
