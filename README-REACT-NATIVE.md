# Kotlin SDK in React Native App

Inorder to use Kotlin SDK in React Native application we have to create react methods in your android source to invoke native android methods of Fyno's Kotlin SDK

Open your android project in studio and Follow below steps

Step 1 - Adding FCM Dependencies
Open your project(root-level) `build.gradle` file and add dependencies

```kotlin
repositories {
    google()
    mavenCentral()
}
dependencies {
    classpath('com.android.tools.build:gradle:8.1.1')
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    classpath("com.facebook.react:react-native-gradle-plugin")
    classpath("com.google.gms.google-services:com.google.gms.google-services.gradle.plugin:4.3.15")
}
```

Step 2 - Adding Fyno Dependencies
Open your module(app-level) `build.gradle` file and add Fyno dependencies along with fcm plugin

```kotlin
apply plugin: 'com.google.gms.google-services'

...

dependencies{
    ...
    implementation 'io.fyno.kotlin-sdk:kotlin-sdk:0.0.2'
}
```

Step 3 - Add ReactMethods in your android code
In your android `src/` create a class named FynoSDK and Implement `@ReactMethods` for initialising push and user profiling by importing `io.fyno.kotlin_sdk.FynoSdk`

```kotlin
package com.fynoreactnative
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.util.Log
import io.fyno.kotlin_sdk.FynoSdk

class FynoSDK(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
override fun getName() = "FynoSDK"

@ReactMethod
fun initiateFynoPush(integration: String) {
  this.currentActivity?.let {
    FynoSdk.initialize(it.applicationContext, "<WSID>",    "<API_KEY>")
    FynoSdk.registerPush(null,null,null, integration)
  };
}

@ReactMethod
fun identify(userId: String) {
  this.currentActivity?.let {
  FynoSdk.identify(userId)};
 }
}
```

Step 4 - Now add FynoSDK class to your `createNativeModules` method inside `ReactPackage` class

```kotlin
class FynoPackage: ReactPackage {
    override fun createViewManagers(
        reactContext: ReactApplicationContext
    ): MutableList<ViewManager<View, ReactShadowNode<*>>> = mutableListOf()

    override fun createNativeModules(
        reactContext: ReactApplicationContext
    ): MutableList<NativeModule> = listOf(FynoSDK(reactContext)).toMutableList()
}
```

Step 6 - Now in your react native code trigger the native modules of FynoSDK

```kotlin
 const {FynoSDK} = NativeModules;
  const registerPush = () => {
    FynoSDK.initiateFynoPush("<FYNO-PUSH-INTEGRATION-ID>")
  };
  const login = ()=>{
    // Your login logic
    FynoSDK.identify("<USER-DISTINCT-ID>")
  }
```

For implementing more methods refer our [Kotlin SDK docs](https://docs.fyno.io/docs/push-fyno-sdk)
