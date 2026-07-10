package com.example.calorietracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorWeight
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.calorietracker.data.local.WeightEntity
import com.example.calorietracker.ui.viewmodels.MainViewModel
import com.example.calorietracker.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val weightLog by viewModel.allWeight.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Group by day
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val groupedWeight = weightLog.groupBy { dateFormat.format(Date(it.timestamp)) }
    
    data class DaySum(val date: String, val timestamp: Long, val weightLbs: Float)
    
    // For weight we probably just want the last recorded weight for the day, or average
    val daySums = groupedWeight.map { (date, dayLogs) ->
        DaySum(
            date = date,
            timestamp = dayLogs.first().timestamp,
            weightLbs = dayLogs.first().weightLbs // Last logged
        )
    }.sortedBy { it.timestamp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight History & Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val fabColor = AppColors.getPrimaryButtonColor(viewModel)
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = fabColor,
                contentColor = contentColorFor(fabColor)
            ) {
                Text("Log Weight")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val weightGoal by viewModel.weightGoal.collectAsState()
            var showGoalDialog by remember { mutableStateOf(false) }

            if (weightGoal == 0f) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { showGoalDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text("You haven't set a weight goal yet! Tap here to set one.", modifier = Modifier.padding(16.dp))
                }
            } else {
                val currentWeight = daySums.lastOrNull()?.weightLbs ?: 0f
                if (currentWeight > 0f) {
                    val diff = currentWeight - weightGoal
                    val diffText = if (diff > 0) "${String.format("%.1f", diff)} lbs to go!" else if (diff < 0) "Goal passed by ${String.format("%.1f", -diff)} lbs!" else "Goal reached!"
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { showGoalDialog = true },
                        colors = CardDefaults.cardColors(containerColor = AppColors.getCardBackgroundColor(viewModel))
                    ) {
                        Text("Goal: $weightGoal lbs | Current: $currentWeight lbs\n$diffText", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(240.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.getWeightBannerColor(viewModel),
                    contentColor = contentColorFor(AppColors.getWeightBannerColor(viewModel))
                )
            ) {
                if (daySums.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("No weight data yet", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    val lineColor = MaterialTheme.colorScheme.onSecondary
                    val cardBgColor = MaterialTheme.colorScheme.secondary
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(top = 24.dp, bottom = 48.dp, start = 48.dp, end = 24.dp)) {
                        val allWeights = daySums.map { it.weightLbs }.toMutableList()
                        if (weightGoal > 0f) allWeights.add(weightGoal)
                        
                        val maxWeight = allWeights.maxOrNull()?.coerceAtLeast(100f) ?: 100f
                        val minWeight = (allWeights.minOrNull() ?: 0f) * 0.8f
                        val range = (maxWeight - minWeight).coerceAtLeast(10f)
                        val barWidth = size.width / (daySums.size * 2f).coerceAtLeast(1f)
                        val maxBarHeight = size.height
                        
                        // Grid lines
                        val gridLinesCount = 4
                        for (i in 0..gridLinesCount) {
                            val yLine = size.height - (i * size.height / gridLinesCount)
                            drawLine(
                                color = lineColor.copy(alpha = 0.3f),
                                start = Offset(0f, yLine),
                                end = Offset(size.width, yLine),
                                strokeWidth = 2f
                            )
                        }

                        val axisPaint = android.graphics.Paint().apply {
                            color = lineColor.copy(alpha = 0.7f).toArgb()
                            textSize = 30f
                        }
                        
                        // Y axis labels
                        drawContext.canvas.nativeCanvas.drawText(maxWeight.toInt().toString(), -40f, 0f, axisPaint)
                        drawContext.canvas.nativeCanvas.drawText(minWeight.toInt().toString(), -40f, size.height, axisPaint)

                        if (weightGoal > 0f) {
                            val goalY = size.height - (((weightGoal - minWeight) / range) * maxBarHeight)
                            drawLine(
                                color = lineColor.copy(alpha = 0.8f),
                                start = Offset(0f, goalY),
                                end = Offset(size.width, goalY),
                                strokeWidth = 4f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                            )
                            drawContext.canvas.nativeCanvas.drawText("Goal", size.width - 60f, goalY - 10f, axisPaint)
                        }

                        val path = androidx.compose.ui.graphics.Path()

                        daySums.forEachIndexed { index, day ->
                            val valueHeight = ((day.weightLbs - minWeight) / range) * maxBarHeight
                            val xOffset = index * (barWidth * 2) + barWidth / 2f
                            val yOffset = size.height - valueHeight

                            // X axis labels
                            val shortDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(day.timestamp))
                            drawContext.canvas.nativeCanvas.drawText(shortDate, xOffset - 10f, size.height + 40f, axisPaint)

                            if (index == 0) {
                                path.moveTo(xOffset, yOffset)
                            } else {
                                path.lineTo(xOffset, yOffset)
                            }
                        }

                        if (daySums.size > 1) {
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                            )
                        }

                        daySums.forEachIndexed { index, day ->
                            val valueHeight = ((day.weightLbs - minWeight) / range) * maxBarHeight
                            val xOffset = index * (barWidth * 2) + barWidth / 2f
                            val yOffset = size.height - valueHeight

                            drawCircle(color = cardBgColor, radius = 16f, center = Offset(xOffset, yOffset))
                            drawCircle(color = lineColor, radius = 10f, center = Offset(xOffset, yOffset))
                        }
                    }
                }
            }
            Text(
                "Weight Log",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            var weightToDelete by remember { mutableStateOf<WeightEntity?>(null) }
            
            if (weightToDelete != null) {
                AlertDialog(
                    onDismissRequest = { weightToDelete = null },
                    title = { Text("Delete Entry") },
                    text = { Text("Are you sure you want to delete this ${weightToDelete?.weightLbs} lbs entry?") },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.deleteWeight(weightToDelete!!)
                            weightToDelete = null 
                        }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { weightToDelete = null }) { Text("Cancel") }
                    }
                )
            }
            
            if (showGoalDialog) {
                var goalInput by remember { mutableStateOf(if (weightGoal > 0f) weightGoal.toString() else "") }
                AlertDialog(
                    onDismissRequest = { showGoalDialog = false },
                    title = { Text("Set Weight Goal") },
                    text = {
                        OutlinedTextField(
                            value = goalInput,
                            onValueChange = { goalInput = it },
                            label = { Text("Goal (lbs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            goalInput.toFloatOrNull()?.let {
                                viewModel.saveWeightGoal(it)
                                Toast.makeText(context, "Weight goal has been saved", Toast.LENGTH_SHORT).show()
                            }
                            showGoalDialog = false
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
                    }
                )
            }

            if (showAddDialog) {
                var weightInput by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Log Weight") },
                    text = {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (lbs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val lbs = weightInput.toFloatOrNull()
                            if (lbs != null && lbs > 0) {
                                viewModel.addWeight(lbs)
                                showAddDialog = false
                            }
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedWeight.forEach { (date, logsForDay) ->
                    item {
                        Text(date, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(logsForDay, key = { it.id }) { weight ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                    weightToDelete = weight
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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.getCardBackgroundColor(viewModel))
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${weight.weightLbs} lbs", style = MaterialTheme.typography.titleMedium)
                                        Text(timeFormat.format(Date(weight.timestamp)), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Icon(Icons.Default.MonitorWeight, contentDescription = "Weight", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
