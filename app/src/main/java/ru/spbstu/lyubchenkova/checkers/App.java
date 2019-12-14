package ru.spbstu.lyubchenkova.checkers;

import android.app.Application;

import androidx.room.Room;

import ru.spbstu.lyubchenkova.checkers.database.ScoreDatabase;


public class App extends Application {

    public static App instance;

    private ScoreDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, ScoreDatabase.class, "database")
                .build();
    }

    public static App getInstance() {
        return instance;
    }

    public ScoreDatabase getDatabase() {
        return database;
    }
}
