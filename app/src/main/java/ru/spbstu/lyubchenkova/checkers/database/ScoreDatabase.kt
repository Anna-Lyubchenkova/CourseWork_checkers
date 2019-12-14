package ru.spbstu.lyubchenkova.checkers.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScoreEntry::class], version = 1, exportSchema = false)
abstract class ScoreDatabase: RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
}