package expo.modules.telephony

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TelephonyModule : Module() {
    private val moduleScope = CoroutineScope(Dispatchers.Main)

    override fun definition() = ModuleDefinition {
        Name("ExpoTelephony")

        Events("SmsReceived", "CallReceived")

        AsyncFunction("startService") { message: String ->
            val context = appContext.reactContext ?: return@AsyncFunction "ERROR: NO_CONTEXT"
            val serviceIntent = Intent(context, TelephonyService::class.java).apply {
                putExtra("inputExtra", message)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            "SERVICE_STARTED"
        }

        AsyncFunction("stopService") {
            val context = appContext.reactContext ?: return@AsyncFunction "ERROR: NO_CONTEXT"
            val serviceIntent = Intent(context, TelephonyService::class.java)
            context.stopService(serviceIntent)
            "SERVICE_STOPPED"
        }

        AsyncFunction("sendSms") { phoneNumber: String, message: String ->
            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    appContext.reactContext?.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
                "SMS Sent Successfully"
            } catch (e: Exception) {
                throw Exception("SMS_SEND_FAILED: ${e.message}")
            }
        }

        AsyncFunction("getMissedCalls") { limit: Int ->
            val result = mutableListOf<Map<String, Any>>()
            val context = appContext.reactContext ?: return@AsyncFunction result

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                throw Exception("PERMISSION_DENIED: READ_CALL_LOG permission required")
            }

            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                "${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.MISSED_TYPE.toString()),
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                var count = 0

                while (it.moveToNext() && count < limit) {
                    val number = it.getString(numberIndex)
                    val date = it.getLong(dateIndex)
                    val call = mapOf(
                        "number" to (number ?: "Unknown"),
                        "date" to date.toDouble()
                    )
                    result.add(call)
                    count++
                }
            }
            result
        }

        AsyncFunction("sendUssd") { ussdCode: String, promise: Promise ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val telephonyManager = appContext.reactContext?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                
                if (telephonyManager == null) {
                    promise.reject("TELEPHONY_SERVICE_NOT_AVAILABLE", "Telephony service not available", null)
                } else if (ContextCompat.checkSelfPermission(appContext.reactContext!!, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    promise.reject("PERMISSION_DENIED", "CALL_PHONE permission required", null)
                } else {
                    moduleScope.launch {
                        try {
                            val response = executeUssdInternal(telephonyManager, ussdCode)
                            promise.resolve(response)
                        } catch (e: Exception) {
                            promise.reject("USSD_FAILED", e.message, e)
                        }
                    }
                }
            } else {
                promise.reject("UNSUPPORTED_VERSION", "USSD sending requires Android O or higher", null)
            }
        }
    }

    private suspend fun executeUssdInternal(telephonyManager: TelephonyManager, ussdCode: String): String = suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val callback = object : TelephonyManager.UssdResponseCallback() {
                override fun onReceiveUssdResponse(telephonyManager: TelephonyManager, request: String, response: CharSequence) {
                    continuation.resume(response.toString())
                }

                override fun onReceiveUssdResponseFailed(telephonyManager: TelephonyManager, request: String, failureCode: Int) {
                    continuation.resumeWithException(Exception("Failure code $failureCode"))
                }
            }
            telephonyManager.sendUssdRequest(ussdCode, callback, Handler(Looper.getMainLooper()))
        } else {
            continuation.resumeWithException(Exception("UNSUPPORTED_VERSION"))
        }
    }
}
