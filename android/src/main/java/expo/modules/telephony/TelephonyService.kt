package expo.modules.telephony

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.content.pm.ServiceInfo

class TelephonyService : Service() {
    companion object {
        const val CHANNEL_ID = "TelephonyServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("inputExtra") ?: "Service is running"
        createNotificationChannel()
        
        // Use a generic icon or the app's mipmap if possible. 
        // In a library, we might not know the exact resource ID, but we can try to guess or use a system icon.
        // For now, let's use a standard icon if we can't find the app's one.
        val iconRes = applicationContext.resources.getIdentifier("ic_launcher", "mipmap", applicationContext.packageName)
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Telephony Service Active")
            .setContentText(input)
            .setSmallIcon(if (iconRes != 0) iconRes else android.R.drawable.ic_dialog_info)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Telephony Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
