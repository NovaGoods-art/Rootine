package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allLogs: Flow<List<HabitLog>> = habitDao.getAllLogs()

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun logDone(habitId: Int, date: String) {
        val existing = habitDao.getLogForHabitAndDate(habitId, date)
        if (existing?.status == "done") {
            // Delete log to toggle off
            habitDao.deleteLogForHabitAndDate(habitId, date)
        } else {
            // Replace with done status
            habitDao.insertLog(HabitLog(habitId = habitId, date = date, status = "done"))
        }
    }

    suspend fun logSkip(habitId: Int, date: String, reason: String) {
        habitDao.insertLog(HabitLog(habitId = habitId, date = date, status = "skipped", skipReason = reason))
    }

    suspend fun logMiss(habitId: Int, date: String) {
        habitDao.insertLog(HabitLog(habitId = habitId, date = date, status = "missed"))
    }

    suspend fun clearLog(habitId: Int, date: String) {
        habitDao.deleteLogForHabitAndDate(habitId, date)
    }
}
