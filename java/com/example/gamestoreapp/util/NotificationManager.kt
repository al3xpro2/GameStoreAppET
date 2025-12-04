package com.example.gamestoreapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gamestoreapp.R

class NotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "purchase_channel"
        private const val CHANNEL_NAME = "Purchase Notifications"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for successful purchases"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPurchaseCompletedNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_gamestore) // Asegúrate de tener este drawable
            .setContentTitle("¡Compra Exitosa!")
            .setContentText("Tu pedido ha sido procesado correctamente.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // No se requiere permiso para mostrar la notificación si el canal ya está creado
        // y el usuario no las ha desactivado.
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}