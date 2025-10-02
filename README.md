# Workout Tracker

## Overview
Workout Tracker is an Android application designed to help users manage their workouts effectively. The app includes features such as a timer, workout input screen, and functionality for tracking sets and repetitions.

## Features
- **Timer Functionality**: Users can set a countdown timer for their workouts, helping them manage their exercise intervals.
- **Workout Input**: A dedicated screen for users to input their workout details, including weight, repetitions, and rest times.
- **Tracking Progress**: Users can track their workout progress, including the number of sets completed and repetitions.

## Project Structure
The project is organized into several packages and modules:

- **MainActivity.kt**: The entry point of the application, initializing the UI.
- **TimerActivity.kt**: Manages the timer and tracks sets.
- **WorkoutInputActivity.kt**: UI for inputting workout details.
- **TrackingActivity.kt**: Displays current workout progress.
- **Data Models**: Includes `Workout.kt` and `Set.kt` for managing workout data.
- **Database**: `WorkoutDatabase.kt` for storing and retrieving workout data.
- **Utilities**: `TimerUtils.kt` for timer management functions.

## Installation
To run the project, clone the repository and open it in your preferred IDE. Ensure you have the necessary Android SDK and dependencies installed.

## Usage
1. Launch the app.
2. Input your workout details in the Workout Input screen.
3. Start the timer and track your sets and repetitions.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License.