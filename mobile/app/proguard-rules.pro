# Add project specific ProGuard rules here.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class com.visiontwin.app.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
