package com.tsumutaku.shiranapp.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class GraphicOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {


    //private lateinit var paint:Paint
    private var paint: Paint = Paint()

    var xy_Lists:MutableList<Pair<Float,Float>> = mutableListOf()

    //スコアを表示する用のインスタンス
    var totalPoint = 0
    var saved_xy_lists = mutableListOf<Pair<Float,Float>>()

    //ーーーーーーーーーーーーーーカメラ画面上に、運動スコアやポーズ検出結果を描画するーーーーーーーーーーーーー
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //canvas?.drawColor(Color.argb(127, 0, 127, 63));// 背景、半透明にする場合

        paint.setColor(Color.argb(255, 68, 255, 255));
        paint.setStrokeWidth(10f)
        paint.setAntiAlias(true)
        paint.setStyle(Paint.Style.STROKE)
        //canvas?.drawCircle(450f, 800f, 5f, paint) // (x1,y1,r,paint) 中心x1座標, 中心y1座標, r半径

        //点
        if(xy_Lists.isNotEmpty()){
            for(i in 0..xy_Lists.size-1 ){
                val xx = xy_Lists[i].first
                val yy = xy_Lists[i].second
                canvas?.drawPoint(xx,yy,paint)
            }
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(5f);
            paint.setTextSize(100f);
            paint.setColor(Color.argb(255, 255, 255, 0));
            val a = totalPoint.toInt() / 1000
            canvas?.drawText("Score $a ",100f,100f,paint)

            canvas?.drawLines(drawLine(),paint)
        }
    }



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
                Log.d("pose","全ランドマーク　${all}")

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
                Log.d("pose", "ポーズ検出しっぱい: $e")
            }
            .addOnCompleteListener { }//results -> imageProxy.close()Log.d("pose","検出完了")
    }

    //ーーーーーーーーーーーーーーーーーーーーーーー各部位の　ｘ　ｙ　の値をリスト化してかえすーーーーーーーーー
    fun setParts(pose: Pose){

        val list = mutableListOf<Pair<Float,Float>>()
        val listPoseLandmark = mutableListOf<PoseLandmark>(
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_HIP)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)!!
            ,pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)!!
            ,pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)!!
        )
        for(i in listPoseLandmark){
            val x = i.position.x
            val y = i.position.y
            list.add(Pair(x,y))
        }
        xy_Lists = list
    }

    //　　　　　　前回の値　－　今回の値　＝　運動量--------------------------------------------------
    fun ExerciseIntensity(){

        //初回　値を保存しておくだけ
        if(saved_xy_lists.isEmpty()){
            saved_xy_lists = xy_Lists
        }else{//2回目以降、移動ポイントを計算
            for(i in 0..saved_xy_lists.size - 1){
                val diff_x = saved_xy_lists[i].first - xy_Lists[i].first
                val point_x = Math.abs(diff_x).toInt() //絶対値に変換
                val diff_y = saved_xy_lists[i].second - xy_Lists[i].second
                val point_y = Math.abs(diff_y).toInt() //絶対値に変換
                totalPoint = totalPoint + point_x + point_y
            }
            saved_xy_lists = xy_Lists
        }
    }

    fun drawLine():FloatArray{
        val b = mutableListOf<Float>()
        val numList = listOf<Int>(0,1, 0,2, 1,3, 2,4, 3,5, 0,6, 1,7, 6,7, 6,8, 7,9, 8,10, 9,11)
        for (i in numList){
            b.add(xy_Lists[i].first)
            b.add(xy_Lists[i].second)
        }
        return  b.toFloatArray()
        /*

         */
    }


}