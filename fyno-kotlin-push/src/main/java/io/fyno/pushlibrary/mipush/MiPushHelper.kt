package io.fyno.pushlibrary.mipush

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.xiaomi.mipush.sdk.*
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus
import io.fyno.core.FynoUser
import io.fyno.pushlibrary.helper.NotificationHelper
import io.fyno.pushlibrary.helper.NotificationHelper.isFynoMessage
import io.fyno.pushlibrary.helper.NotificationHelper.rawMessage
import org.json.JSONObject


class MiPushHelper : PushMessageReceiver() {
    private var mRegId: String? = null
    private val mResultCode: Long = -1
    private val mReason: String? = null
    private val mCommand: String? = null
    private var mMessage: String? = null
    private var mTopic: String? = null
    private var mAlias: String? = null
    private var mUserAccount: String? = null
    private var mStartTime: String? = null
    private var mEndTime: String? = null

    //Process transparent messages
    override fun onReceivePassThroughMessage(context: Context, message: MiPushMessage) {
        mMessage = message.content
        if (!TextUtils.isEmpty(message.topic)) {
            mTopic = message.topic
        } else if (!TextUtils.isEmpty(message.alias)) {
            mAlias = message.alias
        } else if (!TextUtils.isEmpty(message.userAccount)) {
            mUserAccount = message.userAccount
        }
        Log.d("MIReceived", "onReceivePassThroughMessage: ${mMessage.toString()} $mAlias and $mUserAccount")
        if(message.isFynoMessage())
            NotificationHelper.renderXiaomiMessage(context, message.rawMessage())
    }

    //Process the clicks of customized notification messages
    override fun onNotificationMessageClicked(context: Context, message: MiPushMessage) {
        mMessage = message.content
        if (!TextUtils.isEmpty(message.topic)) {
            mTopic = message.topic
        } else if (!TextUtils.isEmpty(message.alias)) {
            mAlias = message.alias
        } else if (!TextUtils.isEmpty(message.userAccount)) {
            mUserAccount = message.userAccount
        }
        val data = JSONObject(message.content)
//        FynoSdk.updateStatus(data.get("callback") as String, MessageStatus.CLICKED)
    }

    override fun onNotificationMessageArrived(context: Context, message: MiPushMessage) {
        Log.d("MIReceived", "onReceivePushMessage: $message")
        if(message.isFynoMessage())
        NotificationHelper.renderXiaomiMessage(context, message.rawMessage())
        else {
            FynoCallback().updateStatus(JSONObject(message.content).getString("callback"),
                MessageStatus.RECEIVED)
            return
        }
    }

    override fun onCommandResult(context: Context, message: MiPushCommandMessage) {
        val command = message.command
        val arguments = message.commandArguments
        val cmdArg1 = if (arguments != null && arguments.size > 0) arguments[0] else null
        val cmdArg2 = if (arguments != null && arguments.size > 1) arguments[1] else null
        if (MiPushClient.COMMAND_REGISTER == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mRegId = cmdArg1
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mAlias = cmdArg1
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mAlias = cmdArg1
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mTopic = cmdArg1
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mTopic = cmdArg1
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mStartTime = cmdArg1
                mEndTime = cmdArg2
            }
        }
    }

    override fun onReceiveRegisterResult(context: Context, message: MiPushCommandMessage) {
        val command = message.command
        val arguments = message.commandArguments
        val cmdArg1 = if (arguments != null && arguments.size > 0) arguments[0] else null
        val cmdArg2 = if (arguments != null && arguments.size > 1) arguments[1] else null
        if (MiPushClient.COMMAND_REGISTER == command) {
            if (message.resultCode == ErrorCode.SUCCESS.toLong()) {
                mRegId = cmdArg1
                Log.d(TAG, "onReceiveRegisterResult: $mRegId")
                mRegId?.let { FynoUser.setMiToken(it) }
            }
        }
    }

    companion object {
        const val TAG = "FYNO_XIAOMI_PUSH"
    }
}