package com.example.brigadeapp.helpers.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.brigadeapp.data.services.local.ReportLocalService
import com.example.brigadeapp.data.services.remote.ReportRemoteService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import com.example.brigadeapp.R


class ReportSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val CHANNEL_ID = "reports_sync_channel"
    private val NOTIF_ID = 2001

    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) @androidx.annotation.RequiresPermission(
        android.Manifest.permission.POST_NOTIFICATIONS
    ) {
        try {
            val localService = ReportLocalService(applicationContext)
            val remoteService = ReportRemoteService(
                FirebaseFirestore.getInstance(),
                FirebaseStorage.getInstance(),
                applicationContext
            )

            val unsyncedRes = localService.getUnsynced()
            val list = unsyncedRes.getOrNull() ?: emptyList()
            if (list.isEmpty()) return@withContext Result.success()

            createNotificationChannel()
            notifyStart(list.size)

            var completed = 0
            for (i in 0 until list.size) {
                try {
                    val remoteResult = remoteService.saveReport(list[i])
                    if (remoteResult.isSuccess) {
                        localService.markSynced(list[i].id)
                    }
                } catch (e: Exception) {
                    throw Exception("Error syncing report id=${list[i].id}: ${e.message}")
                }
                completed++
                notifyProgress(completed, list.size)
            }

            notifyDone()
            return@withContext Result.success()
        } catch (t: Throwable) {
            return@withContext Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Report Sync"
            val descriptionText = "Notifications about report synchronization"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun notifyStart(total: Int) {
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(applicationContext.getString(R.string.Syncing_Reports))
            .setContentText("Uploading $total pending reports")
            .setOngoing(true)
            .setProgress(total, 0, false)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notif)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun notifyProgress(done: Int, total: Int) {
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(applicationContext.getString(R.string.Syncing_Reports))
            .setContentText("Uploaded $done of $total reports")
            .setOngoing(true)
            .setProgress(total, done, false)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notif)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun notifyDone() {
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(applicationContext.getString(R.string.Synced_Reports))
            .setContentText(applicationContext.getString(R.string.All_Reports_Synced))
            .setOngoing(false)
            .setProgress(0, 0, false)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notif)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "report_sync_work"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ReportSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}