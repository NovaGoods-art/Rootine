package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsState()
    val logs by viewModel.allLogs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedLogDot by remember { mutableStateOf<Pair<Int, LocalDate>?>(null) } // habitId, date
    var showDeleteConfirm by remember { mutableStateOf<Habit?>(null) }

    val today = LocalDate.now()
    val last7Days = remember(today) {
        (0..6).reversed().map { today.minusDays(it.toLong()) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Welcome to your Rootine.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Breathe in. Consistency over perfection, always.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Global stats row
            val avgConsistency = viewModel.getAverageGlobalConsistency(habits, logs)
            val skipsLeft = viewModel.getRemainingSkips(logs)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(avgConsistency * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Avg Consistency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$skipsLeft",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Skips Remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Quiet Insight Banner
            val insight = viewModel.getQuietInsight(habits, logs)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Insight",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Habits Section Title
            Text(
                text = "Today's Gentle Intentions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Habit List
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No habits tracked yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Tap the '+' bubble to declare a quiet focus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habits) { habit ->
                        val habitConsistency = viewModel.getHabitConsistency(habit.id, habits, logs)
                        val habitLogs = logs.filter { it.habitId == habit.id }
                        
                        HabitCard(
                            habit = habit,
                            consistency = habitConsistency,
                            last7Days = last7Days,
                            habitLogs = habitLogs,
                            onDotClick = { clickedDate ->
                                selectedLogDot = Pair(habit.id, clickedDate)
                            },
                            onDeleteClick = {
                                showDeleteConfirm = habit
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Add Habit Dialog
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, threshold, plantType ->
                viewModel.addHabit(name, threshold, plantType)
                showAddDialog = false
            }
        )
    }

    // Modal Log Event Dialog (Done / Skip Reason / Clear)
    if (selectedLogDot != null) {
        val (habitId, clickedDate) = selectedLogDot!!
        val activeLog = logs.firstOrNull { it.habitId == habitId && it.date == clickedDate.toString() }
        val habitName = habits.firstOrNull { it.id == habitId }?.name ?: "Habit"

        LogActionDialog(
            habitName = habitName,
            date = clickedDate,
            currentLog = activeLog,
            onDismiss = { selectedLogDot = null },
            onMarkDone = {
                viewModel.toggleDone(habitId, clickedDate.toString())
                selectedLogDot = null
            },
            onMarkSkip = { reason ->
                viewModel.skipHabit(habitId, clickedDate.toString(), reason)
                selectedLogDot = null
            },
            onClear = {
                viewModel.clearLog(habitId, clickedDate.toString())
                selectedLogDot = null
            }
        )
    }

    // Confirm Delete Dialog
    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Release focus?") },
            text = { Text("Are you sure you want to delete '${showDeleteConfirm?.name}'? This removes its history gently with no shame.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteHabit(it) }
                        showDeleteConfirm = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Nevermind")
                }
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    consistency: Float,
    last7Days: List<LocalDate>,
    habitLogs: List<HabitLog>,
    onDotClick: (LocalDate) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title & Trash Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Plant: ${habit.plantType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month consistency bar vs. threshold
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "This month: ${(consistency * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Goal: ${(habit.goalThreshold * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Custom Progress track with goal indicator line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    // Filled part
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(consistency.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (consistency >= habit.goalThreshold) EarthPrimary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                    )
                    
                    // Goal vertical line marker
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .align(Alignment.CenterStart)
                            .offset(x = (280 * habit.goalThreshold).dp) // approximate visual scale offset
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row of 7 daily dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                last7Days.forEach { date ->
                    val dateLog = habitLogs.firstOrNull { it.date == date.toString() }
                    val isToday = date == LocalDate.now()
                    
                    val dotColor = when (dateLog?.status) {
                        "done" -> EarthPrimary
                        "skipped" -> EarthAmber
                        "missed" -> EarthGrayQuiet
                        else -> EarthGrayQuiet.copy(alpha = 0.4f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDotClick(date) }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(dotColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Float, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf(0.70f) }
    var plantType by remember { mutableStateOf("Fern") }
    val plantTypes = listOf("Fern", "Sunflower", "Cactus", "Tulip")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Frame a New Rootine") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Drink water, Draw, Walk") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "Consistency Goal: ${(threshold * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Aim for a realistic threshold, not perfection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Slider(
                        value = threshold,
                        onValueChange = { threshold = it },
                        valueRange = 0.1f..1.0f,
                        steps = 8
                    )
                }

                Column {
                    Text(
                        text = "Select your Garden Companion:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        plantTypes.forEach { type ->
                            InputChip(
                                selected = plantType == type,
                                onClick = { plantType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, threshold, plantType)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Seed Intention")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LogActionDialog(
    habitName: String,
    date: LocalDate,
    currentLog: HabitLog?,
    onDismiss: () -> Unit,
    onMarkDone: () -> Unit,
    onMarkSkip: (String) -> Unit,
    onClear: () -> Unit
) {
    var showReasons by remember { mutableStateOf(false) }
    val skipReasons = listOf("tired", "busy", "unwell", "weather")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = habitName, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Log entry for ${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.dayOfMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentLog != null) {
                    Text(
                        text = "Current State: " + when (currentLog.status) {
                            "done" -> "Completed"
                            "skipped" -> "Skipped (${currentLog.skipReason})"
                            else -> "Missed"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!showReasons) {
                    Button(
                        onClick = onMarkDone,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EarthPrimary)
                    ) {
                        Text(if (currentLog?.status == "done") "Toggle Completed Off" else "Mark Completed")
                    }

                    Button(
                        onClick = { showReasons = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EarthAmber)
                    ) {
                        Text("Forgive & Skip with Reason")
                    }

                    if (currentLog != null) {
                        OutlinedButton(
                            onClick = onClear,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset / Clear Day")
                        }
                    }
                } else {
                    Text(
                        text = "Why are you skipping? Skips are forgiven:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        skipReasons.forEach { reason ->
                            OutlinedButton(
                                onClick = { onMarkSkip(reason) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(reason.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                            }
                        }
                        TextButton(
                            onClick = { showReasons = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
