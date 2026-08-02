package com.giriraj.cabalert

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern

class CabAccessibilityService : AccessibilityService() {

    private var alreadyAlerted = false
    private val pattern = Pattern.compile("Arriving in (\\d+) min", Pattern.CASE_INSENSITIVE)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val prefs = getSharedPreferences("cab_alert_prefs", Context.MODE_PRIVATE)
        val targetPackage = prefs.getString("target_package", "com.routemetic.driver")
        val threshold = prefs.getInt("threshold_minutes", 3)

        if (event == null || event.packageName != targetPackage) return

        val root = rootInActiveWindow ?: return
        val text = collectText(root)
        root.recycle()

        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            val minutes = matcher.group(1)?.toIntOrNull() ?: return
            if (minutes <= threshold) {
                if (!alreadyAlerted) {
                    fireAlert(minutes)
                    alreadyAlerted = true
                }
            } else {
                alreadyAlerted = false
            }
        }
    }

    private fun collectText(node: AccessibilityNodeInfo, builder: StringBuilder = StringBuilder()): String {
        node.text?.let { builder.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                collectText(child, builder)
                child.recycle()
            }
        }
        return builder.toString()
    }

    private fun fireAlert(minutesLeft: Int) {
        val channelId = "cab_alert_channel"
        val nm = getSystemService(NotificationManager::class.java)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val channel = NotificationChannel(
            channelId,
            "Cab Arrival Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        nm.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cab arriving soon!")
            .setContentText("Your cab is about $minutesLeft min away — get ready.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        nm.notify(1001, notification)

        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onInterrupt() {}
}
