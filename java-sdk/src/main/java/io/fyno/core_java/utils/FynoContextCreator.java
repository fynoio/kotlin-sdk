package io.fyno.core_java.utils;
import android.content.Context;
import io.fyno.core_java.helpers.SQLDataHelper;

public final class FynoContextCreator {
    public Context context;
    public static SQLDataHelper sqlDataHelper;

    public FynoContextCreator() {

    }



    public void setContext(Context context) {
        this.context = context;
        FynoContextCreator.sqlDataHelper = new SQLDataHelper(context);
    }

    public boolean isInitialized() {
        return this.context != null;
    }
}