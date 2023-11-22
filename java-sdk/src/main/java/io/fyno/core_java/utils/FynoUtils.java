package io.fyno.core_java.utils;

public class FynoUtils {
    public String getEndpoint(String event, String ws, String env, String profile, String newId, String version) {
        String baseEndpoint = FynoConstants.PROD_ENDPOINT;

        if (env != null && env.equals("test")) {
            baseEndpoint = FynoConstants.DEV_ENDPOINT;
        }

        String commonPath = ws + "/" + version + "/" + FynoConstants.PROFILE;

        switch (event) {
            case "create_profile":
                return baseEndpoint + "/" + commonPath;
            case "get_profile":
            case "upsert_profile":
                return baseEndpoint + "/" + commonPath + "/" + profile;
            case "merge_profile":
                return baseEndpoint + "/" + commonPath + "/" + profile + "/merge/" + newId;
            case "update_channel":
                return baseEndpoint + "/" + commonPath + "/" + profile + "/channel";
            case "delete_channel":
                return baseEndpoint + "/" + commonPath + "/" + profile + "/channel/delete";
            case "event_trigger":
                return baseEndpoint + "/" + ws + "/" + version + "/" + FynoConstants.EVENT_PATH;
            default:
                return "";
        }
    }
}




