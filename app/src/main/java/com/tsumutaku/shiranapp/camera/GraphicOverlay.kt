package com.tsumutaku.shiranapp.camera

import android.content.Context
import android.graphics.*
import android.provider.Settings.Global.getString
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.scale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.Methods
import java.lang.Boolean.getBoolean

class GraphicOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //private lateinit var paint:Paint
    private var paint: Paint = Paint()

    //スコアを表示する用のインスタンス
    private var totalPoint = 0
    private var xy_Lists:MutableList<Pair<Float,Float>> = mutableListOf()
    private var saved_xy_lists = mutableListOf<Pair<Float,Float>>()
    private var countDown = 4
    private val isPrivacyMode = context.getSharedPreferences("sample", Context.MODE_PRIVATE)
        .getBoolean(context.getString(R.string.privacyMode),false)
    private val shiran_app: Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.shiran_app_icon)


    //ーーーーーーーーーーーーーーカメラ画面上に、運動スコアやポーズ検出結果を描画するーーーーーーーーーーーーー
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //canvas?.drawColor(Color.argb(127, 0, 127, 63));// 背景、半透明にする場合
        paint.setColor(Color.argb(255, 0, 255, 0));
        paint.setStrokeWidth(10f)
        paint.setStyle(Paint.Style.FILL_AND_STROKE)
        if(countDown>0 && 4>countDown){
            //paint.setColor(Color.argb(255, 0, 255, 0));
                //paint.setStyle(Paint.Style.STROKE)
            paint.setTextSize(500f)
            canvas?.drawText("$countDown",canvas.width/2-125.toFloat(),canvas.height/2.toFloat(),paint)
        }

        if(xy_Lists.isNotEmpty()){
            for(i in 0..xy_Lists.size-4 ){
                val xx = xy_Lists[i].first
                val yy = xy_Lists[i].second
                canvas?.drawCircle(xx, yy, 10f, paint) // (x1,y1,r,paint) 中心x1座標, 中心y1座標, r半径
                //canvas?.drawPoint(xx,yy,paint)//-------------点------------
            }

            paint.setStyle(Paint.Style.FILL_AND_STROKE);//
            paint.setStrokeWidth(10f);
            paint.setTextSize(80f);
            paint.setColor(Color.argb(255, 250, 250, 0));
            canvas?.drawText("Score ${totalPoint/5000} ",5f,80f,paint)//--------テキストーーーーーーー

            paint.setStrokeWidth(30f)
            paint.setColor(Color.argb(255, 255, 255, 0));

            val drawline = mutableListOf<Float>()
            val numList = listOf<Int>(0,1, 0,2, 1,3, 2,4, 3,5, 0,6, 1,7, 6,7, 6,8, 7,9, 8,10, 9,11)
            for (i in numList){
                drawline.add(xy_Lists[i].first)
                drawline.add(xy_Lists[i].second)
            }
            canvas?.drawLines(drawline.toFloatArray(),paint)//---------------------線---------------

            if (isPrivacyMode){
                //倍率を決める
                val lEar_x = xy_Lists[22].first
                val rEar_x = xy_Lists[23].first
                val Nose_x = xy_Lists[24].first
                val Nose_y = xy_Lists[24].second
                val L_EartoNose = Math.abs(lEar_x - Nose_x)
                val R_EartoNose = Math.abs(Nose_x - rEar_x)
                val times = (L_EartoNose + R_EartoNose)/shiran_app.width * 2.0f

                val bit_w=shiran_app.width/2*times
                val bit_h=shiran_app.height/2*times
                val top = Nose_y - bit_h
                val left = Nose_x - bit_w
                canvas?.scale(times,times)
                canvas?.drawBitmap(shiran_app,left/times,top/times,paint)
            }
        }

        /**
        if(isPrivacyMode){
            drawPrivacy(context,canvas!!,paint,xy_Lists,totalPoint)
        }else{
            drawDefault(canvas!!,paint,xy_Lists,totalPoint)
        }
        */
    }

    fun point():Int{ return totalPoint/5000 }


    //ーーーーーーーーーーーーーーーーーーーーーポーズ検出メソッドーーーーーーーーーーーーーーーーーーーー
    fun poseDetectionML(bitmap: Bitmap){

        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        val poseDetector = PoseDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val all = pose.allPoseLandmarks
                if(MainActivity.debag){Log.d("pose","全ランドマーク　${all}")}

                if(all.isNotEmpty()){
                    setParts(pose)
                    //ExerciseIntensity()
                    invalidate()
                }else{
                    xy_Lists = mutableListOf()//人が画面外に出たら、描画も停止する。
                    invalidate()
                }

            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                if(MainActivity.debag){Log.d("pose", "ポーズ検出しっぱい: $e")}
            }
            .addOnCompleteListener { }//results -> imageProxy.close()Log.d("pose","検出完了")
    }

    //ーーーーーーーーーーーーーーーーーーーーーーー各部位の　ｘ　ｙ　の値をリスト化してかえすーーーーーーーーー
    fun setParts(pose: Pose){

        val list = mutableListOf<Pair<Float,Float>>()
        val listPoseLandmark = mutableListOf<PoseLandmark>(
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)!!//0
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)!!//5
            ,pose.getPoseLandmark(PoseLandmark.LEFT_HIP)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)!!//10
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)!!//15
            ,pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)!!//20
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_EAR)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)!!
            ,pose.getPoseLandmark(PoseLandmark.NOSE)!!
        )
        for(i in listPoseLandmark){
            val x = i.position.x
            val y = i.position.y
            list.add(Pair(x,y))
        }
        xy_Lists = list
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

    fun countDown(){
        var runnable = Runnable {  }
        //val handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            countDown -= 1

            if(countDown==0){ handler.removeCallbacks(runnable) }
            else{handler.postDelayed(runnable,1000)}
        }
        handler.post(runnable)
    }
    companion object{

        fun drawDefault(canvas: Canvas,paint:Paint,xy_Lists:MutableList<Pair<Float,Float>>,totalPoint:Int){

        }

        fun drawPrivacy(context: Context,canvas: Canvas,paint:Paint,xy_Lists:MutableList<Pair<Float,Float>>,totalPoint:Int){

            val shiran_app: Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.shiran_app_icon)//mipmap.shira_app_round
            if(xy_Lists.isNotEmpty()){
                for(i in 0..xy_Lists.size-4 ){
                    val xx = xy_Lists[i].first
                    val yy = xy_Lists[i].second
                    canvas?.drawCircle(xx, yy, 10f, paint) // (x1,y1,r,paint) 中心x1座標, 中心y1座標, r半径
                    //canvas?.drawPoint(xx,yy,paint)//-------------点------------
                }

                paint.setStyle(Paint.Style.FILL_AND_STROKE);//
                paint.setStrokeWidth(10f);
                paint.setTextSize(100f);
                paint.setColor(Color.argb(255, 250, 250, 0));
                canvas?.drawText("Score ${totalPoint/5000} ",5f,50f,paint)//--------テキストーーーーーーー

                paint.setStrokeWidth(30f)
                paint.setColor(Color.argb(255, 255, 255, 0));

                val drawline = mutableListOf<Float>()
                val numList = listOf<Int>(0,1, 0,2, 1,3, 2,4, 3,5, 0,6, 1,7, 6,7, 6,8, 7,9, 8,10, 9,11)
                for (i in numList){
                    drawline.add(xy_Lists[i].first)
                    drawline.add(xy_Lists[i].second)
                }
                canvas?.drawLines(drawline.toFloatArray(),paint)//---------------------線---------------

                //胴体の塗りつぶし
                //val extra = xy_Lists[6].second - xy_Lists[1].second
                //canvas?.drawRect(xy_Lists[1].first, xy_Lists[1].second, xy_Lists[6].first, xy_Lists[6].second+extra/2,paint)
                //canvas?.drawRect(xy_Lists[0].first, xy_Lists[0].second, xy_Lists[7].first, xy_Lists[7].second+extra/2,paint)

                //倍率を決める
                val lEar_x = xy_Lists[22].first
                val rEar_x = xy_Lists[23].first
                val Nose_x = xy_Lists[24].first
                val Nose_y = xy_Lists[24].second
                val L_EartoNose = Math.abs(lEar_x - Nose_x)
                val R_EartoNose = Math.abs(Nose_x - rEar_x)
                val times = (L_EartoNose + R_EartoNose)/shiran_app.width * 3.0f

                val bit_w=shiran_app.width/2*times
                val bit_h=shiran_app.height/2*times
                val top = Nose_y - bit_h
                val left = Nose_x - bit_w
                canvas?.scale(times,times)
                canvas?.drawBitmap(shiran_app,left/times,top/times,paint)
            }
        }
    }
}