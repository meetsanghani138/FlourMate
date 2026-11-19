package com.marwadiuniversity.flourmate.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.marwadiuniversity.flourmate.R
import com.marwadiuniversity.flourmate.database.FlourMateDatabase
import com.marwadiuniversity.flourmate.models.DeliveryLocation
import com.marwadiuniversity.flourmate.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var partnerId: Long = 0
    private var activeOrderId: Long = 0

    companion object {
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 1001
        const val EXTRA_ORDER_ID = "extra_order_id"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sessionManager = SessionManager(this)
        partnerId = sessionManager.getUserId()

        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                saveLocation(location.latitude, location.longitude)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        activeOrderId = intent?.getLongExtra(EXTRA_ORDER_ID, 0) ?: 0

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FlourMate Delivery Tracking")
            .setContentText("Sharing live delivery location...")
            .setSmallIcon(R.drawable.ic_delivery_bike)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startLocationUpdates()

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).setMinUpdateIntervalMillis(3000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun saveLocation(lat: Double, lng: Double) {
        if (partnerId <= 0) return
        val db = FlourMateDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.deliveryDao().insertLocation(
                DeliveryLocation(
                    deliveryPartnerId = partnerId,
                    orderId = activeOrderId,
                    latitude = lat,
                    longitude = lng
                )
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Live Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
