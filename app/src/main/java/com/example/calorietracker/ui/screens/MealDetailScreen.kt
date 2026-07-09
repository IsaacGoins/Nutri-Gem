package com.example.calorietracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.calorietracker.data.local.MealEntity
import com.example.calorietracker.data.network.GeminiItem
import com.example.calorietracker.ui.viewmodels.MainViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(viewModel: MainViewModel, onBack: () -> Unit, onEditingChange: (Boolean) -> Unit = {}) {
    val meals by viewModel.allMeals.collectAsState()
    
    // Group by day
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val groupedMeals = meals.groupBy { dateFormat.format(Date(it.timestamp)) }
    
    data class DaySum(val date: String, val timestamp: Long, val kcals: Int, val protein: Int, val carbs: Int, val fat: Int)
    
    val daySums = groupedMeals.map { (date, dayMeals) ->
        DaySum(
            date = date,
            timestamp = dayMeals.first().timestamp,
            kcals = dayMeals.sumOf { it.calories },
            protein = dayMeals.sumOf { it.proteinG },
            carbs = dayMeals.sumOf { it.carbsG },
            fat = dayMeals.sumOf { it.fatG }
        )
    }.sortedBy { it.timestamp }

    var selectedMetric by remember { mutableStateOf("Kcal") }
    var mealToEdit by remember { mutableStateOf<MealEntity?>(null) }
    
    LaunchedEffect(mealToEdit) {
        onEditingChange(mealToEdit != null)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Meal History & Analytics") },
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Kcal", "Protein", "Carbs", "Fat").forEach { metric ->
                        FilterChip(
                            selected = selectedMetric == metric,
                            onClick = { selectedMetric = metric },
                            label = { Text(metric) }
                        )
                    }
                }

            val primaryColor = MaterialTheme.colorScheme.primary
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(240.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                if (daySums.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("No meal data yet", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(top = 24.dp, bottom = 48.dp, start = 48.dp, end = 24.dp)) {
                        val maxVal = daySums.maxOfOrNull { 
                            when(selectedMetric) {
                                "Kcal" -> it.kcals
                                "Protein" -> it.protein
                                "Carbs" -> it.carbs
                                else -> it.fat
                            }
                        }?.toFloat()?.coerceAtLeast(1f) ?: 1f
                        
                        val xStep = if (daySums.size > 1) size.width / (daySums.size - 1) else size.width / 2f
                        
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
                        
                        drawContext.canvas.nativeCanvas.drawText(maxVal.toInt().toString(), -40f, 0f, axisPaint)
                        drawContext.canvas.nativeCanvas.drawText("0", -40f, size.height, axisPaint)
                        
                        val path = Path()
                        val lineColor = when(selectedMetric) {
                            "Kcal" -> primaryColor
                            "Protein" -> Color(0xFFEF5350) 
                            "Carbs" -> Color(0xFF66BB6A) 
                            else -> Color(0xFF42A5F5) 
                        }

                        daySums.forEachIndexed { index, day ->
                            val x = if (daySums.size == 1) size.width / 2f else index * xStep
                            val v = when(selectedMetric) {
                                "Kcal" -> day.kcals
                                "Protein" -> day.protein
                                "Carbs" -> day.carbs
                                else -> day.fat
                            }
                            val y = size.height - ((v / maxVal) * size.height)
                            
                            val shortDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(day.timestamp))
                            drawContext.canvas.nativeCanvas.drawText(shortDate, x - 30f, size.height + 40f, axisPaint)
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        if (daySums.size > 1) {
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        
                        daySums.forEachIndexed { index, day ->
                            val x = if (daySums.size == 1) size.width / 2f else index * xStep
                            val v = when(selectedMetric) {
                                "Kcal" -> day.kcals
                                "Protein" -> day.protein
                                "Carbs" -> day.carbs
                                else -> day.fat
                            }
                            val y = size.height - ((v / maxVal) * size.height)
                            drawCircle(color = Color.White, radius = 16f, center = Offset(x, y))
                            drawCircle(color = lineColor, radius = 10f, center = Offset(x, y))
                        }
                    }
                }
            }
            Text(
                "Meal Log",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            var mealToDelete by remember { mutableStateOf<MealEntity?>(null) }
            
            if (mealToDelete != null) {
                AlertDialog(
                    onDismissRequest = { mealToDelete = null },
                    title = { Text("Delete Meal") },
                    text = { Text("Are you sure you want to delete ${mealToDelete?.name}?") },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.deleteMeal(mealToDelete!!)
                            mealToDelete = null 
                        }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mealToDelete = null }) { Text("Cancel") }
                    }
                )
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedMeals.forEach { (date, mealsForDay) ->
                    item {
                        Text(date, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(mealsForDay, key = { it.id }) { meal ->
                        key(meal) {
                            var expanded by remember { mutableStateOf(false) }
                        
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    mealToDelete = meal
                                    false
                                } else if (it == SwipeToDismissBoxValue.StartToEnd) {
                                    mealToEdit = meal
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
                            Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(meal.name, style = MaterialTheme.typography.titleMedium)
                                        Text(timeFormat.format(Date(meal.timestamp)), style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("${meal.calories} kcal | ${meal.proteinG}g P | ${meal.carbsG}g C | ${meal.fatG}g F")
                                        
                                        AnimatedVisibility(visible = expanded) {
                                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                                HorizontalDivider()
                                                val itemsList = try {
                                                    Json.decodeFromString<List<GeminiItem>>(meal.itemsJson)
                                                } catch(e: Exception) { emptyList() }
                                                
                                                if (itemsList.isEmpty()) {
                                                    Text("No breakdown available.", style = MaterialTheme.typography.bodySmall)
                                                } else {
                                                    itemsList.forEach { item ->
                                                        Text("- ${item.name}: ${item.calories} kcal, ${item.protein_g}g P, ${item.carbs_g}g C, ${item.fat_g}g F", style = MaterialTheme.typography.bodySmall)
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
                }
            }
        }
    } // closes Scaffold
        
    AnimatedVisibility(
            visible = mealToEdit != null,
            modifier = Modifier.fillMaxSize()
        ) {
            if (mealToEdit != null) {
                EditMealScreen(
                    meal = mealToEdit!!,
                    viewModel = viewModel,
                    onDismiss = { mealToEdit = null },
                    onSave = { updatedMeal ->
                        viewModel.updateMeal(updatedMeal)
                        mealToEdit = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMealScreen(meal: MealEntity, viewModel: MainViewModel, onDismiss: () -> Unit, onSave: (MealEntity) -> Unit) {
    BackHandler { onDismiss() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusManager = LocalFocusManager.current

    val itemsList = try {
        Json.decodeFromString<List<GeminiItem>>(meal.itemsJson)
    } catch(e: Exception) { emptyList() }
    
    var editedMealName by remember(meal) { mutableStateOf(meal.name) }
    var editedTimestamp by remember(meal) { mutableStateOf(meal.timestamp) }
    val editedItems = remember(meal) { mutableStateListOf(*itemsList.map { it.copy() }.toTypedArray()) }
    
    val scope = rememberCoroutineScope()
    var isAnalyzing by remember(meal) { mutableStateOf(false) }
    
    val aiItems = remember(meal) { mutableStateListOf(MealItemInput()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Meal") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val updatedMeal = meal.copy(
                            name = editedMealName,
                            timestamp = editedTimestamp,
                            calories = editedItems.sumOf { it.calories },
                            proteinG = editedItems.sumOf { it.protein_g },
                            carbsG = editedItems.sumOf { it.carbs_g },
                            fatG = editedItems.sumOf { it.fat_g },
                            itemsJson = Json.encodeToString(editedItems.toList())
                        )
                        onSave(updatedMeal)
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = editedMealName,
                    onValueChange = { editedMealName = it },
                    label = { Text("Meal Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

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

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Time: " + SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(editedTimestamp)))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            itemsIndexed(editedItems, key = { _, item -> item.id }) { index, item ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            editedItems.removeAt(index)
                            false
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, CardDefaults.shape)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = { editedItems[index] = item.copy(name = it) },
                                    label = { Text("Name") },
                                    modifier = Modifier.weight(1.5f)
                                )
                                if (item.isFdaVerified) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "FDA Verified",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text("FDA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                OutlinedTextField(
                                    value = if (item.calories == 0) "" else item.calories.toString(),
                                    onValueChange = { editedItems[index] = item.copy(calories = it.toIntOrNull() ?: 0) },
                                    label = { Text("Kcal") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (item.protein_g == 0) "" else item.protein_g.toString(),
                                    onValueChange = { editedItems[index] = item.copy(protein_g = it.toIntOrNull() ?: 0) },
                                    label = { Text("Pro(g)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = if (item.carbs_g == 0) "" else item.carbs_g.toString(),
                                    onValueChange = { editedItems[index] = item.copy(carbs_g = it.toIntOrNull() ?: 0) },
                                    label = { Text("Carb(g)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = if (item.fat_g == 0) "" else item.fat_g.toString(),
                                    onValueChange = { editedItems[index] = item.copy(fat_g = it.toIntOrNull() ?: 0) },
                                    label = { Text("Fat(g)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                TextButton(onClick = { editedItems.add(GeminiItem("", 0)) }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add Blank Item")
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Add Items via AI", style = MaterialTheme.typography.titleMedium)
            }

            itemsIndexed(aiItems, key = { _, item -> item.id }) { index, item ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            if (aiItems.size > 1) {
                                aiItems.removeAt(index)
                            }
                            false
                        } else {
                            false
                        }
                    }
                )
                
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, RoundedCornerShape(8.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { aiItems[index] = item.copy(name = it) },
                            label = { Text("Item") },
                            modifier = Modifier.weight(2f)
                        )
                        OutlinedTextField(
                            value = item.quantity,
                            onValueChange = { aiItems[index] = item.copy(quantity = it) },
                            label = { Text("Qty") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        var expanded by remember { mutableStateOf(false) }
                        val unitOptions = listOf("g", "oz", "cups", "tbsp", "tsp", "ml", "piece")

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            OutlinedTextField(
                                value = item.unit,
                                onValueChange = { aiItems[index] = item.copy(unit = it) },
                                label = { Text("Unit") },
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                unitOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            aiItems[index] = item.copy(unit = option)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                TextButton(onClick = { aiItems.add(MealItemInput()) }) {
                    Text("+ Add Another")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isAnalyzing = true
                        val prompt = "Calculate macros for the following items. If unsure, provide your best estimation without asking for clarification. Respond with a valid JSON having status='success':\n" +
                            aiItems.filter { it.name.isNotBlank() }.joinToString("\n") { "${it.quantity.ifBlank { "1" }} ${it.unit} of ${it.name}" }
                        scope.launch {
                            val newItems = viewModel.analyzeSingleItem(prompt)
                            if (newItems.isNotEmpty()) {
                                editedItems.addAll(newItems)
                                aiItems.clear()
                                aiItems.add(MealItemInput())
                            } else {
                                android.widget.Toast.makeText(context, "Failed to analyze items. Please try again.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            isAnalyzing = false
                        }
                    },
                    enabled = !isAnalyzing && aiItems.any { it.name.isNotBlank() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) {
                        Text("Analyzing with AI...")
                    } else {
                        Text("Calculate & Add")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
