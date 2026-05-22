package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String, // YYYY-MM-DD
    val status: String, // "done", "skipped" (missed is implicit or stored if explicit)
    val skipReason: String? = null // "tired", "busy", "unwell", "weather"
)
