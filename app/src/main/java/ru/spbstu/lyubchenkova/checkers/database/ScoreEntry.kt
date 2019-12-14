package ru.spbstu.lyubchenkova.checkers.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_table")
data class ScoreEntry(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var playerName: String = "",
        var score: Int = 0
)