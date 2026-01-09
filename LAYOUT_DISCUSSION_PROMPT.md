# Multi-Perspektiven Diskussions-Prompt für Layout-Änderung

Kopiere diesen Prompt und füge ihn in einen neuen Chat ein (GitHub Copilot, ChatGPT, Claude, etc.):

---

## 🎭 ROLLEN-DISKUSSION: Eigene Übungen Feature

Du bist ein Experten-Panel bestehend aus 4 Personen. Diskutiert die folgende Design-Entscheidung aus verschiedenen Perspektiven.

### 📱 KONTEXT:

**App:** Android Workout Tracker (Kotlin, Material Design)
**Aktueller Stand:** 
- 6 Standard-Übungen als Cards (Bankdrücken, Kreuzheben, etc.)
- 1 "Eigene Übung" Card mit Texteingabe
- Problem: Eigene Übungen werden NICHT gespeichert und müssen jedes Mal neu eingegeben werden

**User Feedback:**
- "Ich muss meine Bizeps-Übung jedes Mal neu eingeben"
- "Kann man eigene Übungen nicht speichern?"

---

### 🎨 DESIGN-OPTIONEN:

#### **Option A: Alles auf einem Screen**
- Standard-Cards OBEN (wie bisher)
- "Eigene Übung" Card bleibt
- NEU: "Zuletzt verwendet" Bereich UNTEN (scrollbar)
- Zeigt eigene Übungen mit Häufigkeit & Datum

#### **Option B: Tab-Navigation**
- Tab 1: Standard (6 Cards)
- Tab 2: Eigene (Eingabe + Liste gespeicherter Übungen)
- Tab 3: Zuletzt (Historie nach Datum sortiert)

#### **Option C: Smart Autocomplete**
- Standard-Cards bleiben wie bisher
- "Eigene Übung" Card mit Eingabefeld
- Beim Tippen: Dropdown mit Vorschlägen aus Historie
- Zeigt: "Bizeps Curl (3x, vor 2 Tagen)"

---

## 🎭 DISKUTIERT AUS FOLGENDEN PERSPEKTIVEN:

### 1️⃣ **UX/UI DESIGNER** 🎨
**Fokus:** User Experience, Usability, Design Trends
**Fragen:**
- Welche Option ist am intuitivsten?
- Wie viele Klicks braucht der User?
- Ist es visuell ansprechend?
- Accessibility-Aspekte?
- Design-Konsistenz mit Material Design?

### 2️⃣ **ANDROID ENTWICKLER** 💻
**Fokus:** Technische Umsetzbarkeit, Code-Qualität, Wartbarkeit
**Fragen:**
- Wie viel Entwicklungszeit pro Option? (Stunden schätzen)
- Welche neuen Komponenten/Libraries nötig?
- Database-Änderungen erforderlich?
- Performance-Implikationen?
- Test-Komplexität?

### 3️⃣ **PRODUCT OWNER** 📊
**Fokus:** Business Value, Time-to-Market, User Satisfaction
**Fragen:**
- Welche Option liefert schnellsten Mehrwert?
- ROI (Return on Investment)?
- Passt es zur Roadmap?
- Beta-Test strategie?
- Release-Priorisierung?

### 4️⃣ **END USER** (Fitness-Enthusiast) 💪
**Fokus:** Praktische Nutzung, Alltags-Tauglichkeit
**Fragen:**
- Welche Option würdest du im Gym nutzen?
- Was ist schnell während dem Training?
- Willst du Übungen organisieren oder nur schnell starten?
- Störende Features?
- Was vermisst du aktuell am meisten?

---

## 📝 DISKUSSIONS-FORMAT:

**Für jede Option (A, B, C):**

1. **UX Designer** gibt Statement (2-3 Sätze)
2. **Entwickler** antwortet mit Machbarkeit (inkl. Zeitschätzung)
3. **Product Owner** bewertet Business Value (1-10 Punkte)
4. **End User** gibt praktisches Feedback

**Am Ende:** 
- Konsens finden oder Abstimmung
- Empfehlung mit Begründung
- Nächste Schritte definieren

---

## 🎯 ZUSÄTZLICHE INFOS:

**Technischer Stack:**
- Kotlin, Android SDK 21-36
- Room Database (bereits implementiert)
- Material Design Components
- Bereits vorhanden: CompletedSet Tabelle mit exerciseName

**Constraints:**
- App muss offline funktionieren
- Kein Cloud-Sync
- Minimale APK-Größe
- App ist bereits im Play Store (Version 36)

---

**START DER DISKUSSION:**

Beginnt mit Option A und diskutiert alle Perspektiven. Dann Option B, dann Option C. Am Ende: Gesamtempfehlung.

Los geht's! 🚀
