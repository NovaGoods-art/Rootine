package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    val allHabits: StateFlow<List<Habit>>
    val allLogs: StateFlow<List<HabitLog>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())
        
        allHabits = repository.allHabits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        allLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial data if database is empty
        viewModelScope.launch {
            repository.allHabits.collect { habitsList ->
                if (habitsList.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        // Create 3 beautiful habits
        val h1 = Habit(name = "Morning Sun & Breathing", goalThreshold = 0.70f, plantType = "Cactus")
        val h2 = Habit(name = "Afternoon Walk", goalThreshold = 0.60f, plantType = "Sunflower")
        val h3 = Habit(name = "Stay Hydrated", goalThreshold = 0.80f, plantType = "Fern")
        
        repository.insertHabit(h1)
        repository.insertHabit(h2)
        repository.insertHabit(h3)

        // Generate logs for the last 15 days to populate heatmaps & stats elegantly
        val today = LocalDate.now()
        
        // Let's seed some done, some skipped, and leave some blank (implicit missed)
        // For h1: Morning Sun (High consistency 75%)
        val skipReasons = listOf("busy", "unwell", "tired", "weather")
        
        // We simulate logs for the past 14 days
        for (i in 1..14) {
            val dateStr = today.minusDays(i.toLong()).toString()
            when (i % 7) {
                0, 1, 3, 5 -> repository.logDone(1, dateStr)
                2 -> repository.logSkip(1, dateStr, skipReasons[i % skipReasons.size])
                // others are misses
            }
        }

        // For h2: Afternoon Walk (Consistency around 60%)
        for (i in 1..14) {
            val dateStr = today.minusDays(i.toLong()).toString()
            when (i % 5) {
                0, 2, 4 -> repository.logDone(2, dateStr)
                1 -> repository.logSkip(2, dateStr, "weather")
            }
        }

        // For h3: Stay Hydrated (Consistency around 85%)
        for (i in 1..14) {
            val dateStr = today.minusDays(i.toLong()).toString()
            when (i % 4) {
                0, 1, 2 -> repository.logDone(3, dateStr)
                3 -> repository.logSkip(3, dateStr, "tired")
            }
        }
        
        // Log today as done for Hydration and Sun
        repository.logDone(1, today.toString())
        repository.logDone(3, today.toString())
    }

    // Interactive operations
    fun addHabit(name: String, threshold: Float, plantType: String) = viewModelScope.launch {
        repository.insertHabit(Habit(name = name, goalThreshold = threshold, plantType = plantType))
    }

    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        repository.deleteHabit(habit)
    }

    fun updateHabit(habit: Habit) = viewModelScope.launch {
        repository.updateHabit(habit)
    }

    fun toggleDone(habitId: Int, date: String) = viewModelScope.launch {
        repository.logDone(habitId, date)
    }

    fun skipHabit(habitId: Int, date: String, reason: String) = viewModelScope.launch {
        repository.logSkip(habitId, date, reason)
    }

    fun clearLog(habitId: Int, date: String) = viewModelScope.launch {
        repository.clearLog(habitId, date)
    }

    // Statistics Calculation functions that take state as arguments
    fun getHabitConsistency(habitId: Int, habits: List<Habit>, logs: List<HabitLog>): Float {
        val habit = habits.firstOrNull { it.id == habitId } ?: return 0f
        val habitLogs = logs.filter { it.habitId == habitId }
        
        // We look at the rolling 30 days or since creation to compute rolling consistency
        val today = LocalDate.now()
        val limitDate = today.minusDays(29) // 30 days window
        
        val creationDate = InstantToLocalDate(habit.createdAt)
        val startDate = if (creationDate.isAfter(limitDate)) creationDate else limitDate
        val totalDays = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        
        // Filter logs within this window
        val relevantLogs = habitLogs.filter {
            val d = LocalDate.parse(it.date)
            !d.isBefore(startDate) && !d.isAfter(today)
        }
        
        val doneCount = relevantLogs.count { it.status == "done" }
        val skippedCount = relevantLogs.count { it.status == "skipped" }
        
        val denominator = totalDays - skippedCount
        if (denominator <= 0) return 1.0f // if they skipped 100% of the active days, skips are forgiven, so 100% (or 1.0)
        
        return doneCount.toFloat() / denominator.toFloat()
    }

    fun getAverageGlobalConsistency(habits: List<Habit>, logs: List<HabitLog>): Float {
        if (habits.isEmpty()) return 0f
        var sum = 0f
        habits.forEach {
            sum += getHabitConsistency(it.id, habits, logs)
        }
        return sum / habits.size
    }

    fun getRemainingSkips(logs: List<HabitLog>): Int {
        val today = LocalDate.now()
        // Count all skipped logs in the current calendar month
        val currentMonthLogs = logs.filter {
            val d = LocalDate.parse(it.date)
            d.year == today.year && d.monthValue == today.monthValue && it.status == "skipped"
        }
        val maxSkips = 5
        return (maxSkips - currentMonthLogs.size).coerceAtLeast(0)
    }

    fun getLongestSoftStreak(habitId: Int, habits: List<Habit>, logs: List<HabitLog>): Int {
        val habit = habits.firstOrNull { it.id == habitId } ?: return 0
        val habitLogs = logs.filter { it.habitId == habitId }.associateBy { it.date }
        
        val today = LocalDate.now()
        val creationDate = InstantToLocalDate(habit.createdAt)
        
        var maxStreak = 0
        var currentStreak = 0
        
        var tempDate = creationDate
        while (!tempDate.isAfter(today)) {
            val dateStr = tempDate.toString()
            val log = habitLogs[dateStr]
            
            if (log != null && (log.status == "done" || log.status == "skipped")) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 0
            }
            tempDate = tempDate.plusDays(1)
        }
        return maxStreak
    }

    fun getGlobalLongestSoftStreak(habits: List<Habit>, logs: List<HabitLog>): Int {
        if (habits.isEmpty()) return 0
        return habits.maxOfOrNull { getLongestSoftStreak(it.id, habits, logs) } ?: 0
    }

    fun getWeeklyConsistencyForChart(habitId: Int, habits: List<Habit>, logs: List<HabitLog>): List<Float> {
        val habit = habits.firstOrNull { it.id == habitId } ?: return List(8) { 0f }
        val habitLogs = logs.filter { it.habitId == habitId }.associateBy { it.date }
        val today = LocalDate.now()
        
        // We want 8 weeks ending with this week
        // Week 7: current 7 days [today - 6 to today]
        // Week 6: [today - 13 to today - 7]
        // etc... down to Week 0
        val weeksList = mutableListOf<Float>()
        for (w in 7 downTo 0) {
            val weekEnd = today.minusDays((w * 7).toLong())
            val weekStart = weekEnd.minusDays(6)
            
            var doneCount = 0
            var skippedCount = 0
            var tempDate = weekStart
            while (!tempDate.isAfter(weekEnd)) {
                val log = habitLogs[tempDate.toString()]
                if (log != null) {
                    if (log.status == "done") doneCount++
                    else if (log.status == "skipped") skippedCount++
                }
                tempDate = tempDate.plusDays(1)
            }
            val denominator = 7 - skippedCount
            val consistency = if (denominator <= 0) 1.0f else doneCount.toFloat() / denominator.toFloat()
            weeksList.add(consistency)
        }
        return weeksList
    }

    fun getSkipReasonBreakdown(logs: List<HabitLog>): Map<String, Int> {
        val counts = mutableMapOf("tired" to 0, "busy" to 0, "unwell" to 0, "weather" to 0)
        logs.forEach { log ->
            if (log.status == "skipped" && log.skipReason != null) {
                val cleanKey = log.skipReason.lowercase()
                if (counts.containsKey(cleanKey)) {
                    counts[cleanKey] = counts.getOrDefault(cleanKey, 0) + 1
                }
            }
        }
        return counts
    }

    fun getQuietInsight(habits: List<Habit>, logs: List<HabitLog>): String {
        if (habits.isEmpty()) {
            return "Seed some gentle intentions above. Slow progress is beautiful."
        }
        val skipsNum = logs.count { it.status == "skipped" }
        val doneNum = logs.count { it.status == "done" }
        
        if (doneNum == 0 && skipsNum == 0) {
            return "Honest averages are a journey of miles. Mark or skip a habit today to begin."
        }

        val reasons = getSkipReasonBreakdown(logs)
        val majorReasonEntry = reasons.maxByOrNull { it.value }
        
        if (majorReasonEntry != null && majorReasonEntry.value > 0) {
            val count = majorReasonEntry.value
            val r = majorReasonEntry.key
            return when (r) {
                "tired" -> "Listening to fatigue is wisdom. You nested $count skips to rest. Well done honoring yourself."
                "busy" -> "A full schedule is real life. You forgave yourself $count times today. Life happens."
                "unwell" -> "Your wellness is an ongoing flow. $count skips when unwell is exactly how you heal."
                "weather" -> "The external elements are out of control. You pivoted gracefully $count times. Nature is wise."
                else -> "Skips are safe zones. You preserved your streak $skipsNum times with forgiving skips."
            }
        }

        // Default soft advice
        val avgC = getAverageGlobalConsistency(habits, logs) * 100
        return if (avgC >= 70) {
            "You are showing up ${avgC.toInt()}% of the time overall. This comfortably matches Rootine goals!"
        } else {
            "Consistency is currently ${avgC.toInt()}%. That is enough. Every dot counts as a touchpoint."
        }
    }

    private fun InstantToLocalDate(timestamp: Long): LocalDate {
        // Simple conversion for database dates safely
        return try {
            val epochDay = timestamp / (24 * 60 * 60 * 1000)
            LocalDate.ofEpochDay(epochDay)
        } catch (e: Exception) {
            LocalDate.now().minusDays(30)
        }
    }
}
