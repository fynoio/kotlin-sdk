package io.fyno.kotlin_sdk.utils

import android.annotation.SuppressLint
import android.content.Context
import io.fyno.kotlin_sdk.helpers.SQLDataHelper

@SuppressLint("StaticFieldLeak")
object FynoContextCreator {
    lateinit var context: Context
    val sqlDataHelper: SQLDataHelper by lazy { SQLDataHelper(context) }
    fun isInitialized(): Boolean {
        return this::context.isInitialized
    }
}