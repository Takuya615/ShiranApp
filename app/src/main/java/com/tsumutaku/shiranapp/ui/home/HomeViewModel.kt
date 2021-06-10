package com.tsumutaku.shiranapp.ui.home

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.work.*
import androidx.work.R
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.camera.CameraDialogFragment
import com.tsumutaku.shiranapp.camera.EditWorker
import com.tsumutaku.shiranapp.setting.AlertDialogs
import java.io.File


class HomeViewModel : ViewModel() {

    fun WMrequest(activity: Activity,file: String,time:Int){
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val workManager = WorkManager.getInstance(activity)
        val myData: Data = workDataOf("file" to file, "taskSec" to time)
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest = OneTimeWorkRequestBuilder<EditWorker>()
            .setInputData(myData)
            .setConstraints(constraints)
            .build()

        workManager
            .enqueueUniqueWork(
            "editVideo",
            ExistingWorkPolicy.REPLACE,
            uploadWorkRequest
        )


    }

}
