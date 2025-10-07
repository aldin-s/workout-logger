# REPS
**Simple Workout Tracker**

> *Track Your Progress*

## Overview
REPS is a minimalist Android workout tracker designed for serious lifters. No gamification, no social features—just pure functionality to log your sets, track your progress, and focus on what matters: getting stronger.

## Features
- **⏱️ Rest Timer**: Smart countdown timer between sets with state preservation
- **📊 Workout Logging**: Quick and easy input for exercises, weight, reps, and sets
- **📈 Progress Tracking**: View your workout history grouped by date with session consolidation
- **🌙 Dark Mode**: Clean, minimal interface with brutalist design (#121212 background)
- **🌍 Internationalization**: Full support for German and English
- **💾 Local Storage**: All data stored locally with Room Database—no cloud, no tracking

## Design Philosophy
REPS follows a strict minimalist approach:
- Flat design with no elevation or shadows
- High contrast typography hierarchy (18sp → 14sp → 12sp)
- No unnecessary animations or distractions
- Focus on speed and usability in the gym

## Tech Stack
- **Language**: Kotlin
- **UI**: Material Design Components
- **Database**: Room (SQLite)
- **Architecture**: MVVM-style with coroutines
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)

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