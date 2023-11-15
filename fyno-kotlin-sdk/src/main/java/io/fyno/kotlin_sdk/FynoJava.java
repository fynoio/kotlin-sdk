package io.fyno.kotlin_sdk;

import android.content.Context;

import io.fyno.core.FynoCore;
import io.fyno.core.FynoUser;
import io.fyno.core.utils.LogLevel;
import io.fyno.pushlibrary.FynoPush;
import io.fyno.pushlibrary.models.PushRegion;

public class FynoJava {
    static FynoCore core = new FynoCore();

    public static void initialize(Context context, String workspaceId, String token, String userId) {
        if (userId == null || userId.isEmpty()) {
            FynoCore.Companion.initialize(context, workspaceId, token, "live");
        } else {
            FynoCore.Companion.initialize(context, workspaceId, token, "live");
            identify(userId);
        }
    }

    public static void registerPush(String xiaomiApplicationId, String xiaomiApplicationKey, PushRegion pushRegion, String fcmIntegration, String miIntegration) {
        FynoPush fynoPush = new FynoPush();
        fynoPush.registerPush(xiaomiApplicationId, xiaomiApplicationKey, pushRegion, fcmIntegration);
    }

    public static void identify(String uniqueId) {
        FynoCore.Companion.identify(uniqueId, uniqueId, true);
    }

    public static void resetUser() {
        FynoCore.Companion.resetUser();
    }

    public static void resetConfig() {
        FynoCore.Companion.resetConfig();
    }

    public static void saveConfig(String wsId, String apiKey, String fcmIntegration, String miIntegration) {
        FynoCore.Companion.saveConfig(wsId, apiKey, fcmIntegration, miIntegration);
    }

    public static void setLogLevel(LogLevel level) {
        FynoCore.Companion.setLogLevel(level);
    }
}

