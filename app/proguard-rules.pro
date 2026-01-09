# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK's proguard-android-optimize.txt

# Keep data classes for Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** getDatabase(...);
}

# Keep model classes
-keep class com.asstudio.berlin.reps.data.model.** { *; }
-keep class com.asstudio.berlin.reps.data.entity.** { *; }

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
