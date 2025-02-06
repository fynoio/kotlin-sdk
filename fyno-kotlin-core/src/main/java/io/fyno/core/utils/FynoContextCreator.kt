package io.fyno.core.utils

import android.content.Context
import android.util.Log
import io.fyno.core.helpers.SQLDataHelper
import java.lang.ref.WeakReference

object FynoContextCreator {
    private var contextRef: WeakReference<Context>? = null
    private var _sqlDataHelper: SQLDataHelper? = null

    val sqlDataHelper: SQLDataHelper?
        get() {
            if (_sqlDataHelper == null) {
                Logger.w("FYNO_CONTEXT_CREATOR", "SQLDataHelper is accessed before initialization.")
                return null
            }
            return _sqlDataHelper!!
        }

    fun setContext(context: Context) {
        contextRef = WeakReference(context)
        _sqlDataHelper = SQLDataHelper(context)
    }

    fun getContext(): Context? {
        return contextRef?.get()
    }

    fun isInitialized(): Boolean {
        Log.w("FYNO_CONTEXT_CREATOR", "Is Fyno context initialized: ${contextRef?.get()} & Is SQLDataHelper initialized $_sqlDataHelper", )
        return (contextRef?.get() != null && _sqlDataHelper != null)
    }
}