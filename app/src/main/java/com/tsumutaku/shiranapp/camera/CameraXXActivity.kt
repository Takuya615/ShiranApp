package com.tsumutaku.shiranapp.camera

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaActionSound
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.work.*
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.AlertDialogs
import com.tsumutaku.shiranapp.setting.Methods
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import com.tsumutaku.shiranapp.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.activity_camera_x.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File
import java.time.Duration
import java.util.*

private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
)

@SuppressLint("RestrictedApi")
class CameraXXActivity : AppCompatActivity(){

    private lateinit var captureButton: ImageButton
    private lateinit var switchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var backView: ConstraintLayout

    private var mTimer: Timer? = null
    private var mTimerSec:Int = 0
    private var taskSec: Int = 0
    //private var mHandler = Handler()
    private var lensFacing = CameraX.LensFacing.FRONT


    private lateinit var viewFinder: TextureView
    private lateinit var videoCapture: VideoCapture
    private lateinit var graphicOverlay:GraphicOverlay
    //private var TurnOn:Boolean = false
    private var ScoreStop:Boolean = false
    private var IntensityPoint:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x_x)

        //画面をオンのままにしておく
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar!!.hide()

        viewFinder = findViewById(R.id.view_finder)
        captureButton = findViewById(R.id.capture_button)
        backView = findViewById(R.id.backview)
        switchButton = findViewById(R.id.switch_button)
        backButton = findViewById(R.id.back_button)
        taskSec = 60//                                              Methods().taskTimeCaluculate(this)
        graphicOverlay = findViewById(R.id.graphicOverlay)
        Sounds.getInstance(this)//音の初期化

        // カメラパーミッションの要求
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        goal_timer.text ="/${showTime(taskSec)}"//目標時間を表示

        // texture viewが変化した時にLayoutの再計算を行う
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        switchButton.setOnClickListener{
            lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
                CameraX.LensFacing.BACK
            } else {
                CameraX.LensFacing.FRONT
            }
            CameraX.getCameraWithLensFacing(lensFacing)
            startCamera()
        }

        backButton.setOnClickListener{
            val intent= Intent(this@CameraXXActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //ポーズ検出
        val handler = Handler(Looper.getMainLooper())
        var runnable = Runnable {  }
        //val handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            val bitmap = viewFinder.bitmap
            if(bitmap!=null){graphicOverlay.poseDetectionML(bitmap)}//　　GraphicOverlayクラスで、　ポーズ検出し、それを画面に描画する
            if(ScoreStop){
                graphicOverlay.ExerciseIntensity()//表示されているスコアの値に加算していくメソッド
            }

            handler.postDelayed(runnable,100)
        }
        handler.post(runnable)
    }

    private fun startCamera() {
        CameraX.unbindAll()
        // viewfinder use caseのコンフィグレーションオブジェクトを生成
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)//内・外カメラの使い分け用
            setTargetResolution(screenSize)//　　Size(480,360)　Size(metrics.widthPixels, metrics.heightPixels)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        // viewfinder use caseの生成
        val preview = Preview(previewConfig)
        // viewfinderが更新されたらLayoutを再計算
        preview.setOnPreviewOutputUpdateListener {

            // SurfaceTextureの更新して再度親Viewに追加する
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            val surfacetexture: SurfaceTexture = it.surfaceTexture
            viewFinder.setSurfaceTexture(surfacetexture)
            updateTransform()
        }



        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setVideoFrameRate(30)//¸フレームレートの設定（１秒に３０フレ）
            setLensFacing(lensFacing)//内・外カメラの使い分け用
            setTargetResolution(screenSize)//　Size(480, 360)　　Size(metrics.widthPixels, metrics.heightPixels)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)
        }.build()
        videoCapture = VideoCapture(videoCaptureConfig)

        val captureButton = findViewById<ImageButton>(R.id.capture_button)
        captureButton.setOnClickListener {
            val sound = MediaActionSound()//シャッター音
            val file = File(this.filesDir, "sample.mp4")//${System.currentTimeMillis()}
            captureButton.visibility = View.INVISIBLE
            switchButton.visibility = View.INVISIBLE
            //backButton.visibility = View.INVISIBLE
            graphicOverlay.countDown()

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                ScoreStop = true
                //backView.setBackgroundColor(Color.WHITE)
                TimeRecorder(this)
                sound.play(MediaActionSound.START_VIDEO_RECORDING)//シャッター音
                videoCapture.startRecording(file, object: VideoCapture.OnVideoSavedListener{
                    override fun onVideoSaved(file: File?) {
                        val prefs = getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
                        prefs.edit().putInt(getString(R.string.progress),1).apply()
                        val intent= Intent(this@CameraXXActivity, MainActivity::class.java)
                        intent.putExtra("file",file!!.path)
                        intent.putExtra("time",taskSec)
                        startActivity(intent)
                        finish()
                        //val prefs = getSharedPreferences("sample", Context.MODE_PRIVATE)
                        //prefs.edit().putBoolean(getString(R.string.now_editing),true).apply()
                        //OpenCV(file!!.path,applicationContext,mTimerSec)

                        /*val intent = Intent(applicationContext,OpenCVIntentService::class.java)
                        intent.putExtra("file", file!!.path)
                        intent.putExtra("taskSec",taskSec)
                        OpenCVIntentService().enqueueWork(this@CameraXXActivity,intent)*/


                    }
                    override fun onError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
//Log.d("tag", "ビデオ　しっぱい。。。。: $message")
//Log.d("tag", "ビデオ　しっぱい。。。。: $cause")
                    }
                })
            },3000)

            handler.postDelayed({
                ScoreStop = false
                mTimer!!.cancel()
                backView.setBackgroundColor(Color.GREEN)
                Sounds.getInstance(this@CameraXXActivity).playSound(Sounds.SOUND_DRUMROLL)
            },3000+60*1000)//after 63 seconds
            handler.postDelayed({
                Sounds.getInstance(this@CameraXXActivity).playSound(Sounds.SAD_TROMBONE)
            },3000+60*1000+1000)//after 64 seconds
            handler.postDelayed({
                sound.play(MediaActionSound.STOP_VIDEO_RECORDING)//シャッター音
                videoCapture.stopRecording()
                //CameraDialogFragment(-1,0).show(supportFragmentManager,"sample")

            },3000+60*1000+1000+5000)//after 69 seconds


        }
// use caseをlifecycleにバインドする
        CameraX.bindToLifecycle(this, preview,videoCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()

// view finderの中心の計算
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

// 表示回転を考慮したプレビュー出力
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

// TextureViewへのセット
        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                    "パーミッションが許可されませんでした",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    private fun TimeRecorder(context: Context){
        mTimer = Timer()
        val mHandler = Handler(Looper.getMainLooper())
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mTimerSec += 1
                mHandler.post {
                    timer.text = showTime(mTimerSec)
                    if(mTimerSec==taskSec){
                        Sounds.getInstance(this@CameraXXActivity).playSound(Sounds.SOUND_DRUMROLL)
                        ScoreStop = false//時間がきたらポーズ検出のスコアを止める。
                    }
                    if (mTimerSec >= taskSec && taskSec != 0) {
                        backView.setBackgroundColor(Color.GREEN)
                        captureButton.setBackgroundColor(Color.GREEN)
                    }
                }
            }
        }, 1000, 1000)
    }

    private fun showTime(time:Int):String{
        val seconds = time % 60;
        val minite = (time / 60) % 60;
        val timer = String.format("%02d:%02d", minite, seconds)
        return  timer
    }

}




