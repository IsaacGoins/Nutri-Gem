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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val scores by viewModel.allScores.collectAsState()
    val isScoring by viewModel.isScoring.collectAsState()

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
                    IconButton(onClick = { viewModel.generateScoreForYesterday(force = true) }) {
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
        } else if (scores.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No scores available. Ensure you logged food/water yesterday.", modifier = Modifier.padding(16.dp))
            }
        } else {
            val latestScore = scores.first()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("Score Breakdown", style = MaterialTheme.typography.titleLarge)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScoreRingCard(title = "Clean Diet", score = latestScore.cleanScore, modifier = Modifier.weight(1f))
                    ScoreRingCard(title = "Macros", score = latestScore.macroScore, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScoreRingCard(title = "Calories", score = latestScore.calorieScore, modifier = Modifier.weight(1f))
                    ScoreRingCard(title = "Hydration", score = latestScore.waterScore, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScoreRingCard(title = "Meal Balance", score = latestScore.balanceScore, modifier = Modifier.weight(1f))
                    ScoreRingCard(title = "Overall Score", score = latestScore.overallScore, modifier = Modifier.weight(1f), isOverall = true)
                }

                // AI Feedback
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(latestScore.feedback, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // History Graph
                Text("Score History", style = MaterialTheme.typography.titleLarge)
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        ScoreHistoryGraph(scores = scores)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ScoreRingCard(title: String, score: Int, modifier: Modifier = Modifier, isOverall: Boolean = false) {
    val containerColor = if (isOverall) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOverall) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
fun ScoreHistoryGraph(scores: List<com.example.calorietracker.data.local.DailyScoreEntity>) {
    if (scores.isEmpty()) return
    val sortedScores = scores.sortedBy { it.dateTimestamp }.takeLast(7)
    val maxScore = 100f
    
    val lineColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = Modifier.fillMaxSize()) {
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
        
        // Y-axis grid & labels
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
        
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 8f,
                center = point
            )
        }
    }
}
