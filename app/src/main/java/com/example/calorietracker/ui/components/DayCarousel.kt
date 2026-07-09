package com.example.calorietracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> DayCarousel(
    days: List<T>,
    getDate: (T) -> Long,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No data available",
    content: @Composable (day: T) -> Unit
) {
    if (days.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val sortedDays = remember(days) { days.sortedBy { getDate(it) } }
    
    val pagerState = rememberPagerState(
        initialPage = sortedDays.size - 1,
        pageCount = { sortedDays.size }
    )
    val scope = rememberCoroutineScope()
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = getDate(sortedDays[pagerState.currentPage])
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                        val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = selectedMillis }
                        val localCal = Calendar.getInstance().apply {
                            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val selectedLocalStart = localCal.timeInMillis
                        val selectedLocalEnd = selectedLocalStart + 86400000L
                        
                        val index = sortedDays.indexOfFirst { 
                            val ts = getDate(it)
                            ts in selectedLocalStart until selectedLocalEnd
                        }
                        if (index != -1) {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    }
                }) { Text("Jump") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                enabled = pagerState.currentPage > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Day")
            }
            
            val currentDate = getDate(sortedDays[pagerState.currentPage])
            val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(currentDate))
            
            Text(
                text = dateString,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { showDatePicker = true }
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            )
            
            IconButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                enabled = pagerState.currentPage < sortedDays.size - 1
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day")
            }
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            content(sortedDays[page])
        }
    }
}
