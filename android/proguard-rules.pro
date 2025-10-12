# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-verbose

-dontwarn android.support.**
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreetypeBuild

-keep class com.badlogic.gdx.controllers.android.AndroidControllers

-keepclassmembers class com.badlogic.gdx.backends.android.AndroidInput* {
   <init>(com.badlogic.gdx.Application, android.content.Context, java.lang.Object, com.badlogic.gdx.backends.android.AndroidApplicationConfiguration);
}

-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
   boolean contactFilter(long, long);
   void    beginContact(long);
   void    endContact(long);
   void    preSolve(long, long);
   void    postSolve(long, long);
   boolean reportFixture(long);
   float   reportRayFixture(long, float, float, float, float, float);
}

# Сохранение файла деобфускации
-printmapping mapping.txt

# Игнорирование desktop-специфичных классов
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.imageio.**
-dontwarn javax.sound.**
-dontwarn org.lwjgl.opengles.**
-dontwarn com.apple.eio.**

# Сохранение важных классов для игр
-keep class ru.schneider_dev.tronfg.** { *; }
-keep class com.badlogic.gdx.** { *; }
-keep class com.boontaran.** { *; }

# Дополнительная защита для методов физики и ограничения скорости
-keepclassmembers class ru.schneider_dev.tronfg.levels.Level {
    private void checkAndLimitPlayerSpeed();
    private void updateJumpCooldowns(float);
    private void resetJumpCooldowns();
    private void updateWorld(float);
}

-keepclassmembers class ru.schneider_dev.tronfg.player.Player {
    private void limitRoverSpeed();
    public void jumpBack(float);
    public void jumpForward(float);
    public void onKey(boolean, boolean);
}

# Сохранение констант физики
-keepclassmembers class ru.schneider_dev.tronfg.levels.Level {
    public static final float WORLD_SCALE;
}

-keepclassmembers class ru.schneider_dev.tronfg.Setting {
    public static final float GRAVITY;
    public static final float WHEEL_TORQUE;
}

# Сохранение нативных методов
-keepclasseswithmembernames class * {
    native <methods>;
}

# Сохранение enum'ов
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Игнорирование отсутствующих классов
-ignorewarnings

# Оптимизации для поддержки страниц памяти 16 КБ
# Оптимизация размера страниц памяти
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Оптимизация памяти для игровых объектов
-keepclassmembers class ru.schneider_dev.tronfg.** {
    private static final int MEMORY_PAGE_SIZE;
    private static final boolean MEMORY_OPTIMIZATION;
}

# Оптимизация нативных библиотек
-keep class **.NativeLib { *; }
-keepclassmembers class **.NativeLib {
    native <methods>;
}

# Оптимизация для эффективного использования памяти
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Оптимизация строк для экономии памяти
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5

# Сохранение важных методов для оптимизации памяти
-keepclassmembers class ru.schneider_dev.tronfg.** {
    public void optimizeMemory();
    public void setMemoryPageSize(int);
    public int getMemoryPageSize();
}
