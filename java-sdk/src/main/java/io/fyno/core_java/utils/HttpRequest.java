package io.fyno.core_java.utils;

import org.json.JSONObject;

public class HttpRequest {
    private String url;
    private JSONObject postData;
    private String method;
    private int maxRetries;
    private int retryCount;

    public HttpRequest(String url, JSONObject postData, String method, int maxRetries) {
        this.url = url;
        this.postData = postData;
        this.method = method;
        this.maxRetries = maxRetries;
        this.retryCount = 0;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject getPostData() {
        return postData;
    }

    public void setPostData(JSONObject postData) {
        this.postData = postData;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}