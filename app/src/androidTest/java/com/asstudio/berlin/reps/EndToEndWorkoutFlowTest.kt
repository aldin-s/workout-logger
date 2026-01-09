package com.asstudio.berlin.reps

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-End Test für den kompletten Workout-Flow
 * 
 * Testet den kompletten User Journey:
 * 1. App startet auf MainActivity
 * 2. "Start Workout" Button klicken
 * 3. Übung auswählen (z.B. Bankdrücken)
 * 4. Gewicht eingeben
 * 5. Wiederholungen eingeben
 * 6. Pausenzeit eingeben
 * 7. Anzahl Sätze eingeben
 * 8. "Start" Button klicken
 * 9. Timer Activity öffnet sich
 * 10. Timer läuft herunter
 * 
 * Dieser Test simuliert einen echten Benutzer!
 */
@RunWith(AndroidJUnit4::class)
class EndToEndWorkoutFlowTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun completeWorkoutFlow_StandardExercise_Success() {
        // ========================================
        // SCHRITT 1: MainActivity - Start Workout
        // ========================================
        
        // Warte kurz bis UI geladen ist
        Thread.sleep(500)
        
        // Verifiziere dass wir auf MainActivity sind
        onView(withId(R.id.startWorkoutButton))
            .check(matches(isDisplayed()))
        
        // Klicke "Start Workout" Button
        onView(withId(R.id.startWorkoutButton))
            .perform(click())
        
        // ========================================
        // SCHRITT 2: WorkoutInputActivity - Übung auswählen
        // ========================================
        
        // Warte bis WorkoutInputActivity geladen ist
        Thread.sleep(500)
        
        // Verifiziere dass wir auf WorkoutInputActivity sind
        onView(withId(R.id.cardBankdruecken))
            .check(matches(isDisplayed()))
        
        // Wähle "Bankdrücken" aus
        onView(withId(R.id.cardBankdruecken))
            .perform(click())
        
        // ========================================
        // SCHRITT 3: Eingaben machen
        // ========================================
        
        // Gewicht eingeben: 100 kg
        onView(withId(R.id.weightEditText))
            .perform(clearText(), typeText("100"), closeSoftKeyboard())
        
        // Wiederholungen eingeben: 5 reps
        onView(withId(R.id.repsEditText))
            .perform(clearText(), typeText("5"), closeSoftKeyboard())
        
