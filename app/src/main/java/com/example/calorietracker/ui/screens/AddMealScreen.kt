package com.example.calorietracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.GeminiState
import com.example.calorietracker.ui.viewmodels.MainViewModel

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

data class MealItemInput(
    var name: String = "",
    var quantity: String = "",
    var unit: String = "",
    var isManual: Boolean = false,
    var calories: String = "",
    var proteinG: String = "",
    var carbsG: String = "",
    var fatG: String = "",
    val id: String = java.util.UUID.randomUUID().toString()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val geminiState by viewModel.geminiState.collectAsState()
    val mealItems = remember { mutableStateListOf(MealItemInput()) }
    var clarificationAnswer by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Reset state when leaving
    DisposableEffect(Unit) {
        onDispose { viewModel.resetGeminiState() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Meal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }) {
            when (val state = geminiState) {
                is GeminiState.Idle, is GeminiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        if (state is GeminiState.Error) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(mealItems, key = { _, item -> item.id }) { index, item ->
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = {
                                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                                if (mealItems.size > 1) {
                                                    mealItems.removeAt(index)
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
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                            if (item.isManual) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = item.name,
                                                        onValueChange = { mealItems[index] = item.copy(name = it) },
                                                        label = { Text("Name") },
                                                        modifier = Modifier.weight(1.5f)
                                                    )
                                                    OutlinedTextField(
                                                        value = item.calories,
                                                        onValueChange = { mealItems[index] = item.copy(calories = it) },
                                                        label = { Text("Kcal") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = item.proteinG,
                                                        onValueChange = { mealItems[index] = item.copy(proteinG = it) },
                                                        label = { Text("Pro(g)") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = item.carbsG,
                                                        onValueChange = { mealItems[index] = item.copy(carbsG = it) },
                                                        label = { Text("Carb(g)") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = item.fatG,
                                                        onValueChange = { mealItems[index] = item.copy(fatG = it) },
                                                        label = { Text("Fat(g)") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = item.name,
                                                        onValueChange = { mealItems[index] = item.copy(name = it) },
                                                        label = { Text("Item") },
                                                        modifier = Modifier.weight(2f)
                                                    )
                                                    OutlinedTextField(
                                                        value = item.quantity,
                                                        onValueChange = { mealItems[index] = item.copy(quantity = it) },
                                                        label = { Text("Qty") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    var expanded by remember { mutableStateOf(false) }
                                                    val unitOptions = listOf("g", "oz", "cup", "tbsp", "tsp", "ml", "piece")

                                                    ExposedDropdownMenuBox(
                                                        expanded = expanded,
                                                        onExpandedChange = { expanded = !expanded },
                                                        modifier = Modifier.weight(1.5f)
                                                    ) {
                                                        OutlinedTextField(
                                                            value = item.unit,
                                                            onValueChange = { mealItems[index] = item.copy(unit = it) },
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
                                                                        mealItems[index] = item.copy(unit = option)
                                                                        expanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            }
                                        }
                                    }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    TextButton(onClick = { mealItems.add(MealItemInput()) }) {
                                        Text("+ Add Item")
                                    }
                                    TextButton(onClick = { mealItems.add(MealItemInput(isManual = true)) }) {
                                        Text("+ Add Manual Item")
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val aiItems = mealItems.filter { !it.isManual }
                                val manualItems = mealItems.filter { it.isManual }.map {
                                    com.example.calorietracker.data.network.GeminiItem(
                                        name = it.name.ifBlank { "Custom Item" },
                                        calories = it.calories.toIntOrNull() ?: 0,
                                        protein_g = it.proteinG.toIntOrNull() ?: 0,
                                        carbs_g = it.carbsG.toIntOrNull() ?: 0,
                                        fat_g = it.fatG.toIntOrNull() ?: 0
                                    )
                                }

                                if (aiItems.isEmpty() && manualItems.isNotEmpty()) {
                                    val data = com.example.calorietracker.data.network.GeminiData(
                                        meal_name = "Custom Meal",
                                        total_calories = manualItems.sumOf { it.calories },
                                        macros = com.example.calorietracker.data.network.GeminiMacros(
                                            protein_g = manualItems.sumOf { it.protein_g },
                                            carbs_g = manualItems.sumOf { it.carbs_g },
                                            fat_g = manualItems.sumOf { it.fat_g }
                                        ),
                                        items = manualItems
                                    )
                                    viewModel.setGeminiSuccessState(data)
                                } else if (aiItems.isNotEmpty()) {
                                    val prompt = "Calculate macros for the following meal:\n" +
                                            aiItems.joinToString("\n") { "${it.quantity} ${it.unit} of ${it.name}" }
                                    viewModel.analyzeMeal(prompt, manualItems, aiItems.map { it.name })
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calculate")
                        }
                    }
                }
                is GeminiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(), 
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Analyzing meal with AI...", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
                is GeminiState.NeedsClarification -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetGeminiState() },
                        title = { Text("Clarification Needed") },
                        text = {
                            Column {
                                Text(state.question)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = clarificationAnswer,
                                    onValueChange = { clarificationAnswer = it },
                                    label = { Text("Your answer") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val aiItems = mealItems.filter { !it.isManual }
                                val manualItems = mealItems.filter { it.isManual }.map {
                                    com.example.calorietracker.data.network.GeminiItem(
                                        name = it.name.ifBlank { "Custom Item" },
                                        calories = it.calories.toIntOrNull() ?: 0,
                                        protein_g = it.proteinG.toIntOrNull() ?: 0,
                                        carbs_g = it.carbsG.toIntOrNull() ?: 0,
                                        fat_g = it.fatG.toIntOrNull() ?: 0
                                    )
                                }
                                val prompt = "Calculate macros for the following meal:\n" +
                                        aiItems.joinToString("\n") { "${it.quantity} ${it.unit} of ${it.name}" } +
                                        "\nClarification: $clarificationAnswer"
                                viewModel.analyzeMeal(prompt, manualItems, aiItems.map { it.name })
                            }) {
                                Text("Submit")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.resetGeminiState() }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                is GeminiState.Success -> {
                    val data = state.response.data
                    if (data != null) {
                        var editedMealName by remember { mutableStateOf(data.meal_name) }
                        var editedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
                        val editedItems = remember { mutableStateListOf(*data.items.map { it.copy() }.toTypedArray()) }
                        
                        val totalCalories = editedItems.sumOf { it.calories }
                        val totalProtein = editedItems.sumOf { it.protein_g }
                        val totalCarbs = editedItems.sumOf { it.carbs_g }
                        val totalFat = editedItems.sumOf { it.fat_g }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                initialHour = java.text.SimpleDateFormat("HH", java.util.Locale.getDefault()).format(java.util.Date(editedTimestamp)).toInt(),
                                initialMinute = java.text.SimpleDateFormat("mm", java.util.Locale.getDefault()).format(java.util.Date(editedTimestamp)).toInt()
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
                                Text("Time: " + java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(editedTimestamp)))
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Calories: $totalCalories", style = MaterialTheme.typography.titleMedium)
                                Text("Protein: ${totalProtein}g", style = MaterialTheme.typography.titleMedium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Carbs: ${totalCarbs}g", style = MaterialTheme.typography.titleMedium)
                                Text("Fat: ${totalFat}g", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (editedItems.size > 1) {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Ingredients for $editedMealName", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        editedItems.forEachIndexed { index, item ->
                                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Column(modifier = Modifier.padding(8.dp)) {
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
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        OutlinedTextField(
                                                            value = if (item.protein_g == 0) "" else item.protein_g.toString(),
                                                            onValueChange = { editedItems[index] = item.copy(protein_g = it.toIntOrNull() ?: 0) },
                                                            label = { Text("Protein (g)") },
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        OutlinedTextField(
                                                            value = if (item.carbs_g == 0) "" else item.carbs_g.toString(),
                                                            onValueChange = { editedItems[index] = item.copy(carbs_g = it.toIntOrNull() ?: 0) },
                                                            label = { Text("Carbs (g)") },
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        OutlinedTextField(
                                                            value = if (item.fat_g == 0) "" else item.fat_g.toString(),
                                                            onValueChange = { editedItems[index] = item.copy(fat_g = it.toIntOrNull() ?: 0) },
                                                            label = { Text("Fat (g)") },
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text("Item Details", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                editedItems.forEachIndexed { index, item ->
                                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
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
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = if (item.protein_g == 0) "" else item.protein_g.toString(),
                                                    onValueChange = { editedItems[index] = item.copy(protein_g = it.toIntOrNull() ?: 0) },
                                                    label = { Text("Protein (g)") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = if (item.carbs_g == 0) "" else item.carbs_g.toString(),
                                                    onValueChange = { editedItems[index] = item.copy(carbs_g = it.toIntOrNull() ?: 0) },
                                                    label = { Text("Carbs (g)") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = if (item.fat_g == 0) "" else item.fat_g.toString(),
                                                    onValueChange = { editedItems[index] = item.copy(fat_g = it.toIntOrNull() ?: 0) },
                                                    label = { Text("Fat (g)") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedButton(onClick = { viewModel.resetGeminiState() }) {
                                    Text("Discard")
                                }
                                Button(onClick = {
                                    data.meal_name = editedMealName
                                    data.total_calories = totalCalories
                                    data.macros.protein_g = totalProtein
                                    data.macros.carbs_g = totalCarbs
                                    data.macros.fat_g = totalFat
                                    data.items = editedItems.toList()
                                    viewModel.saveMeal(state.response, editedTimestamp)
                                    onSaveComplete()
                                }) {
                                    Text("Save Meal")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
