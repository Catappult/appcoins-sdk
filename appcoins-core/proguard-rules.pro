# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Web3j
-keep class org.web3j.** { *; }

# RxJava 1
# https://github.com/artem-zinnatullin/RxJavaProGuardRules/blob/master/rxjava-proguard-rules/proguard-rules.txt

-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontnote rx.internal.util.PlatformDependent

# AppCoins SDK
-keep class com.asf.appcoins.** { *; }

# EthereumJ
-keep class ethereumj.** { *; }

# DeepEquals
-keep class cedarsoftware.util.DeepEquals
-keep class cedarsoftware.util.ReflectionUtils

# Okio
-dontwarn okio.DeflaterSink
-dontwarn okio.Okio

# slf4j
-dontwarn org.slf4j.**

# Jackson
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility {
        public static final com.fasterxml.jackson.annotation.JsonAutoDetect$Visibility *;
}

# Javax annotations
-dontwarn javax.annotation.**

# Spongy Castle
-keep class org.spongycastle.** { *; }
-dontwarn org.spongycastle.**

# Lambda expressions
-dontwarn java.lang.invoke**
