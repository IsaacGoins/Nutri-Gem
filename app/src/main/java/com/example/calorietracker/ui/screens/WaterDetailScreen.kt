package com.example.calorietracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.calorietracker.data.local.WaterEntity
import com.example.calorietracker.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val waterIntake by viewModel.allWater.collectAsState()
    
    // Group by day
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val groupedWater = waterIntake.groupBy { dateFormat.format(Date(it.timestamp)) }
    
    data class DaySum(val date: String, val timestamp: Long, val totalOz: Int)
    
    val daySums = groupedWater.map { (date, dayIntakes) ->
        DaySum(
            date = date,
            timestamp = dayIntakes.first().timestamp,
            totalOz = dayIntakes.sumOf { it.amountOz }
        )
    }.sortedBy { it.timestamp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water History & Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(240.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                if (daySums.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("No water data yet", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(top = 24.dp, bottom = 48.dp, start = 48.dp, end = 24.dp)) {
                        val maxWater = daySums.maxOfOrNull { it.totalOz }?.toFloat()?.coerceAtLeast(1f) ?: 1f
                        val barWidth = size.width / (daySums.size * 2f).coerceAtLeast(1f)
                        val maxBarHeight = size.height
                        
                        // Grid lines
                        val gridLinesCount = 4
                        for (i in 0..gridLinesCount) {
                            val yLine = size.height - (i * size.height / gridLinesCount)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                start = Offset(0f, yLine),
                                end = Offset(size.width, yLine),
                                strokeWidth = 2f
                            )
                        }

                        val axisPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                        }
                        
                        // Y axis labels
                        drawContext.canvas.nativeCanvas.drawText(maxWater.toInt().toString(), -40f, 0f, axisPaint)
                        drawContext.canvas.nativeCanvas.drawText("0", -40f, size.height, axisPaint)

                        daySums.forEachIndexed { index, day ->
                            val barHeight = (day.totalOz / maxWater) * maxBarHeight
                            val xOffset = index * (barWidth * 2) + barWidth / 2f
                            val yOffset = size.height - barHeight

                            // X axis labels
                            val shortDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(day.timestamp))
                            drawContext.canvas.nativeCanvas.drawText(shortDate, xOffset - 10f, size.height + 40f, axisPaint)

                            drawRoundRect(
                                color = Color(0xFF00BCD4),
                                topLeft = Offset(xOffset, yOffset),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }
                    }
                }
            }
            Text(
                "Water Log",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            var waterToDelete by remember { mutableStateOf<WaterEntity?>(null) }
            
            if (waterToDelete != null) {
                AlertDialog(
                    onDismissRequest = { waterToDelete = null },
                    title = { Text("Delete Entry") },
                    text = { Text("Are you sure you want to delete this ${waterToDelete?.amountOz} oz entry?") },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.deleteWater(waterToDelete!!)
                            waterToDelete = null 
                        }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { waterToDelete = null }) { Text("Cancel") }
                    }
                )
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedWater.forEach { (date, intakesForDay) ->
                    item {
                        Text(date, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(intakesForDay, key = { it.id }) { water ->
                        var waterToEdit by remember { mutableStateOf<WaterEntity?>(null) }
                        
                        if (waterToEdit != null) {
                            EditWaterDialog(
                                water = waterToEdit!!,
                                onDismiss = { waterToEdit = null },
                                onSave = { updatedWater ->
                                    viewModel.updateWater(updatedWater)
                                    waterToEdit = null
                                }
                            )
                        }

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    waterToDelete = water
                                    false
                                } else if (it == SwipeToDismissBoxValue.StartToEnd) {
                                    waterToEdit = water
                                    false
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                    else -> Color.Transparent
                                }
                                val icon = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                    else -> null
                                }
                                val alignment = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    else -> Alignment.Center
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, CardDefaults.shape)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    if (icon != null) {
                                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
                                    }
                                }
                            }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { waterToEdit = water }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${water.amountOz} oz", style = MaterialTheme.typography.titleMedium)
                                        Text(timeFormat.format(Date(water.timestamp)), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWaterDialog(water: WaterEntity, onDismiss: () -> Unit, onSave: (WaterEntity) -> Unit) {
    var amountText by remember { mutableStateOf(water.amountOz.toString()) }
    var editedTimestamp by remember { mutableStateOf(water.timestamp) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = editedTimestamp)
    val timePickerState = rememberTimePickerState(
        initialHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(editedTimestamp)).toInt(),
        initialMinute = SimpleDateFormat("mm", Locale.getDefault()).format(Date(editedTimestamp)).toInt()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = datePickerState.selectedDateMillis ?: editedTimestamp
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                    calendar.set(java.util.Calendar.MINUTE, timePickerState.minute)
                    editedTimestamp = calendar.timeInMillis
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Water") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (oz)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Time: " + SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(editedTimestamp)))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toIntOrNull()
                if (amount != null && amount > 0) {
                    onSave(water.copy(amountOz = amount, timestamp = editedTimestamp))
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
