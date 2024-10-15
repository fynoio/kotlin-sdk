package io.fyno.core_java;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import androidx.core.app.NotificationManagerCompat;

import io.fyno.core_java.utils.FynoConstants;
import io.fyno.core_java.utils.FynoContextCreator;
import io.fyno.core_java.utils.FynoUtils;
import io.fyno.core_java.utils.LogLevel;
import io.fyno.core_java.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class FynoCore {
    private static final String TAG = "FynoSDK";
    private static Context appContext;
    private static SharedPreferences fynoPreferences;
    private static final FynoContextCreator contextCreator = new FynoContextCreator();
    private static final ConnectionStateMonitor networkMonitor = new ConnectionStateMonitor();

    public static void initialize(Context context, String WSId, String token) {
        networkMonitor.enable(context);

        if (WSId != null && WSId.isEmpty()) {
            throw new IllegalArgumentException("Workspace Id is empty");
        }

        appContext = context;
        contextCreator.setContext(context);
        FynoUser user = new FynoUser();
        RequestHandler request = new RequestHandler();
        fynoPreferences = context.getSharedPreferences(context.getPackageName() + "-fynoio", ContextWrapper.MODE_PRIVATE);

        setString("WS_ID", WSId);
        setString("SECRET", token);
        setString("VERSION", "live");
        user.setWorkspace(WSId);
        user.setApi(token);

        if (user.getIdentity().isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            try {
                FynoUtils fynoUtils = new FynoUtils();
                String endpoint = fynoUtils.getEndpoint("create_profile", FynoUser.getWorkspace(), FynoConstants.ENV, uuid,null, getString("VERSION"));
//                request.requestPOST(endpoint, new JSONObject().put("distinct_id", uuid), "POST");
//                identify(uuid);
                setFlag("isDirty", true);
            } catch (Exception e) {
                Logger.w(TAG, "In Exception initialize: " + e.getMessage());
            }
        }
    }

    public static void identify(String uniqueId, String name, boolean update) {
        if (!contextCreator.isInitialized()) {
            contextCreator.setContext(appContext);
        }

        String oldDistinctId = FynoUser.getIdentity();
        if (oldDistinctId.equals(uniqueId)) {
            return;
        }

        if (update) {
            if (!oldDistinctId.isEmpty()) {
                FynoUtils fynoUtils = new FynoUtils();
                String mergeEndpoint = fynoUtils.getEndpoint("merge_profile", FynoUser.getWorkspace(),FynoConstants.ENV, oldDistinctId, uniqueId, getString("VERSION"));
                RequestHandler.requestPOST(mergeEndpoint, null, "PATCH");
            }
            if(!name.isEmpty()){
            FynoUtils fynoUtils = new FynoUtils();
            String upsertEndpoint = fynoUtils.getEndpoint("upsert_profile", FynoUser.getWorkspace(),FynoConstants.ENV, uniqueId,null, getString("VERSION"));
            RequestHandler.requestPOST(upsertEndpoint, getParamsObj(uniqueId, name), "PUT");
            }

        }
        FynoUser.identify(uniqueId);
    }

    private static JSONObject getParamsObj(String uniqueId, String name) {
        if (!contextCreator.isInitialized()) {
            contextCreator.setContext(appContext);
        }

        JSONObject jsonObject = new JSONObject();
        JSONObject channelObj = new JSONObject();
        JSONArray pushObj = new JSONArray();

        try {
            jsonObject.put("distinct_id", uniqueId);
            if (name != null && !name.isEmpty()) {
                jsonObject.put("name", name);
            }

            String fcmToken = FynoUser.getFcmToken();
            String xiaomiToken = FynoUser.getMiToken();

            int notificationStatus = areNotificationPermissionsEnabled() ? 1 : 0;

            if (fcmToken != null && !fcmToken.isEmpty()) {
                pushObj.put(new JSONObject()
                        .put("token", "fcm_token:" + fcmToken)
                        .put("integration_id", FynoUser.getFcmIntegration())
                        .put("status", notificationStatus));
            }

            if (xiaomiToken != null && !xiaomiToken.isEmpty()) {
                pushObj.put(new JSONObject()
                        .put("token", "mi_token:" + xiaomiToken)
                        .put("integration_id", FynoUser.getMiIntegration())
                        .put("status", notificationStatus));
            }

            if (pushObj.length() > 0) {
                channelObj.put("push", pushObj);
                jsonObject.put("channel", channelObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static boolean areNotificationPermissionsEnabled() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);
        return notificationManager.areNotificationsEnabled();
    }

    public static void resetUser() {
        if (!contextCreator.isInitialized()) {
            contextCreator.setContext(appContext);
        }

        String uuid = UUID.randomUUID().toString();
        JSONObject deleteJson = new JSONObject();
        JSONArray deleteArr = new JSONArray();
        String fcmToken = FynoUser.getFcmToken();
        String xiaomiToken = FynoUser.getMiToken();

        if (fcmToken != null && !fcmToken.isEmpty()) {
            deleteArr.put("fcm_token:" + fcmToken);
        }

        if (xiaomiToken != null && !xiaomiToken.isEmpty()) {
            deleteArr.put("mi_token:" + xiaomiToken);
        }

        if (deleteArr.length() > 0) {
            try {
                deleteJson.put("push", deleteArr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        JSONObject jsonObject = getParamsObj(uuid, null);

        FynoUtils fynoUtils = new FynoUtils();
        String deleteEndpoint = fynoUtils.getEndpoint("delete_channel", FynoUser.getWorkspace(),FynoConstants.ENV, FynoUser.getIdentity(),null, getString("VERSION"));
        RequestHandler.requestPOST(deleteEndpoint, deleteJson, "POST");

        String createProfileEndpoint = fynoUtils.getEndpoint("create_profile", FynoUser.getWorkspace(),FynoConstants.ENV,null,null, getString("VERSION"));
        RequestHandler.requestPOST(createProfileEndpoint, jsonObject, "POST");

        identify(uuid, "", false);
    }

    public static void resetConfig() {
        if (!contextCreator.isInitialized()) {
            contextCreator.setContext(appContext);
        }

        FynoUser.setWorkspace("");
        FynoUser.setApi("");
        FynoUser.setMiIntegration("");
        FynoUser.setFcmIntegration("");
    }

    public static void saveConfig(String wsId, String apiKey, String fcmIntegration, String miIntegration) {
        if (!contextCreator.isInitialized()) {
            contextCreator.setContext(appContext);
        }

        FynoUser.setWorkspace(wsId);
        FynoUser.setApi(apiKey);
        FynoUser.setMiIntegration(miIntegration);
        FynoUser.setFcmIntegration(fcmIntegration);
    }

    public static void setLogLevel(LogLevel level) {
        Logger.setLevel(level);
    }

    public static void mergeProfile(String oldDistinctId, String newDistinctId) {
        if (!contextCreator.isInitialized()) {
            contextCreator.setContext(appContext);
        }

        if (oldDistinctId.isEmpty() || newDistinctId.isEmpty()) {
            Logger.i(TAG, "mergeProfile: Failed as Both old id and new id are required to merge profile");
            return;
        }

        if (oldDistinctId.equals(newDistinctId)) {
            Logger.i(TAG, "mergeProfile: No need to merge as both old id and new id are the same");
            return;
        }

        try {
            FynoUtils fynoUtils = new FynoUtils();
            String mergeProfileEndpoint = fynoUtils.getEndpoint("merge_profile", FynoUser.getWorkspace(),FynoConstants.ENV, oldDistinctId, newDistinctId, getString("VERSION"));
            RequestHandler.requestPOST(mergeProfileEndpoint, null, "PATCH");
            identify(newDistinctId, "", false);
        } catch (Exception e) {
            Logger.i(TAG, "mergeProfile: Failed with exception " + e.getMessage());
        }
    }

    private static void setString(String key, String value) {
        fynoPreferences.edit().putString(key, value).apply();
    }

    private static void setFlag(String key, boolean value) {
        fynoPreferences.edit().putBoolean(key, value).apply();
    }

    static String getString(String key) {
        return fynoPreferences.getString(key, "") != null ? fynoPreferences.getString(key, "") : "";
    }

    private static boolean getFlag(String key) {
        return fynoPreferences.getBoolean(key, false);
    }
}