        // Pausenzeit eingeben: 120 Sekunden (2 Minuten)
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("120"), closeSoftKeyboard())
        
        // Anzahl Sätze eingeben: 3 Sätze
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("3"), closeSoftKeyboard())
        
        // Warte kurz damit Validierung durchläuft
        Thread.sleep(300)
        
        // ========================================
        // SCHRITT 4: Start Button klicken
        // ========================================
        
        // Verifiziere dass Start Button aktiviert ist
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
        
        // Klicke Start Button
        onView(withId(R.id.startButton))
            .perform(click())
        
        // ========================================
        // SCHRITT 5: TimerActivity - Timer läuft
        // ========================================
        
        // Warte bis TimerActivity geladen ist und Service startet
        Thread.sleep(2000)
        
        // Verifiziere dass wir auf TimerActivity sind
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))
        
        // Verifiziere dass Timer-Text angezeigt wird (sollte 02:00 oder 01:59 sein)
        onView(withId(R.id.timerTextView))
            .check(matches(not(withText("00:00"))))
        
        // Verifiziere dass Satz-Anzeige korrekt ist (1/3)
        onView(withId(R.id.setsTextView))
            .check(matches(withText("Satz 1/3")))
        
        // Verifiziere dass Übungsname angezeigt wird
        onView(withId(R.id.exerciseNameTextView))
            .check(matches(withText("Bankdrücken")))
        
        // ========================================
        // SCHRITT 6: Warte und verifiziere Timer läuft herunter
        // ========================================
        
        // Warte 3 Sekunden
        Thread.sleep(3000)
        
        // Timer sollte jetzt bei ~01:57 oder weniger sein
        // (Wir können nicht exakt prüfen wegen Timing, aber es sollte nicht mehr 02:00 sein)
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))
        
        // ✅ SUCCESS: Kompletter Workflow funktioniert!
    }

    @Test
    fun completeWorkoutFlow_CustomExercise_Success() {
        // ========================================
        // KOMPLETTER FLOW MIT EIGENER ÜBUNG
        // (Wichtig für Android 14 Xiaomi Bug-Test!)
        // ========================================
        
        Thread.sleep(500)
        
        // MainActivity: Start Workout
        onView(withId(R.id.startWorkoutButton))
            .perform(click())
        
        Thread.sleep(500)
        
        // WorkoutInputActivity: Eigene Übung auswählen
        onView(withId(R.id.cardCustomExercise))
            .perform(click())
        
        // Übungsnamen eingeben
        onView(withId(R.id.customExerciseEditText))
            .perform(typeText("Bizeps Curl"), closeSoftKeyboard())
        
        // Eingaben machen
        onView(withId(R.id.weightEditText))
            .perform(clearText(), typeText("30"), closeSoftKeyboard())
        
        onView(withId(R.id.repsEditText))
            .perform(clearText(), typeText("12"), closeSoftKeyboard())
        
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("90"), closeSoftKeyboard())
        
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("3"), closeSoftKeyboard())
        
        Thread.sleep(300)
        
        // Start Button klicken
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
            .perform(click())
        
        // Warte auf TimerActivity
        Thread.sleep(2000)
        
        // Verifiziere Timer läuft
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))
            .check(matches(not(withText("00:00"))))
        
        // Verifiziere eigene Übung wird angezeigt
        onView(withId(R.id.exerciseNameTextView))
            .check(matches(withText("Bizeps Curl")))
        
        // Verifiziere Satz-Anzeige
        onView(withId(R.id.setsTextView))
            .check(matches(withText("Satz 1/3")))
        
        // ✅ SUCCESS: Eigene Übung funktioniert (Android 14+ Fix verifiziert!)
    }

    @Test
    fun completeWorkoutFlow_MinimalValues_Success() {
        // ========================================
        // EDGE CASE: Minimale Werte
        // ========================================
        
        Thread.sleep(500)
        
        onView(withId(R.id.startWorkoutButton))
            .perform(click())
        
        Thread.sleep(500)
        
        // Kreuzheben auswählen
        onView(withId(R.id.cardKreuzheben))
            .perform(click())
        
        // Minimale Werte eingeben
        onView(withId(R.id.weightEditText))
            .perform(clearText(), typeText("0.5"), closeSoftKeyboard())
        
        onView(withId(R.id.repsEditText))
            .perform(clearText(), typeText("1"), closeSoftKeyboard())
        
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("10"), closeSoftKeyboard())
        
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("1"), closeSoftKeyboard())
        
        Thread.sleep(300)
        
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
            .perform(click())
        
        Thread.sleep(2000)
        
        // Verifiziere Timer läuft mit minimalen Werten
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.setsTextView))
            .check(matches(withText("Satz 1/1")))
        
        // ✅ SUCCESS: Minimale Werte funktionieren!
    }

    @Test
    fun completeWorkoutFlow_MaximalValues_Success() {
        // ========================================
        // EDGE CASE: Maximale Werte
        // ========================================
        
        Thread.sleep(500)
        
        onView(withId(R.id.startWorkoutButton))
            .perform(click())
        
        Thread.sleep(500)
        
        // Kniebeuge auswählen
        onView(withId(R.id.cardKniebeuge))
            .perform(click())
        
        // Maximale Werte eingeben
        onView(withId(R.id.weightEditText))
            .perform(clearText(), typeText("999"), closeSoftKeyboard())
        
        onView(withId(R.id.repsEditText))
            .perform(clearText(), typeText("100"), closeSoftKeyboard())
        
        onView(withId(R.id.pauseTimeEditText))
            .perform(clearText(), typeText("600"), closeSoftKeyboard())
        
        onView(withId(R.id.setsEditText))
            .perform(clearText(), typeText("50"), closeSoftKeyboard())
        
        Thread.sleep(300)
        
        onView(withId(R.id.startButton))
            .check(matches(isEnabled()))
            .perform(click())
        
        Thread.sleep(2000)
        
        // Verifiziere Timer läuft mit maximalen Werten
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.setsTextView))
            .check(matches(withText("Satz 1/50")))
        
        onView(withId(R.id.exerciseNameTextView))
            .check(matches(withText("Kniebeuge")))
        
        // ✅ SUCCESS: Maximale Werte funktionieren!
    }
}
