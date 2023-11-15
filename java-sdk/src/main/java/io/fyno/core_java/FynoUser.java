package io.fyno.core_java;

import io.fyno.core_java.helpers.Config;
import io.fyno.core_java.utils.FynoConstants;
import io.fyno.core_java.utils.FynoContextCreator;
import io.fyno.core_java.utils.FynoUtils;
import io.fyno.core_java.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class FynoUser {
    private static final String TAG = "FynoUser";

    private static void updatePush(String tokenType, String token) {
        if (!getIdentity().isEmpty() && !getWorkspace().isEmpty()) {
                try {
                    String ws = getWorkspace();
                    String profile = getIdentity();
                    String endpoint = new FynoUtils().getEndpoint(
                            "update_channel",
                            ws,
                            FynoConstants.ENV,
                            getIdentity(),
                            null,
                            FynoCore.getString("VERSION")
                    );

                    int notificationStatus = FynoCore.areNotificationPermissionsEnabled() ? 1 : 0;
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("channel", new JSONObject().put("push", new JSONArray()
                            .put(new JSONObject()
                                    .put("token", tokenType + ":" + token)
                                    .put("integration_id", getIntegrationId(tokenType))
                                    .put("status", notificationStatus)
                            )
                    ));

                    RequestHandler.requestPOST(endpoint, requestBody, "PATCH");

                    FynoContextCreator.sqlDataHelper.insertConfigByKey(
                            new Config("fyno_" + tokenType + "_token", token)
                    );
                } catch (Exception e) {
                    Logger.i(TAG, "Exception in set" + tokenType.toUpperCase() + "Token: " + e.getMessage());
                }
        }
    }

    private static void updateChannel(String channel, String token) {
        if (!getIdentity().isEmpty() && !getWorkspace().isEmpty()) {
            try {
                String endpoint = new FynoUtils().getEndpoint(
                        "update_channel",
                        getWorkspace(),
                        FynoConstants.ENV,
                        getIdentity(),
                        null,
                        FynoCore.getString("VERSION")
                );

                JSONObject requestBody = new JSONObject();
                requestBody.put("channel", new JSONObject().put(channel, token));

                RequestHandler.requestPOST(endpoint, requestBody, "PATCH");

                FynoContextCreator.sqlDataHelper.insertConfigByKey(
                        new Config("fyno_" + channel, token)
                );
            } catch (Exception e) {
                Logger.i(TAG, "Exception in set" + channel + ": " + e.getMessage());
            }
        }
    }

    private static String getToken(String tokenType) {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_" + tokenType + "_token").getValue();
    }

    private static String getIntegrationId(String tokenType) {
        if ("fcm_token".equals(tokenType)) {
            return getFcmIntegration();
        } else if ("mi_token".equals(tokenType)) {
            return getMiIntegration();
        } else {
            return "";
        }
    }

    public static void identify(String distinctId) {
        FynoContextCreator.sqlDataHelper.insertConfigByKey(new Config("fyno_distinct_id", distinctId));
    }

    public static String getIdentity() {
        String distinctId = FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_distinct_id").getValue();
        return distinctId;
    }

    public static void setFcmToken(String token) {
        updatePush("fcm_token", token);
    }

    public static String getFcmToken() {
        return getToken("fcm_token");
    }

    public static void setMiToken(String token) {
        updatePush("mi_token", token);
    }

    public static String getMiToken() {
        return getToken("mi_token");
    }

    public static void setApnsToken(String token) {
        updatePush("apns_token", token);
    }

    public static String getApnsToken() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_apns_token").getValue();
    }

    public static void setEmail(String email) {
        updateChannel("email", email);
    }

    public static String getEmail() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_email").getValue();
    }

    public static void setMobile(String mobile) {
        updateChannel("sms", mobile);
    }

    public static String getMobile() {
        return getToken("mobile");
    }

    public static void setWhatsapp(String mobile) {
        updateChannel("whatsapp", mobile);
    }

    public static String getWhatsapp() {
        return getToken("wa_mobile");
    }

    public static void setWorkspace(String workspaceId) {
        FynoContextCreator.sqlDataHelper.insertConfigByKey(new Config("fyno_wsid", workspaceId));
    }

    public static String getWorkspace() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_wsid").getValue();
    }

    public static void setFynoIntegration(String integrationId) {
        FynoContextCreator.sqlDataHelper.insertConfigByKey(new Config("fyno_integration_id", integrationId));
    }

    public static String getFynoIntegration() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_integration_id").getValue();
    }

    public static void setFcmIntegration(String integrationId) {
        FynoContextCreator.sqlDataHelper.insertConfigByKey(new Config("fyno_fcm_integration_id", integrationId));
    }

    public static String getFcmIntegration() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_fcm_integration_id").getValue();
    }

    public static void setMiIntegration(String integrationId) {
        FynoContextCreator.sqlDataHelper.insertConfigByKey(new Config("fyno_mi_integration_id", integrationId));
    }

    public static String getMiIntegration() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_mi_integration_id").getValue();
    }

    public static void setApi(String secret) {
        FynoContextCreator.sqlDataHelper.insertConfigByKey(new Config("fyno_ws_secret", secret));
    }

    public static String getApi() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_ws_secret").getValue();
    }

    public static void setUserName(String name) {
        FynoContextCreator.sqlDataHelper.insertConfigByKey(new Config("fyno_user_name", name));
    }

    public static String getUserName() {
        return FynoContextCreator.sqlDataHelper.getConfigByKey("fyno_user_name").getValue();
    }
}
