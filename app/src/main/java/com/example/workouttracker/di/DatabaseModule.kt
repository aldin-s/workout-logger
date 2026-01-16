package com.example.workouttracker.di

import android.content.Context
import androidx.room.Room
import com.example.workouttracker.data.database.CompletedSetDao
import com.example.workouttracker.data.database.ExerciseDao
import com.example.workouttracker.data.database.WorkoutDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WorkoutDatabase {
        return Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "workout_database"
        )
        .addMigrations(
            WorkoutDatabase.MIGRATION_1_2,
            WorkoutDatabase.MIGRATION_2_3
        )
        .build()
    }
    
    @Provides
    fun provideCompletedSetDao(database: WorkoutDatabase): CompletedSetDao {
        return database.completedSetDao()
    }
    
    @Provides
    fun provideExerciseDao(database: WorkoutDatabase): ExerciseDao {
        return database.exerciseDao()
    }
}
