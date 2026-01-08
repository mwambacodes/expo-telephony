package expo.modules.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import com.facebook.react.bridge.Arguments

class SmsReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")

        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                try {
                    val pdus = bundle.get("pdus") as? Array<*> ?: return
                    val format = bundle.getString("format")
                    
                    for (pdu in pdus) {
                        val msg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            SmsMessage.createFromPdu(pdu as ByteArray, format)
                        } else {
                            @Suppress("DEPRECATION")
                            SmsMessage.createFromPdu(pdu as ByteArray)
                        }
                        
                        val msgFrom = msg?.originatingAddress ?: "Unknown"
                        val msgBody = msg?.messageBody ?: ""

                        Log.d(TAG, "SMS received from: $msgFrom, body: $msgBody")

                        // Emit event to React Native
                        val params = Arguments.createMap().apply {
                            putString("originatingAddress", msgFrom)
                            putString("body", msgBody)
                        }
                        TelephonyUtils.emitDeviceEvent(context, "SmsReceived", params)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing SMS", e)
                }
            }
        }
    }
}
