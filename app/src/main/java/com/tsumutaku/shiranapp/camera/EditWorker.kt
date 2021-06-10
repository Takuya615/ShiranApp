package com.tsumutaku.shiranapp.camera


import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.tsumutaku.shiranapp.MainActivity


class EditWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    val context :Context = appContext

    override suspend fun doWork(): Result {

        val filePath = inputData.getString("file")
        val taskSec = inputData.getInt("taskSec",0)
        if(taskSec == -1){
            //val progress = "ダウンロード中"
            //setForeground(createForegroundInfo(progress))
            val convertedUri = Uri.parse(filePath)
            download(convertedUri)
        }else{
            val progress = "動画編集中..."
            setForeground(createForegroundInfo(progress))

            OpenCV(filePath!!,applicationContext, taskSec)
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    fun createForegroundInfo(progress: String): ForegroundInfo{
        val id = "id"//applicationContext.getString(R.string.notification_channel_id)
        val title = "しらんプリ"//applicationContext.getString(R.string.notification_title)
        val cancel = "キャンセル"//applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker

        val channel = NotificationChannel(
            id, title,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService<NotificationManager>(context,NotificationManager::class.java) as NotificationManager
        manager.createNotificationChannel(channel)

        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.notification_bg)
            .setOngoing(true)
            //.setProgress(100,0,false)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(1,notification)
    }

    fun download(uri: Uri):Long{

        val  dm: DownloadManager = context.getSystemService(JobIntentService.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)
        request.setDestinationInExternalPublicDir(
            "/Android/media/com.tsumutaku.shiranapp/",//externalMediaDirs.first()と同じ
            "${System.currentTimeMillis()}.mp4"
        )
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle("charenge")
        val downloadReference = dm.enqueue(request) ?: 0

        //¸¸イベントログ
        if(!MainActivity.debag){
            val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.METHOD, "SHARE")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
        }

        return downloadReference
    }

}
