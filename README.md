# OXCE for Android

OXCE for Android is, as the name suggests, a port of [OXCE](https://openxcom.org/forum/index.php/topic,5251.0.html) to the
Android platform. In order to achieve this, it uses SDL2 set of libraries, and a portion of Java
code for some convenient features like auto-updating files after installing new version.

Uses [afiledialog](https://github.com/jfmdev/afiledialog) library.

# Building

In order to build OXCE for Android, you'll need:

 - Android Studio (latest stable recommended)
 - Android SDK with **compileSdk 35** and **targetSdk 36**
 - Android NDK **r30** (30.0.14904198)
 - Gradle **9.3.1** / Android Gradle Plugin **9.1.1** (handled automatically by the wrapper)

## Getting the Code

This project uses git submodules, so in order to get the code, you'll have to do the following:

1. Clone this project:

    ```
    git clone https://github.com/MeridianOXC/openxcom-android.git
    cd openxcom-android
    git checkout oxce-plus-proto
    ```

2. Get submodules:

    ```
    git submodule init
    git submodule update
    ```

3. The SDK and NDK paths are detected automatically by Android Studio. If building from the
command line, create a `local.properties` file in the project root:

    ```
    sdk.dir=/path/to/Android/sdk
    ```

   The NDK version is pinned in `app/build.gradle` (`ndkVersion`) and will be downloaded
   automatically by the SDK manager if not already installed.

# Building the app

Run the Gradle wrapper with the `assembleDebug` or `assembleRelease` task:

    $ ./gradlew assembleRelease

The resulting .apk will be in `app/build/outputs/apk/` folder.

You might also include additional data in the apk itself. Everything you put in
`app/src/main/jni/OpenXcom/bin` subdirectories will be packed as assets and automatically
installed on first launch. Note that redistribution of such builds may be illegal in some
countries and should only be used for debugging purposes.

## NDK compatibility notes

The following NDK-level compatibility shims are in place (see commit history for details):

- **libc++ 18 / NDK r27+** — `char_traits<signed char>` and `char_traits<unsigned int>` were
  removed from libc++ 18. A compat header (`libcxx18_char_traits_compat.h`) is force-included
  for all C++ translation units.
- **NDK r28+** — `-Wincompatible-function-pointer-types` is now an error; suppressed for the
  SDL2 target where the GL ES2 function table has a benign const-qualifier mismatch.
- **NDK r30** — `ALooper_pollAll` was removed; replaced with `ALooper_pollOnce` (identical
  signature, same semantics for a zero-timeout non-blocking poll).

## Android 11+ storage permissions

The app writes game data to external storage (`/sdcard/openxcom/`). On Android 11 and later
(API 30+), `WRITE_EXTERNAL_STORAGE` has no effect. The app requests the
`MANAGE_EXTERNAL_STORAGE` ("All files access") permission at first launch via a system
settings screen. This permission must be granted for the preloader to extract bundled assets.

# Credits

This is a fork of sfalexrog's repository of OpenXcom for Android and most of the actual work has been done by him.

I (Meridian) have only copied and customized his work on this build system.

Many thanks also to Stoddard for helping with all aspects of the project.
