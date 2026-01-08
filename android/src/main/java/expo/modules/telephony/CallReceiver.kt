package expo.modules.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.facebook.react.bridge.Arguments

class CallReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CallReceiver"
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state == null) return

            Log.d(TAG, "Call State: $state Number: $incomingNumber")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    lastState = TelephonyManager.EXTRA_STATE_RINGING
                    if (!incomingNumber.isNullOrEmpty()) {
                        savedNumber = incomingNumber
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // If previous state was RINGING and now IDLE, it matches a missed/rejected call pattern
                    if (lastState == TelephonyManager.EXTRA_STATE_RINGING) {
                        Log.d(TAG, "Potential Missed Call detected from: $savedNumber")
                        emitDeviceEvent(context, savedNumber)
                    }
                    lastState = TelephonyManager.EXTRA_STATE_IDLE
                    savedNumber = null
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call answered, so it's not a missed call. Clear the saved number.
                    lastState = TelephonyManager.EXTRA_STATE_OFFHOOK
                    savedNumber = null
                }
            }
        }
    }

    private fun emitDeviceEvent(context: Context, number: String?) {
        val params = Arguments.createMap().apply {
            putString("number", number)
            putString("type", "missed")
        }
        TelephonyUtils.emitDeviceEvent(context, "CallReceived", params)
    }
}
