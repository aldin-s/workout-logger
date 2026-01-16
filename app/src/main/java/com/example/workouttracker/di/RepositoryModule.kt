package com.example.workouttracker.di

import com.example.workouttracker.data.repository.ExerciseRepository
import com.example.workouttracker.data.repository.ExerciseRepositoryImpl
import com.example.workouttracker.data.repository.TimerRepository
import com.example.workouttracker.data.repository.TimerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTimerRepository(
        timerRepositoryImpl: TimerRepositoryImpl
    ): TimerRepository
    
    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        impl: ExerciseRepositoryImpl
    ): ExerciseRepository
}
