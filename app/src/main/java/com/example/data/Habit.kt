package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val goalThreshold: Float, // e.g. 0.70f for 70%
    val createdAt: Long = System.currentTimeMillis(),
    val plantType: String = "Fern" // "Fern", "Sunflower", "Bonsai", "Tulip", "Cactus"
)
