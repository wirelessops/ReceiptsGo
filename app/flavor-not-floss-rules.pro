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