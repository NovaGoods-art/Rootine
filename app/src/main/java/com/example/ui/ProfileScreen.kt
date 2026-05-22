package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EarthAmber
import com.example.ui.theme.EarthPrimary

@Composable
fun ProfileScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsState()
    val logs by viewModel.allLogs.collectAsState()

    var userName by rememberSaveable { mutableStateOf("Celia Gray") }
    var isEditingName by remember { mutableStateOf(false) }
    var currentNameInput by remember { mutableStateOf("") }
    
    // Avatar selection: Cactus, Flower, Leaf, Bonsai
    var selectedAvatar by rememberSaveable { mutableStateOf("Leaf") }
    val avatarOptions = listOf("Leaf", "Flower", "Bonsai", "Cactus")

    val totalHabits = habits.size
    val longestSoftStreak = viewModel.getGlobalLongestSoftStreak(habits, logs)
    val avgConsistency = viewModel.getAverageGlobalConsistency(habits, logs)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header
        Text(
            text = "Your Gentle Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Avatar Visual Card with active icon
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val symbol = when (selectedAvatar) {
                "Leaf" -> "🌿"
                "Flower" -> "🌸"
                "Bonsai" -> "🌳"
                "Cactus" -> "🌵"
                else -> "🌱"
            }
            Text(
                text = symbol,
                fontSize = 58.sp,
                textAlign = TextAlign.Center
            )
        }

        // Avatar selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            avatarOptions.forEach { type ->
                val symbol = when (type) {
                    "Leaf" -> "🌿"
                    "Flower" -> "🌸"
                    "Bonsai" -> "🌳"
                    "Cactus" -> "🌵"
                    else -> "🌱"
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedAvatar == type) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                        .clickable { selectedAvatar = type },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = symbol, fontSize = 20.sp)
                }
            }
        }

        // Editable User Name Row
        if (isEditingName) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentNameInput,
                    onValueChange = { currentNameInput = it },
                    singleLine = true,
                    label = { Text("Set Name") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (currentNameInput.isNotBlank()) {
                            userName = currentNameInput
                        }
                        isEditingName = false
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Accept Name", tint = EarthPrimary)
                }
                IconButton(
                    onClick = { isEditingName = false }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel Detail", tint = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        currentNameInput = userName
                        isEditingName = true
                    }
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit name",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Core soft streak parameters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$totalHabits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total Habits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$longestSoftStreak Days",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EarthAmber
                    )
                    Text(
                        text = "Longest Soft Streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Growth story panel
        val percentage = (avgConsistency * 100).toInt()
        val growthStory = when {
            percentage >= 80 -> "You showed up $percentage% of the time. You are blooming magnificent, remember to hold space to breathe."
            percentage >= 70 -> "You showed up $percentage% of the time this month. That is comfortably clear of the goal line. That's enough."
            percentage >= 50 -> "You showed up $percentage% of the time. A gentle pace is still a glorious path forward. Sleep well tonight."
            percentage >= 30 -> "You showed up $percentage% of the time. You held space for your intentions in real life. That is a great victory."
            else -> "Every drop of efforts counts. Your seeds are sleeping, waiting for their next gentle touchpoint. Tomorrow is soft."
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Zen Flower",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 12.dp)
                )
                Text(
                    text = "Your Growth Story",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = growthStory,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
