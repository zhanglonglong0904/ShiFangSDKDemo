#指定压缩级别
-optimizationpasses 5

#混淆时采用的算法
-optimizations !code/simplification/arithmetic,!field/,!class/merging/,!code/allocation/variable

#混淆时不使用大小写混合类名
-dontusemixedcaseclassnames

#打印混淆的详细信息
-verbose

-keepattributes *Annotation*,InnerClasses #保留注解不混淆
-keepattributes Signature # 避免混淆泛型
-keepattributes SourceFile,LineNumberTable  # 抛出异常时保留代码行号



#---------------------------------AI称重 SDK---------------------------------
-keep class com.shifang.** {*;}
-keep public class org.pytorch.** {*;}
-keep public class com.facebook.** {*;}
-keepclassmembers enum * {*;}
-keepclasseswithmembernames class * {
    native <methods>;
}
#*******************第三方SDK**********************#
#blankj
-keep class com.blankj.**{*;}
#okhttp3.x
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-keep class okhttp3.** { *;}
-keep class org.** { *;}
-dontwarn okio.**
#retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**
#gson
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
#fastJson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }

#*******************第三方SDK**********************#
#---------------------------------AI称重 SDK---------------------------------

