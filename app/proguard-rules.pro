# ProGuard Rules for FlourMate Android Application

# Keep all data models for Room and Firebase Serialization
-keep class com.marwadiuniversity.flourmate.models.** { *; }

# Keep Room DAOs and Database
-keep class com.marwadiuniversity.flourmate.database.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Keep Firebase and Google Play Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Gson annotations and serialized names
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep View Binding
-keep class com.marwadiuniversity.flourmate.databinding.** { *; }

# Keep MainActivity
-keep class com.marwadiuniversity.flourmate.MainActivity { *; }
