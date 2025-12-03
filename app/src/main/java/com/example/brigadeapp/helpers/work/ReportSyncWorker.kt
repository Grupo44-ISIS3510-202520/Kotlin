package com.example.brigadeapp.helpers.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.brigadeapp.data.repository.FileRepositoryImpl
import com.example.brigadeapp.data.services.local.ReportLocalService
import com.example.brigadeapp.data.services.remote.ReportRemoteService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import com.example.brigadeapp.R
import java.text.SimpleDateFormat
import java.util.Locale


@HiltWorker
class ReportSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileRepository: FileRepositoryImpl
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
                    var reportToSync = list[i]
                    
                    // Upload image if it's a local file
                    if (reportToSync.imageUrl != null && isLocalFile(reportToSync.imageUrl!!)) {
                        val imageFile = File(reportToSync.imageUrl!!)
                        if (imageFile.exists()) {
                            val uploadResult = fileRepository.uploadFile(
                                imageFile,
                                "brigadeapp-report-images",
                                "${System.currentTimeMillis()}_${imageFile.name}"
                            )
                            if (uploadResult.isSuccess) {
                                val newUrl = uploadResult.getOrNull()
                                reportToSync = reportToSync.copy(imageUrl = newUrl)
                            } else {
                                Log.e("ReportSyncWorker", "Failed to upload image: ${uploadResult.exceptionOrNull()?.message}")
                            }
                        }
                    }
                    
                    // Upload audio if it's a local file
                    if (reportToSync.audioUrl != null && isLocalFile(reportToSync.audioUrl!!)) {
                        val audioFile = File(reportToSync.audioUrl!!)
                        if (audioFile.exists()) {
                            val uploadResult = fileRepository.uploadFile(
                                audioFile,
                                "brigadeapp-report-audios",
                                "${System.currentTimeMillis()}_${audioFile.name}"
                            )
                            if (uploadResult.isSuccess) {
                                val newUrl = uploadResult.getOrNull()
                                reportToSync = reportToSync.copy(audioUrl = newUrl)
                            } else {
                                Log.e("ReportSyncWorker", "Failed to upload audio: ${uploadResult.exceptionOrNull()?.message}")
                            }
                        }
                    }

                    val remoteResult = remoteService.saveReport(reportToSync)
                    if (remoteResult.isSuccess) {
                        localService.markSynced(list[i].id)

                        val timeFormat = SimpleDateFormat("HHmmss", Locale.US)
                        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                            .parse(reportToSync.timestamp)
                        val timeString = if (timestamp != null) {
                            timeFormat.format(timestamp)
                        } else {
                            timeFormat.format(java.util.Date())
                        }
                        val tempReportId = "P_$timeString"
                        localService.deletePendingCachedReport(tempReportId)
                    } else {
                        Log.e("ReportSyncWorker", "Failed to save report: ${remoteResult.exceptionOrNull()?.message}")
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

    private fun isLocalFile(path: String): Boolean {
        return path.startsWith("/") || path.startsWith("file://") || File(path).exists()
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
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}