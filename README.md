## Overview

![Fyno: Fire your notifications](https://fynodev.s3.ap-south-1.amazonaws.com/others/Fyno_Banner.jpeg)
Fyno Android SDK allows you to track your notification delivery

<div style="display: flex;gap:10px"><img src="https://app.dev.fyno.io/images/fyno-logo.svg" width="50"><h1> Fyno - Android Push Notification Plugin</h1></div>

Fyno Android SDK handles push amplification, notification customization, and tracks your notification delivery

## 1. Prerequisites

### 1.1 Firebase Setup:

    Setup Firebase and create application in Firebase Console. Please refer [FCM Documentation](https://docs.fyno.io/docs/push-fcm) for more details

### 1.2 Xiaomi Setup:

    Setup _Xiaomi Developer Account_ and create application in Xiaomi push console. Please refer [Mi Push Documentation](https://docs.fyno.io/docs/push-mi-push) for more details.

### 2. Configuration

    Configure Fyno Push provider in [Fyno App](https://app.fyno.io/integrations)

## 2. Installation

### Step 1: Add maven { url 'https://jitpack.io' } repository to your root build gradle.

```kotlin
// place the below snippet in your project gradle file
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add Android dependencies inside app build gradle

Add following line inside dependencies {} in app build gradle

```kotlin
implementation 'com.github.fynoio:kotlin-sdk:1.0.0'
```

<sup>[check the latest version here](https://jitpack.io/#fynoio/kotlin-sdk)</sup>

## 3. Initilization

### Step 1: To integrate Fyno Andreoid SDK initilize Fyno SDK in MainActivity/MainApplication inside `onCreate` method

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        ...
        FynoSdk.initialize(context, workspace_id, api_key)
        super.onCreate()
    }
}
//Initilize
FynoSdk.initialize(context, Workspace_Id, Api_Key)

//Register for push
FynoSdk.registerPush("Xiaomi_App_Id", "Xiaomi_App_Key", "Xiaomi_Push_Region", "Fyno_Integration_Id")

//Identify user on login
FynoSdk.identify("Distinct_Id")
```

> **_NOTE:_** You can also use individual SDK's for communication with Fyno. Refer below section to know more

## Fyno Core

Fyno Core initilizes the context between your andriod application and Fyno, This has all the required configurations needed for FynoSDK, FynoPush, FynoCallbacks to work and helps with profiling the users

### Adding Dependencies

Add following line inside dependencies {...} in app build gradle

```kotlin
dependencies{
    ...
    implementation 'com.github.fynoio.kotlin-sdk:core:1.0.0'
}
```

### Initilization

Fyno Core SDK is responsible for initilize the context to save Fyno configurations and user profiling.
<br/> 1. To initilize Fyno sdk you have to call `FynoCore.initilize(this, "WSID", "APIKEY")`
<br/> 2. On user login to create a user profile call `FynoUser.identify("Distinct_Id")

## Fyno Push

Fyno push handles incoming push notification from fyno and renders the customized push message with diffrent templates as configured in Fyno platform, and also this library takes care of push amplification if enabled.

### Adding Dependencies

Add following line inside dependencies {} in app build gradle

```kotlin
dependencies{
    ...
    implementation 'com.github.fynoio.kotlin-sdk:pushlibrary:1.0.0'
}
```

Download the google-services.json from Firebase console and place it in the root folder as per [FCM Documentations](https://firebase.google.com/docs/android/setup)

### Initilization

Fyno Push SDK is responsible for handling push rendering and push amplification.
<br/> 1. To initilize Fyno Push you are required to use `FynoCore` you have to call `FynoCore.initilize(this, "WSID", "APIKEY")`
<br/> 2. To initilize push call `FynoPush.registerPush` method
<br/> 2.1 Push Amplification: To make use of push amplification Follow [Mi Push Documentation](https://docs.fyno.io/docs/push-mi-push) to implement Xiaomi push

```
//register push
FynoPush.registerPush("Xiaomi_App_Id", "Xiaomi_App_Key", "Xiaomi_Push_Region", "Fyno_Integration_Id")
//register push without amplification
FynoPush.registerFcm("Fyno_Integration_id")
```

## Fyno Callback

If you have your custom implementation of any push provider and do not want to use FynoSDK/FynoPush library then you can use this library to notify message delivery to Fyno. This library provides a method to update the stauts which needs to be called on respective provider's handler methods with the message Status

### Adding Dependencies

Add following line inside dependencies {} in app build gradle

```kotlin
dependencies{
    ...
    implementation 'com.github.fynoio.kotlin-sdk:callback:1.0.0'
}
```

### Initilization

Fyno Push SDK is responsible for sending push callbacks to Fyno by calling `FynoCallback.updateStatus` method.

above method required 2 required parameters and 1 optional parameter
`callback_url - Required parameter - You can get the callback_url from the notification additional payload if the notification triggered from Fyno.`
`status - Required parameter - Status of the push notification to be notified to Fyno.`

```
//register push
FynoPush.registerPush("Xiaomi_App_Id", "Xiaomi_App_Key", "Xiaomi_Push_Region", "Fyno_Integration_Id")
//register push without amplification
FynoPush.registerFcm("Fyno_Integration_id")
```
