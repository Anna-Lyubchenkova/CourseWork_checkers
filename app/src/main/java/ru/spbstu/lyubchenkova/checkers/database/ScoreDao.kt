package ru.spbstu.lyubchenkova.checkers.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreDao {
    @Insert
    fun insert(scoreEntry: ScoreEntry)

    @Query("UPDATE score_table SET score = :score WHERE playerName = :name")
    fun updateScore(name: String, score: Int)

    @Query("SELECT * from score_table")
    suspend fun getAll(): List<ScoreEntry>

    @Query("SELECT * FROM score_table WHERE playerName = :name")
    fun getByName(name: String): ScoreEntry
}