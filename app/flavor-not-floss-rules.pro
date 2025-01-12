

##### Our Personal Classes #####
-keep class com.wops.** { *; }
-dontwarn com.wops.**
-keep class wb.android.** { *; }
-dontwarn wb.android.**
-keep class wb.receipts.** { *; }
-dontwarn wb.receipts.**
-keep class wb.receiptspro.** { *; }
-dontwarn wb.receiptspro.**

##### AWS ProGuard configurations #####

# Class names are needed in reflection
-keepnames class com.amazonaws.**
-keepnames class com.amazonaws.** { *; }
-keepnames class com.amazon.**
# Request handlers defined in request.handlers
-keep class com.amazonaws.services.**.*Handler
# The following are referenced but aren't required to run
-dontwarn com.fasterxml.jackson.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.commons.logging.impl.**
# Android 6.0 release removes support for the Apache HTTP client
-dontwarn org.apache.http.**
-dontwarn org.apache.http.conn.scheme.**
# The SDK has several references of Apache HTTP client
-dontwarn com.amazonaws.http.**
-dontwarn com.amazonaws.metrics.**

### Crashlytics
# Keep file names and line numbers.
-keepattributes SourceFile,LineNumberTable
# Optional: Keep custom exceptions.
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**


##### FireBase ProGuard configurations #####
-keep class com.google.firebase.provider.FirebaseInitProvider

# Keep all exceptions for tracking purposes
-keep public class * extends java.lang.Exception
-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class org.codehaus.**                             { *; }

-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**

-keep class com.amazonaws.internal.**                    { *; }
-keepattributes Signature,*Annotation*,EnclosingMethod
-keepnames class com.fasterxml.jackson.** { *; }


#SDK split into multiple jars so certain classes may be referenced but not used
-dontwarn com.amazonaws.services.s3.**
-dontwarn com.amazonaws.services.sqs.**

-dontnote com.amazonaws.services.sqs.QueueUrlHandler



### Google Drive API
# Needed to keep generic types and @Key annotations accessed via reflection
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault,*Annotation*
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keep class * extends com.google.api.client.json.GenericJson {
*;
}
-keep class com.google.api.services.drive.** {
*;
}

-keep class com.google.** { *;}
-keep interface com.google.** { *;}
-dontwarn com.google.**

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


# Joda-Money has no mandatory dependencies. There is a compile-time dependency on Joda-Convert, but this is not required at runtime thanks to the magic of annotations.
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
