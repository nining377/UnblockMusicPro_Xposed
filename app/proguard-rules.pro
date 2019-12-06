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

-keep public class com.raincat.unblockmusicpro.HTTPHook
-keep public class android.app.**
-keepclassmembernames class com.raincat.unblockmusicpro.MainActivity {
    boolean isModuleActive();
}

-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn com.google.common.util.concurrent.FuturesGetChecked**
-dontwarn javax.lang.model.element.Modifier
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn android.app.**

#混淆变量和函数
-obfuscationdictionary proguard-class.txt
#混淆类名
-classobfuscationdictionary proguard-class.txt
# 指定class
-packageobfuscationdictionary proguard-class.txt
# 将包里的类混淆成n个再重新打包到一个统一的package中  会覆盖flattenpackagehierarchy选项
-repackageclasses com.raincat.unblockmusicpro
# 删除日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int d(...);
    public static int w(...);
    public static int v(...);
    public static int i(...);
}