package com.example.calorietracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.calorietracker.ui.viewmodels.GeminiState
import com.example.calorietracker.ui.viewmodels.MainViewModel

data class MealItemInput(
    var name: String = "",
    var quantity: String = "",
    var unit: String = ""
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
        Box(modifier = Modifier.padding(paddingValues)) {
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
                            itemsIndexed(mealItems) { index, item ->
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    var expanded by remember { mutableStateOf(false) }
                                    val unitOptions = listOf("g", "oz", "cups", "tbsp", "tsp", "ml", "pieces")

                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded },
                                        modifier = Modifier.weight(1.5f)
                                    ) {
                                        OutlinedTextField(
                                            value = item.unit,
                                            onValueChange = { mealItems[index] = item.copy(unit = it) },
                                            label = { Text("Unit") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                            modifier = Modifier.menuAnchor()
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
                                    IconButton(onClick = { if (mealItems.size > 1) mealItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            item {
                                TextButton(onClick = { mealItems.add(MealItemInput()) }) {
                                    Text("+ Add Item")
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val prompt = "Calculate macros for the following meal:\n" +
                                        mealItems.joinToString("\n") { "${it.quantity} ${it.unit} of ${it.name}" }
                                viewModel.analyzeMeal(prompt)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calculate")
                        }
                    }
                }
                is GeminiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                val prompt = "Calculate macros for the following meal:\n" +
                                        mealItems.joinToString("\n") { "${it.quantity} ${it.unit} of ${it.name}" } +
                                        "\nClarification: $clarificationAnswer"
                                viewModel.analyzeMeal(prompt)
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Meal: ${data.meal_name}", style = MaterialTheme.typography.headlineMedium)
                            Text("Total Calories: ${data.total_calories} kcal", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Protein: ${data.macros.protein_g}g", style = MaterialTheme.typography.bodyLarge)
                            Text("Carbs: ${data.macros.carbs_g}g", style = MaterialTheme.typography.bodyLarge)
                            Text("Fat: ${data.macros.fat_g}g", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(32.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedButton(onClick = { viewModel.resetGeminiState() }) {
                                    Text("Discard")
                                }
                                Button(onClick = {
                                    viewModel.saveMeal(state.response)
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
