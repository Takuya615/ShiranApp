package com.tsumutaku.shiranapp.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.media.MediaActionSound
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics

import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.Methods
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import kotlinx.android.synthetic.main.activity_camera_x.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)

@SuppressLint("RestrictedApi")
class CameraXActivity : AppCompatActivity(){


    private lateinit var captureButton: ImageButton
    private lateinit var switchButton:ImageButton
    private lateinit var backButton:ImageButton
    private lateinit var backView: ConstraintLayout

    private var mTimer: Timer? = null
    private var mTimerSec:Int = 0
    private var taskSec: Int = 0
    private var mHandler = Handler()
    private var lensFacing = CameraX.LensFacing.FRONT


    private lateinit var viewFinder: TextureView
    private lateinit var videoCapture: VideoCapture
    //private lateinit var graphicOverlay:GraphicOverlay
    private var TurnOn:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x)
        //画面をオンのままにしておく
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewFinder = findViewById(R.id.view_finder)
        captureButton = findViewById(R.id.capture_button)
        backView = findViewById(R.id.backview)
        switchButton = findViewById(R.id.switch_button)
        backButton = findViewById(R.id.back_button)
        taskSec = Methods().taskTimeCaluculate(this)
        //graphicOverlay = findViewById(R.id.graphicOverlay)
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
            finish()
        }

        //コーチマーク
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val Coach = TutorialCoachMarkActivity(this)
            Coach.CoachMark3(this, this)
        },1000)

        //ポーズ検出
        /*
        var runnable = Runnable {  }
        //val handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            val bitmap = viewFinder.bitmap
            if(bitmap!=null){graphicOverlay.poseDetectionML(bitmap)}//　　GraphicOverlayクラスで、　ポーズ検出し、それを画面に描画する
            if(TurnOn){
                graphicOverlay.ExerciseIntensity()
            }

            handler.postDelayed(runnable,100)
        }
        handler.post(runnable)

         */


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
            setLensFacing(lensFacing)//内・外カメラの使い分け用
            setTargetResolution(screenSize)//　Size(480, 360)　　Size(metrics.widthPixels, metrics.heightPixels)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)
        }.build()
        videoCapture = VideoCapture(videoCaptureConfig)

        val captureButton = findViewById<ImageButton>(R.id.capture_button)
        captureButton.setOnClickListener {
            val sound = MediaActionSound()//シャッター音
            val file = File(this.filesDir,
                "sample.mp4")//${System.currentTimeMillis()}
            if(!TurnOn){
                TurnOn = true

                switchButton.visibility = View.INVISIBLE
                backButton.visibility = View.INVISIBLE
                captureButton.setImageResource(R.drawable.ic_stop)
                captureButton.setBackgroundColor(Color.WHITE)
                backView.setBackgroundColor(Color.WHITE)
                TimeRecorder(this)
                sound.play(MediaActionSound.START_VIDEO_RECORDING)//シャッター音

                videoCapture.startRecording(file, object: VideoCapture.OnVideoSavedListener{
                    override fun onVideoSaved(file: File?) {
                        //Log.d("tag", "ビデオファイル　ゲット！！")
                        Storage().savefile(file!!)//ストレージとストアに保存、さらにFileを削除
                    }
                    override fun onError(useCaseError: VideoCapture.UseCaseError?, message: String?, cause: Throwable?) {
                        //Log.d("tag", "ビデオ　しっぱい。。。。: $message")
                        //Log.d("tag", "ビデオ　しっぱい。。。。: $cause")
                    }
                })
            }else{
                TurnOn = false
                videoCapture.stopRecording()
                backButton.visibility = View.VISIBLE
                mTimer!!.cancel()
                videoCapture.stopRecording()
                sound.play(MediaActionSound.STOP_VIDEO_RECORDING)//シャッター音

                //CameraDialog().showDialog(this, mTimerSec,this)
                CameraDialogFragment(mTimerSec).show(supportFragmentManager,"sample")
                Storage().editScores(this,mTimerSec)

                mTimerSec=0
                timer.text = "00:00"
                //Log.d("tag", "ビデオ　すとっぴ")
            }

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

    /**
     * 定義されたパーミッションをチェックします
     *
     * private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(
    this, it) == PackageManager.PERMISSION_GRANTED
    }
     */

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    private fun TimeRecorder(context:Context){
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mTimerSec += 1
                mHandler.post {
                    timer.text = showTime(mTimerSec)
                    if(mTimerSec==taskSec){
                        Sounds.getInstance(this@CameraXActivity).playSound(Sounds.SOUND_DRUMROLL)
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