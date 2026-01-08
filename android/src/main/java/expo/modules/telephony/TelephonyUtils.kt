package expo.modules.telephony

import android.content.Context
import android.util.Log
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

object TelephonyUtils {
    private const val TAG = "TelephonyUtils"

    fun emitDeviceEvent(context: Context, eventName: String, params: WritableMap?) {
        try {
            val reactApp = context.applicationContext as? ReactApplication
            if (reactApp == null) {
                Log.w(TAG, "ReactApplication is null")
                return
            }

            var reactContext: ReactContext? = null

            // Try getting context from ReactNativeHost (Classic/Hybrid)
            // Using explicit getters to avoid Kotlin property inference issues with platform types
            try {
                reactContext = reactApp.reactNativeHost.reactInstanceManager.currentReactContext
            } catch (e: Exception) {
                Log.e(TAG, "Error getting context from ReactNativeHost", e)
            }

            // If null, try ReactHost (New Arch)
            // Note: Check if ReactApplication has a reactHost property/getter in this RN version
            if (reactContext == null) {
                try {
                    // Using reflection or checking for the getter might be safer if types are not resolving
                    // but for now, let's try the standard property access and ensure cast is correct
                    val host = reactApp.javaClass.getMethod("getReactHost").invoke(reactApp)
                    if (host != null) {
                        val method = host.javaClass.getMethod("getCurrentReactContext")
                        reactContext = method.invoke(host) as? ReactContext
                    }
                } catch (e: Exception) {
                    // Fallback to internal field if necessary or just log
                    Log.d(TAG, "ReactHost getter not found or failed - this is expected on older RN versions")
                }
            }

            if (reactContext != null) {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                    .emit(eventName, params)
                Log.d(TAG, "Event $eventName emitted to React Native")
            } else {
                Log.w(TAG, "ReactContext is null - App might be in background/killed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error emitting event", e)
        }
    }
}
