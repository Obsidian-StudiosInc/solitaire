# Solitare

A fork of the famous ad free 
[Solitaire Game Suite from Ken Magic] (https://code.google.com/archive/p/solitaire-for-android/)
The idea behind this fork is to update the app for higher resolution 
displays, phones without menu buttons, and keep it in sync with current 
Android version.

This is a very early fork, contributions are welcome!

## Build Instructions

To build you need the following

* JDK 1.8 or newer
* Gradle 2.14.1 or newer
* Android SDK 23 or newer ( update version in app/build.gradle)
* Android Build Tools 23.0.1 or newer ( update version in app/build.gradle)
* Android Support Repository 38
* Google Repository 36

The easiest way to build is by using Android Studio, if you have it 
installed. It comes with Gradle, a JDK, and SDK Manager for the 
installation of the Android SDKs and build tools, emulator and other 
resources. You can use the Build menu to generate an APK for use.

If building via cli, the dependencies will need to be installed and 
available for build.

To build via command line use gradle warpper in the project root 
directory, and make sure it is executable.

```shell
chmod 775 gradlew
./gradlew build
```

This will generate a debug apk to install
```shell
app/build/outputs/apk/app-debug.apk
```

More will be added as things progress.
Till then, that's all for now folks...
