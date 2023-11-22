package io.fyno.core_java;

import android.annotation.SuppressLint;
import android.os.Build;
import io.fyno.core_java.models.MessageStatus;
import io.fyno.core_java.RequestHandler;
import io.fyno.core_java.utils.Logger;
import org.json.JSONObject;

import java.util.Date;

public class FynoCallback {
    private final String TAG = "FYNO_CALLBACK";

    @SuppressLint("NewApi")
    public void updateStatus(String callback_url, MessageStatus status, JSONObject action) {
        try {
            JSONObject deviceState = new JSONObject();
            JSONObject message = new JSONObject();
            String[] callbackObject = callback_url.split("\\?");
            deviceState.put("brand", Build.BRAND);
            deviceState.put("deviceName", Build.DEVICE);
            deviceState.put("deviceClass", "");
            deviceState.put("manufacturer", Build.MANUFACTURER);
            deviceState.put("deviceModel", Build.MODEL);
            deviceState.put("OsVersion", Build.VERSION.SDK_INT);

            if (action != null && action.length() > 0) {
                message.put("action", action);
            }
            message.put("deviceDetails", deviceState);

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("status", status);
            postDataParams.put("eventType", "Delivery");
            postDataParams.put("timestamp", new Date());
            postDataParams.put("message", message);
            postDataParams.put("dlr_params", callbackObject[1]);
            RequestHandler.requestPOST(callback_url, postDataParams, "POST");
        } catch (Exception e) {
            Logger.w(TAG, "Unable to update message delivery status for url: " + callback_url, e);
        }
    }
}
