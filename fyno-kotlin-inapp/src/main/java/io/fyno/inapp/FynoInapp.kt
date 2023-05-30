package io.fyno.inapp

import android.content.Intent
import io.fyno.kotlin_sdk.FynoSdk
import io.fyno.kotlin_sdk.utils.FynoContextCreator

object FynoInapp {
    fun enable(user_id: String, wsid: String, signature: String, flag: Boolean, listener: InAppListener){
        val context = FynoContextCreator.context
        if(!flag)
            return
        else{
            Intent(context, FynoInappService::class.java).also { intent ->
                context.startService(intent)
                FynoInappService().initSocket(user_id, wsid, signature, listener)
            }
        }
    }
}