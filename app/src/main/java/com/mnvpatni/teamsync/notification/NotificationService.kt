package com.mnvpatni.teamsync.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.SplashScreen
import java.util.Objects

class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        pushNotification(
            Objects.requireNonNull(message.notification!!).title, message.notification!!.body
        )
    }

    private fun pushNotification(title: String?, msg: String?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification: Notification

        val CHANNEL_ID = "general"

        val iNotify = Intent(
            this,
            SplashScreen::class.java
        )
        iNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =
            PendingIntent.getActivity(this, 100, iNotify, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "General"
            val description = "General notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            notificationManager.createNotificationChannel(channel)

            notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setSubText(msg)
                .build()
        } else {
            notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setSubText(msg)
                .build()
        }

        notificationManager.notify(1, notification)
    }
}