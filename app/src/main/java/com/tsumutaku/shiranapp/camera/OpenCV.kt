package com.tsumutaku.shiranapp.camera

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.AlertDialogs
import com.tsumutaku.shiranapp.ui.home.HomeViewModel
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.VideoWriter
import org.opencv.videoio.Videoio
import java.io.File


class OpenCV(val inputPath: String, val context: Context,val time:Int){
    public lateinit var writer: VideoWriter
    public lateinit var videoCapture :VideoCapture
    private var width: Double = 0.0
    private var height:Double = 0.0
    private var posefinish:Boolean = false

    private var totalPoint = 0//¸スコアの計測用
    private var xy_Lists:MutableList<Pair<Float, Float>> = mutableListOf()
    private var saved_xy_lists = mutableListOf<Pair<Float, Float>>()
    private var mesureCounter = 0
    private val isPrivacyMode = context.getSharedPreferences("sample", Context.MODE_PRIVATE).getBoolean(
            context.getString(
                    R.string.privacyMode
            ), false
    )
    private val bmp : Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.shiran_app_icon)
    private lateinit var shiran_app : Bitmap//= Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.RGB_565)//ARGB_8888

    init {

        newProgress(10)
        val m = Matrix()
        m.postRotate(90f)
        shiran_app = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height,m , true)
        videoCapture = VideoCapture()
        videoCapture.open(inputPath)
        /*
        if (videoCapture.isOpened){
            if(MainActivity.debag){ Log.d(MainActivity.TAG, "開いてるよ")}
        }else{
            if(MainActivity.debag){Log.d(MainActivity.TAG, "だめだめ")}
        }*/

        videoCapture.grab()
        width = videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH)
        height = videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "output.avi")//context.filesDir

        writer = VideoWriter()
        writer.open(
            file.path,
            VideoWriter.fourcc('M', 'J', 'P', 'G'),
            30.0,
            Size(width,height)
        )

        /*
        if (writer.isOpened){
            if(MainActivity.debag){ Log.d(MainActivity.TAG, "writer  開いてるよ")}
        }else{
            if(MainActivity.debag){Log.d(MainActivity.TAG, "writer  だめだめ")}
        }*/
        if(writer.isOpened){
            posefinish=true// １フレ目　読み込み用
            val inpMat = Mat(width.toInt(), height.toInt(),CvType.CV_8UC4)
            while (true){
                if (!posefinish){continue}
                if(videoCapture.read(inpMat)){//videoCapture.grab()
                    posefinish=false
                    if(inpMat.dims() == 0){ posefinish = true;  continue }

                    mesureCounter+=1//１フレ呼ばれるたびにカウントする。３０回呼ばれたら１秒の映像を読み込んだと言うこと

                    if(MainActivity.debag){Log.d(MainActivity.TAG, "カウンター$mesureCounter time$time")}

                    if(mesureCounter == time*30/3){
                        if(MainActivity.debag){Log.d(MainActivity.TAG, "33%")}
                        newProgress(30)}
                    if(mesureCounter == time*30*2/3){
                        if(MainActivity.debag){Log.d(MainActivity.TAG, "66%")}
                        newProgress(50)}

                    val bmp = Bitmap.createBitmap(
                        width.toInt(),
                        height.toInt(),
                        Bitmap.Config.ARGB_8888
                    )//RGB_565

                    Utils.matToBitmap(inpMat, bmp, false)

                    DrawPose(inpMat, bmp)

                }else{
                    if(MainActivity.debag){Log.d(MainActivity.TAG, "videoCapture終了！")}
                    writer.release()
                    videoCapture.release()

                    //"${System.currentTimeMillis()}.mp4"
                    val out = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                        "out_${System.currentTimeMillis()}.mp4"
                    )
                    val soundonly = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                        "soundOnly_${System.currentTimeMillis()}.mp3"
                    )
                    val final = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                        "final_${System.currentTimeMillis()}.mp4"
                    )
                    with_ffmpeg(file, out, soundonly, final)//     avi -> mp4 -> save()

                    break
                }
            }

        }
    }

    fun DrawPose(mat: Mat, bitmap: Bitmap){

        val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
        val poseDetector = PoseDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)
        poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    val all = pose.allPoseLandmarks
                    if(MainActivity.debag){Log.d(MainActivity.TAG, "全ランドマーク　${all}")}
                    if(all.isNotEmpty()){//¸ここのなかで完結させる。ここで動画出力まで済ませてしまう。
                        //¸人が写ってたらポーズ検出
                        xy_Lists = mutableListOf()
                        editVideo(mat, pose)
                    }else{
                        //人が写ってなかったら、なにもマスク画像を作らないまま、ビデオライターへ送る
                        xy_Lists = mutableListOf()//人が画面外に出たら、描画も停止する。
                        outputVideo(mat, null)
                    }

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    if(MainActivity.debag){Log.d("pose", "ポーズ検出しっぱい: $e")}
                    posefinish=true
                }
    }



    fun editVideo(mat: Mat, pose: Pose){//ポーズの情報からBitmapをつくる

        val listPoseLandmark = mutableListOf<PoseLandmark>(
                pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)!!,//0    0-2-4
                pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)!!,//    1-3-5
                pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)!!,
                pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)!!,//5
                pose.getPoseLandmark(PoseLandmark.LEFT_HIP)!!,//          6-8-10
                pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)!!,//         7-9-11
                pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)!!,
                pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)!!,//10
                pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)!!,
                pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)!!,
                pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)!!,//15
                pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)!!,
                pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)!!,
                pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)!!,//20
                pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)!!,
                pose.getPoseLandmark(PoseLandmark.LEFT_EAR)!!,
                pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)!!,
                pose.getPoseLandmark(PoseLandmark.NOSE)!!
        )
        for(i in listPoseLandmark){
            val x = i.position.x
            val y = i.position.y
            xy_Lists.add(Pair(x, y))
        }

        ExerciseIntensity()//xy_list¸を元にしてスコア計算----------------------
        //if (mesureCounter < 30*setuptime){//時間ごとに動画内編集の仕方を帰る場合に有効 }
        val dst = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.RGB_565)//ARGB_8888
        //     ここから描画処理
        val canvas = Canvas(dst)
        val paint= Paint()
        paint.setColor(Color.argb(255, 0, 255, 0));
        paint.setStrokeWidth(40f)
        paint.setAntiAlias(true)
        paint.setStyle(Paint.Style.FILL_AND_STROKE)

        //GraphicOverlay.drawDefault(canvas,paint,xy_Lists,totalPoint)
        //canvas?.drawCircle(450f, 800f, 5f, paint) // (x1,y1,r,paint) 中心x1座標, 中心y1座標, r半径
        /*if(isPrivacyMode){
            GraphicOverlay.drawPrivacy(context,canvas,paint,xy_Lists,totalPoint)
        //   Methods().drawPrivacy()
        }else{
            GraphicOverlay.drawDefault(canvas,paint,xy_Lists,totalPoint)
            //Methods().drawDefault(canvas, paint, xy_Lists, totalPoint)
        }*/

        if(xy_Lists.isNotEmpty()){
            for(i in 0..xy_Lists.size-4 ){
                val xx = xy_Lists[i].first
                val yy = xy_Lists[i].second
                canvas.drawCircle(xx, yy, 10f, paint) // (x1,y1,r,paint) 中心x1座標, 中心y1座標, r半径
                //canvas?.drawPoint(xx,yy,paint)//-------------点------------
            }

            paint.setStrokeWidth(30f)
            paint.setColor(Color.argb(255, 255, 255, 0));

            val drawline = mutableListOf<Float>()
            val numList = listOf<Int>(0, 1, 0, 2, 1, 3, 2, 4, 3, 5, 0, 6, 1, 7, 6, 7, 6, 8, 7, 9, 8, 10, 9, 11)
            for (i in numList){
                drawline.add(xy_Lists[i].first)
                drawline.add(xy_Lists[i].second)
            }
            canvas.drawLines(drawline.toFloatArray(), paint)//---------------------線---------------

            if(isPrivacyMode){
                val Nose_x = xy_Lists[24].second
                val Nose_y = xy_Lists[24].first//height.toFloat() - xy_Lists[24].first
                val lEar_x = xy_Lists[22].second
                val rEar_x = xy_Lists[23].second
                val L_EartoNose = Math.abs(Nose_x - lEar_x)
                val R_EartoNose = Math.abs(Nose_x - rEar_x)
                val times = (L_EartoNose + R_EartoNose)/shiran_app.width * 2.0f

                val bit_w=shiran_app.width/2*times
                val bit_h=shiran_app.height/2*times
                val left = Nose_x - bit_w
                val top = Nose_y - bit_h
                canvas.scale(times,times)
                canvas.drawBitmap(shiran_app,top/times,left/times,paint)
                canvas.scale(1/times,1/times)//スケールを元に戻す。
                /**
                //画像を回転させる
                val matr = Matrix()
                matr.postRotate(90f)
                val shiran_app: Bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matr, true)

                //倍率を決める

                val lEar_x = xy_Lists[22].first//second
                val rEar_x = xy_Lists[23].first//second//
                val Nose_x = xy_Lists[24].first//second//
                val Nose_y = xy_Lists[24].second//first//
                val L_EartoNose = Math.abs(lEar_x - Nose_x)
                val R_EartoNose = Math.abs(Nose_x - rEar_x)
                val times = (L_EartoNose + R_EartoNose)/shiran_app.width * 7.0f

                val bit_w=shiran_app.width/2*times
                val bit_h=shiran_app.height/2*times
                val top = Nose_y - bit_h
                val left = Nose_x - bit_w
                canvas.scale(times, times)
                //canvas.drawBitmap(shiran_app,100f,100f,paint)
                canvas.drawBitmap(shiran_app, left / times, top / times, paint)
                */
            }

            //canvas.rotate(90f, width.toFloat() / 2, height.toFloat() / 2)//270度　回転
            paint.setTextSize(100f);
            val a = totalPoint.toInt() / 5000

            canvas.rotate(90f, 100f, 100f)//90度　回転
            canvas.drawText("Score $a ", 100f, 100f, paint)

        }

        outputVideo(mat, dst)
    }

    fun outputVideo(inpMat: Mat, bitmap: Bitmap?){//¸やってきたBitmapをひたすらライターへ書き込んでいくメソッド

        if(bitmap !=null){
            val matC1 = Mat(width.toInt(), height.toInt(),CvType.CV_8UC4)//3,3, CvType.CV_32F
            val mat = Mat(width.toInt(), height.toInt(),CvType.CV_8UC4)//3,3, CvType.CV_32F
            //val bitmap2: Bitmap = testDraw(videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH).toInt(), videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT).toInt())
            Utils.bitmapToMat(bitmap, matC1)
            Imgproc.cvtColor(matC1, mat, 3);//CV_GRAY2BGR  CV_DIST_MASK_3    チャンネル変更処理

            Core.add(inpMat, mat, mat)//二つのMatを重ねる

            val eMat = Mat(width.toInt(), height.toInt(),CvType.CV_8UC4)
            Imgproc.cvtColor(mat, eMat, 4)//¸カラー変更
            writer.write(eMat)//書き込むoutput
        }else{
            val eMat = Mat(width.toInt(), height.toInt(),CvType.CV_8UC4)

            if (isPrivacyMode){
                //プライバシーモードなら全面黒くする
                val black = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)//RGB_565
                val c = Canvas(black)
                //c.drawColor(Color.argb(255, 0, 0, 0));// 背景、半透明にする場合
                val p = Paint()
                p.setColor(Color.argb(255, 100, 100, 100))
                p.setStyle(Paint.Style.FILL_AND_STROKE)
                c.drawRect(0f,0f, width.toFloat(), height.toFloat(),p)
                Utils.bitmapToMat(black, eMat)
            }else{
                Imgproc.cvtColor(inpMat, eMat, 4)//¸カラーに変更
            }
            writer.write(eMat)
        }
        posefinish=true//¸次のポーズ検出を始めるためのフラグ
    }

    //　　　　　　前回の値　－　今回の値　＝　運動量--------------------------------------------------
    fun ExerciseIntensity():Int{
        //初回　値を保存しておくだけ
        if(saved_xy_lists.isEmpty()){
            saved_xy_lists = xy_Lists
        }else if (xy_Lists.isNotEmpty()){//2回目以降、移動ポイントを計算
            for(i in 0..saved_xy_lists.size - 1){
                val diff_x = saved_xy_lists[i].first - xy_Lists[i].first
                val point_x = Math.abs(diff_x).toInt() //絶対値に変換
                val diff_y = saved_xy_lists[i].second - xy_Lists[i].second
                val point_y = Math.abs(diff_y).toInt() //絶対値に変換
                val a = (point_x + point_y)
                totalPoint = totalPoint + a
            }
            saved_xy_lists = xy_Lists
        }
        return totalPoint
    }

    fun with_ffmpeg(avifile: File, mp4out: File, mp3sound: File, mp4final: File){

        newProgress(70)
        if(MainActivity.debag){Log.i(MainActivity.TAG, "FF変換¸始まるよ")}
        val rc1 = FFmpeg.execute("-i $inputPath ${mp3sound.path}")// -vn
        val rc2 = FFmpeg.execute("-i ${avifile.path} -vf transpose=2 -metadata:s:v:0 rotate=0 -s 640x360 ${mp4out.path}")
        //¸解像度 1280*720 1024x576 768x432  480x270 320x180

        if (rc1 == Config.RETURN_CODE_SUCCESS && rc2 == Config.RETURN_CODE_SUCCESS) {
            if(MainActivity.debag){Log.i(MainActivity.TAG, "音声抽出　")}

            val rc3 = FFmpeg.execute("-i ${mp4out.path} -i ${mp3sound.path} -c:v copy -c:a aac ${mp4final.path}")
            if (rc3 == Config.RETURN_CODE_SUCCESS) {
                if(MainActivity.debag){Log.i(MainActivity.TAG, "douga¸作成せいこう！！保存します　")}
                newProgress(90)
                Storage().savefile(context,mp4final)

                avifile.delete();mp4out.delete();mp3sound.delete();

                /*
                if(dayly){
                    //camera X Activity由来なら
                }else{
                    //camera XX Activity由来なら
                    val uri = Uri.fromFile(mp4final)
                    GalleryActivity().download(uri)
                }*/

            }

        } else if (rc1 == Config.RETURN_CODE_CANCEL) {
            if(MainActivity.debag){Log.i(MainActivity.TAG, "音声抽出　途中でキャンセルされた？？")}
        } else {
            if(MainActivity.debag){Log.i(
                    MainActivity.TAG, String.format(
                    "音声抽出失敗 $rc1 and the output below.",
                    rc1
            )
            )}
        }
    }

    fun newProgress(p:Int){
        val prefs = context.getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
        prefs.edit().putInt(context.getString(R.string.progress),p).apply()
    }
}