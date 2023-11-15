package io.fyno.core_java;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import io.fyno.core_java.utils.FynoContextCreator;
import io.fyno.core_java.utils.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestHandler {
    private static final String TAG = "FynoRequest";
    static FynoContextCreator contextCreator = new FynoContextCreator();
    FynoCore core = new FynoCore();
    private static final int TIMEOUT = 3000;
    private static final long MAX_BACKOFF_DELAY = 60000;
    private static final int MAX_RETRIES = 3;

    // Queue for offline requests
    private static final LinkedList<Request> offlineRequestsQueue = new LinkedList<>();

    // Network callback to listen for connectivity changes
    private ConnectivityManager.NetworkCallback networkCallback = null;

    private static final ExecutorService requestProcessor = Executors.newFixedThreadPool(1);

    private static final AtomicInteger retries = new AtomicInteger(0);

    public static class Request {
        private final String url;
        private final JSONObject postData;
        private final String method;

        public Request(String url, JSONObject postData, String method) {
            this.url = url;
            this.postData = postData;
            this.method = method;
        }
    }

    public static void requestPOST(String r_url, JSONObject postDataParams, String method) {
        Request request = new Request(r_url, postDataParams, method);
        if (contextCreator.isInitialized()) {
            if (isNetworkConnected(contextCreator.context)) {
                processOfflineRequests();
                sendRequest(request);
            } else {
                offlineRequestsQueue.add(request);
            }
        } else {
            sendRequest(request);
        }
    }

//    private void sendRequest(Request request) {
//        Logger.i(TAG, "requestPOST: Started processing " + request.url);
//        requestProcessor.execute(() -> handleRetries(request));
//    }

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
        } else {
            // Fallback for older Android versions
            @SuppressWarnings("deprecation")
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    private static void handleRetries(Request request) {
        int retryCount = retries.get();
        if (retryCount < MAX_RETRIES) {
            try {
                doRequest(request);
            } catch (Exception e) {
                Logger.i(TAG, "Request failed: " + e.getMessage());
                long delayMillis = calculateDelay(retryCount);
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ex) {
                    Logger.e(TAG, "Sleep interrupted: " + ex.getMessage());
                }
                retries.incrementAndGet();
                handleRetries(request);
            }
        } else {
            Logger.w(TAG, "Max retries reached for request: " + request.url);
            offlineRequestsQueue.add(request);
        }
    }

    private static long calculateDelay(int retryCount) {
        return Math.min((long) Math.pow(2, retryCount), MAX_BACKOFF_DELAY);
    }

    private static void doRequest(Request request) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(request.url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            if (contextCreator.isInitialized()) {
                conn.setRequestProperty("Authorization", "Bearer " + FynoUser.getApi());
            }
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestMethod(request.method);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            if (request.postData != null) {
                OutputStream os = conn.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                writer.write(request.postData.toString());
                writer.flush();
                writer.close();
                os.close();
            }

            int responseCode = conn.getResponseCode();

            Logger.d("RequestPost", "requestPOST method = " + request.method + " url = " + request.url + ": " + conn.getResponseMessage());

            if (responseCode >= 200 && responseCode < 300) {
                Logger.i("RequestPost", "requestPOST: " + conn.getResponseMessage());
            } else if (responseCode >= 400 && responseCode < 500) {
                Logger.i(TAG, "Request failed with response code: " + responseCode);
            } else {
                Logger.i(TAG, "Request failed with response code: " + responseCode);
                throw new Exception("Request failed with response code: " + responseCode);
            }
        } finally {
            if (conn != null) {
                conn.getInputStream().close();
                conn.disconnect();
            }
        }
    }

    static void processOfflineRequests() {
        while (!offlineRequestsQueue.isEmpty()) {
            Request request = offlineRequestsQueue.poll();
            sendRequest(request);
        }
    }
    private static void sendRequest(final Request request) {
        final CountDownLatch latch = new CountDownLatch(1);
        requestProcessor.execute(() -> {
            handleRetries(request);
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Logger.e(TAG, "Thread interrupted while sending the request: " + e.getMessage());
        }
    }
}
