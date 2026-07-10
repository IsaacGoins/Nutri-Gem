package com.example.calorietracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.calorietracker.data.local.WaterEntity
import com.example.calorietracker.ui.viewmodels.MainViewModel
import com.example.calorietracker.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WaterDaySum(val date: String, val timestamp: Long, val totalOz: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val waterIntake by viewModel.allWater.collectAsState()
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val groupedWater = waterIntake.groupBy { dateFormat.format(Date(it.timestamp)) }
    
    val daySums = groupedWater.map { (date, dayIntakes) ->
        WaterDaySum(
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
        var selectedTabIndex by remember { mutableStateOf(0) }
        com.example.calorietracker.ui.components.DayCarousel(
            days = daySums,
            getDate = { it.timestamp },
            modifier = Modifier.padding(paddingValues),
            emptyMessage = "No water data available."
        ) { currentDaySum ->
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.height(40.dp)) {
                    Tab(
                        selected = selectedTabIndex == 0, 
                        onClick = { selectedTabIndex = 0 }, 
                        text = { Text("Daily Water") },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Tab(
                        selected = selectedTabIndex == 1, 
                        onClick = { selectedTabIndex = 1 }, 
                        text = { Text("History Graph") },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (selectedTabIndex == 0) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        WaterBanner(water = currentDaySum.totalOz, backgroundColor = AppColors.getWaterBannerColor(viewModel), onClick = {})
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(240.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.getWaterBannerColor(viewModel))
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            WaterHistoryGraph(daySums = daySums, endingAt = currentDaySum.timestamp, viewModel = viewModel)
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
                
                val intakesForDay = groupedWater[currentDaySum.date] ?: emptyList()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                    SwipeToDismissBoxValue.StartToEnd -> AppColors.getSwipeEditBackgroundColor(viewModel)
                                    SwipeToDismissBoxValue.EndToStart -> AppColors.getSwipeDeleteBackgroundColor(viewModel)
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
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.getCardBackgroundColor(viewModel))
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

@Composable
fun WaterHistoryGraph(
    daySums: List<WaterDaySum>, 
    endingAt: Long,
    viewModel: MainViewModel
) {
    if (daySums.isEmpty()) return
    
    val allSorted = daySums.sortedBy { it.timestamp }
    val idx = allSorted.indexOfFirst { it.timestamp == endingAt }
    
    val filteredDays = if (idx != -1) {
        val start = maxOf(0, idx - 3)
        val end = minOf(allSorted.size, start + 7)
        val adjustedStart = maxOf(0, end - 7)
        allSorted.subList(adjustedStart, end)
    } else {
        allSorted.takeLast(7)
    }
    
    var selectedIndex by remember(endingAt, filteredDays) { 
        mutableStateOf<Int?>(null) 
    }
    
    val barColor = AppColors.getWaterGraphBarColor(viewModel)
    val bannerColor = AppColors.getWaterBannerColor(viewModel)
    val axisTextColor = androidx.compose.material3.contentColorFor(bannerColor).toArgb()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            val canvasSize = this.size
            detectTapGestures { tapOffset ->
                val barWidth = canvasSize.width.toFloat() / (filteredDays.size * 2f).coerceAtLeast(1f)
                
                var closestIdx = -1
                var minDistance = Float.MAX_VALUE
                
                filteredDays.forEachIndexed { index, _ ->
                    val xOffset = index * (barWidth * 2) + barWidth / 2f + barWidth / 2f
                    val dist = kotlin.math.abs(tapOffset.x - xOffset)
                    if (dist < minDistance && dist < barWidth * 2) {
                        minDistance = dist
                        closestIdx = index
                    }
                }
                selectedIndex = if (closestIdx != -1) closestIdx else null
            }
        }) {
            val maxWater = filteredDays.maxOfOrNull { it.totalOz }?.toFloat()?.coerceAtLeast(1f) ?: 1f
            val barWidth = size.width / (filteredDays.size * 2f).coerceAtLeast(1f)
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
                color = axisTextColor
                textSize = 30f
            }
            
            drawContext.canvas.nativeCanvas.drawText(maxWater.toInt().toString(), -40f, 0f, axisPaint)
            drawContext.canvas.nativeCanvas.drawText("0", -40f, size.height, axisPaint)

            filteredDays.forEachIndexed { index, day ->
                val barHeight = (day.totalOz / maxWater) * maxBarHeight
                val xOffset = index * (barWidth * 2) + barWidth / 2f
                val yOffset = size.height - barHeight

                val shortDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(day.timestamp))
                drawContext.canvas.nativeCanvas.drawText(shortDate, xOffset - 10f, size.height + 40f, axisPaint)

                drawRoundRect(
                    color = if (selectedIndex == index) Color.White else barColor,
                    topLeft = Offset(xOffset, yOffset),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }
        
        selectedIndex?.let { index ->
            val day = filteredDays[index]
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${day.totalOz} oz",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
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
    val context = LocalContext.current

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
                    val localCalendar = java.util.Calendar.getInstance()
                    val selectedUtc = datePickerState.selectedDateMillis
                    if (selectedUtc != null) {
                        val utcCalendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        utcCalendar.timeInMillis = selectedUtc
                        localCalendar.set(
                            utcCalendar.get(java.util.Calendar.YEAR),
                            utcCalendar.get(java.util.Calendar.MONTH),
                            utcCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                    localCalendar.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                    localCalendar.set(java.util.Calendar.MINUTE, timePickerState.minute)
                    editedTimestamp = localCalendar.timeInMillis
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
                    Toast.makeText(context, "Water intake has been saved", Toast.LENGTH_SHORT).show()
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
