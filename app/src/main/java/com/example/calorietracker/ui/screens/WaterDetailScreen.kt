package com.example.calorietracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                    waterToDelete = water
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
                                    SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.error
                                    else -> Color.Transparent
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
                                    if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onError)
                                    }
                                }
                            }
                        ) {
                            Card(modifier = Modifier.fillMaxWidth()) {
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
