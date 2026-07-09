package com.example.calorietracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calorietracker.data.local.DailyScoreEntity
import com.example.calorietracker.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val scores by viewModel.allScores.collectAsState()
    val isScoring by viewModel.isScoring.collectAsState()
    var showRegenerateDialog by remember { mutableStateOf(false) }

    if (showRegenerateDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateDialog = false },
            title = { Text("Regenerate Score?") },
            text = { Text("This will recalculate your score based on your current logs. Are you sure?") },
            confirmButton = {
                TextButton(onClick = {
                    showRegenerateDialog = false
                    viewModel.generateScoreForYesterday(force = true)
                }) {
                    Text("Regenerate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily AI Score") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRegenerateDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isScoring) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is analyzing yesterday's logs...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            var selectedTabIndex by remember { mutableStateOf(0) }
            com.example.calorietracker.ui.components.DayCarousel(
                days = scores,
                getDate = { it.dateTimestamp },
                modifier = Modifier.padding(paddingValues),
                emptyMessage = "No scores available. Ensure you logged food/water yesterday."
            ) { currentScore ->
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.height(48.dp)) {
                        Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Daily Score") })
                        Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("History Graph") })
                    }
                    
                    if (selectedTabIndex == 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Text("Score Breakdown", style = MaterialTheme.typography.titleLarge)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                ScoreRingCard(title = "Overall Score", score = currentScore.overallScore, modifier = Modifier.weight(1f), isOverall = true)
                                ScoreRingCard(title = "Clean Diet", score = currentScore.cleanScore, modifier = Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                ScoreRingCard(title = "Macros", score = currentScore.macroScore, modifier = Modifier.weight(1f))
                                ScoreRingCard(title = "Calories", score = currentScore.calorieScore, modifier = Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                ScoreRingCard(title = "Hydration", score = currentScore.waterScore, modifier = Modifier.weight(1f))
                                ScoreRingCard(title = "Meal Balance", score = currentScore.balanceScore, modifier = Modifier.weight(1f))
                            }

                            // AI Feedback
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("AI Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(currentScore.feedback, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Score History", style = MaterialTheme.typography.titleLarge)
                            Card(
                                modifier = Modifier.fillMaxWidth().height(250.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    ScoreHistoryGraph(scores = scores, endingAt = currentScore.dateTimestamp)
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
fun ScoreRingCard(title: String, score: Int, modifier: Modifier = Modifier, isOverall: Boolean = false) {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderStroke = if (isOverall) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                val primaryColor = if (isOverall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                val trackColor = primaryColor.copy(alpha = 0.2f)
                
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                    if (score >= 0) {
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = (score / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 12f, cap = StrokeCap.Round)
                        )
                    }
                }
                Text(
                    text = if (score >= 0) "$score" else "X",
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
}
}

@Composable
fun ScoreHistoryGraph(scores: List<com.example.calorietracker.data.local.DailyScoreEntity>, endingAt: Long? = null) {
    if (scores.isEmpty()) return
    
    val allSorted = scores.sortedBy { it.dateTimestamp }
    val idx = if (endingAt != null) allSorted.indexOfFirst { it.dateTimestamp == endingAt } else -1
    
    val sortedScores = if (idx != -1) {
        val start = maxOf(0, idx - 3)
        val end = minOf(allSorted.size, start + 7)
        val adjustedStart = maxOf(0, end - 7)
        allSorted.subList(adjustedStart, end)
    } else {
        allSorted.takeLast(7)
    }
    
    val maxScore = 100f
    val lineColor = MaterialTheme.colorScheme.tertiary
    var selectedIndex by remember(endingAt, sortedScores) { 
        val initialSelection = if (endingAt != null) {
            val i = sortedScores.indexOfFirst { it.dateTimestamp == endingAt }
            if (i != -1) i else null
        } else null
        mutableStateOf<Int?>(initialSelection) 
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures { tapOffset ->
                val padding = 20f
                val startX = padding + 60f 
                val graphWidth = size.width - padding - startX
                val pointSpacing = if (sortedScores.size > 1) graphWidth / (sortedScores.size - 1) else graphWidth / 2
                
                var closestIdx = -1
                var minDistance = Float.MAX_VALUE
                
                sortedScores.forEachIndexed { index, _ ->
                    val x = startX + (if (sortedScores.size > 1) index * pointSpacing else graphWidth / 2)
                    val dist = kotlin.math.abs(tapOffset.x - x)
                    if (dist < minDistance && dist < 100f) {
                        minDistance = dist
                        closestIdx = index
                    }
                }
                selectedIndex = if (closestIdx != -1) closestIdx else null
            }
        }) {
            val padding = 20f
            val bottomPadding = 60f
            val topPadding = 40f
            
            val width = size.width - padding * 2
            val height = size.height - topPadding - bottomPadding
            
            val startX = padding + 60f 
            val startY = topPadding
            
            val gridColor = Color.Gray.copy(alpha = 0.3f)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 35f
            }
            
            drawLine(gridColor, start = Offset(startX, startY), end = Offset(size.width - padding, startY))
            drawContext.canvas.nativeCanvas.drawText("100", padding, startY + 10f, textPaint)
            
            drawLine(gridColor, start = Offset(startX, startY + height / 2), end = Offset(size.width - padding, startY + height / 2))
            drawContext.canvas.nativeCanvas.drawText("50", padding, startY + height / 2 + 10f, textPaint)
            
            drawLine(gridColor, start = Offset(startX, startY + height), end = Offset(size.width - padding, startY + height))
            drawContext.canvas.nativeCanvas.drawText("0", padding, startY + height + 10f, textPaint)
            
            val graphWidth = size.width - padding - startX
            val pointSpacing = if (sortedScores.size > 1) graphWidth / (sortedScores.size - 1) else graphWidth / 2
            
            val points = sortedScores.mapIndexed { index, score ->
                val x = startX + (if (sortedScores.size > 1) index * pointSpacing else graphWidth / 2)
                val y = startY + height - ((score.overallScore / maxScore) * height)
                Offset(x, y)
            }
            
            val timeFormat = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
            points.forEachIndexed { index, point ->
                val dateText = timeFormat.format(java.util.Date(sortedScores[index].dateTimestamp))
                drawContext.canvas.nativeCanvas.drawText(dateText, point.x - 30f, startY + height + 50f, textPaint)
            }
            
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            
            points.forEachIndexed { index, point ->
                drawCircle(
                    color = if (selectedIndex == index) Color.White else lineColor,
                    radius = if (selectedIndex == index) 12f else 8f,
                    center = point
                )
            }
        }
        
        selectedIndex?.let { index ->
            val score = sortedScores[index]
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Score: ${score.overallScore}",
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
