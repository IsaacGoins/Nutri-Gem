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
                // Main Score Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Overall Score", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${latestScore.overallScore}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text("/ 100", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
                    }
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

                // Breakdown
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
                    Spacer(modifier = Modifier.weight(1f)) // Empty spacer for grid alignment
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
fun ScoreRingCard(title: String, score: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                val primaryColor = MaterialTheme.colorScheme.tertiary
                val trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = (score / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                }
                Text("$score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScoreHistoryGraph(scores: List<com.example.calorietracker.data.local.DailyScoreEntity>) {
    if (scores.isEmpty()) return
    val sortedScores = scores.sortedBy { it.dateTimestamp }
    val maxScore = 100f
    
    val lineColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val width = size.width
        val height = size.height
        
        val pointSpacing = if (sortedScores.size > 1) width / (sortedScores.size - 1) else width
        
        val points = sortedScores.mapIndexed { index, score ->
            val x = index * pointSpacing
            val y = height - ((score.overallScore / maxScore) * height)
            Offset(x, y)
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
