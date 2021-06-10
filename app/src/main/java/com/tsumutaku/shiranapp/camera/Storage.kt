package com.tsumutaku.shiranapp.camera

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StreamDownloadTask
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.camera.CameraDialogFragment2.Companion.loadArrayList
import com.tsumutaku.shiranapp.ui.home.HomeViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*


class Storage {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()


    fun savefile(context: Context,file: File){
        if(MainActivity.debag){Log.i(MainActivity.TAG, "ストレージ開始　file$file")}
        val path = System.currentTimeMillis()
        val user = mAuth.currentUser
        val storage = Firebase.storage
        val storageRef = storage.reference
        val photoRef = storageRef.child("${user!!.uid}/$path.mp4")
        val movieUri = Uri.fromFile(file)
        val uploadTask = photoRef.putFile(movieUri)
        val prefs = context.getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)

        uploadTask
        .addOnFailureListener {
            if(MainActivity.debag){Log.i(MainActivity.TAG, "ストレージへ保存しっぱい")}
            prefs.edit().putInt(context.getString(R.string.progress),0).apply()
        }.addOnSuccessListener {
            if(MainActivity.debag){Log.i(MainActivity.TAG, "ストレージへ保存せいこう")}
            prefs.edit().putInt(context.getString(R.string.progress),0).apply()
        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri: Uri? = task.result
                file.delete()
                WriteToStore(downloadUri.toString(),path)

            }
        }
    }

    fun WriteToStore(fileName:String,path:Long){
        val user = mAuth.currentUser
        val docRef = db.collection(user!!.uid)
        val date= Calendar.getInstance().getTime()
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日HH時mm分")//
        val StrDate =dateFormat.format(date).toString()
        val data = hashMapOf(
                "friend" to false,
                "uri" to fileName,
                "date" to StrDate,
                "like" to 0,
                "path" to path
        )

        docRef.document(StrDate).set(data)
        //.addOnSuccessListener { Log.e("TAG", "動画作成日とURIの保存成功") }
        //.addOnFailureListener { e -> Log.e("TAG", "保存失敗", e) }
    }


    fun editScores(context: Context,mTimerSec: Int,IntensityPoint:Int):Int{

        var newCon: Int = 0//連続
        var newRec: Int = 0//復活
        var newtot: Int = 0//総日数
        var point = 0.0//スコア
        var newTotP = 0//総スコア
        val prefs = context.getSharedPreferences(
                "preferences_key_sample",
                Context.MODE_PRIVATE
        )
        val save: SharedPreferences.Editor = prefs.edit()

        val setday: String? = prefs.getString(context.getString(R.string.prefs_dayly_check), "2021-01-28")//前回利用した日
        val now = LocalDate.now() //2019-07-28T15:31:59.754
        val day1 = LocalDate.parse(setday)//2019-08-28T10:15:30.123
        val different = ChronoUnit.DAYS.between(day1, now).toInt() // diff: 30

        val continuous = prefs.getInt(context.getString(R.string.score_continuous), 0)
        val recover = prefs.getInt(context.getString(R.string.score_recover), 0)
        val totalD = prefs.getInt(context.getString(R.string.score_totalDay), 0)
        val totalT = prefs.getInt(context.getString(R.string.score_totalTime), 0)
        var DoNot = prefs.getInt(context.getString(R.string.score_doNotDay), 0)
        val totalPoint = prefs.getInt(context.getString(R.string.score_totalPoint), 100)

        val listRandam = arrayOf(1.4, 1.2, 1.0, 0.8, 0.6)//スコアのランダム要素
        val ran = listRandam.random()

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month= cal.get(Calendar.MONTH)//　　注意　　1月が　0　１２月が　11
        val key = "$year-$month"
        val jsonList:ArrayList<String> = loadArrayList(context,key)
        //val numbers = arrayListOf<String>()

        if (different == 1) {
            newCon = continuous + 1//継続日数
            newRec = recover//復活数
            newtot = totalD + 1//総日数
            point = 100 * newtot * ran + IntensityPoint//その日のスコア値

            jsonList.add(IntensityPoint.toString())//今日の分のスコア
            //numbers.addAll(jsonList)
            //numbers.add()

        } else if (different >= 2) {
            newCon = 0//継続リセット
            newRec = recover + 1//復活数
            newtot = totalD + 1//総日数
            DoNot = DoNot + different - 1
            point = 100 * newtot * ran + IntensityPoint//その日のスコア値

            if(different > now.dayOfMonth){// さぼりが翌月まで続いている場合
                for (i in 2..now.dayOfMonth) {
                    jsonList.add("")
                }
                if(MainActivity.debag){Log.e("tag", "翌月までさぼり続いてるパティーン")}
            }else{
                for (i in 2..different) {// 今月中のさぼりは今月分にカウントされる
                    jsonList.add("")
                }
                if(MainActivity.debag){Log.e("tag", "今月のさぼりパティーン")}
            }
            //jsonList.forEach{ numbers.add(it) }
            jsonList.add(IntensityPoint.toString())//今日の分のスコア

        } else if (different == 0) {
            newCon = continuous//継続日数
            newRec = recover//復活数
            newtot = totalD//総日数
        }
        if(MainActivity.debag){Log.e("tag", "生成された配列は$jsonList ")}
        CameraDialogFragment2.saveArrayList(context,key, jsonList)

        //継続日数の最長値を保存する
        val MAX: Int = prefs.getInt(context.getString(R.string.score_MAX), 0)
        if (MAX < newCon) {
            val updatedMAX = newCon
            save.putInt(context.getString(R.string.score_MAX), updatedMAX)
        }

        /*
        //ワンワン機能
        val wanwanIsOn = FirstFragment().wanwan(prefs)
        if(wanwanIsOn){
            Log.e("TAG", "ワンワン　×１．２倍まえの経験値は$point")
            bonus.text = "ワンワン　×１．２倍"
            point = point*1.2
        }
                */

        //トータル経験値の算出
        newTotP = totalPoint + point.toInt()

        /*
        Log.e("TAG", "ランダムは$ran")
        Log.e("TAG", "総日数はは$newtot")
        Log.e("TAG", "経験値は$point")
        Log.e("TAG", "とーたる経験値は$newTotP")
                */
        val newnum = totalT + mTimerSec//総活動時間
        save.putInt(context.getString(R.string.score_continuous), newCon)
        save.putInt(context.getString(R.string.score_recover), newRec)
        save.putInt(context.getString(R.string.score_totalDay), newtot)
        save.putInt(context.getString(R.string.score_totalTime), newnum)
        save.putInt(context.getString(R.string.score_doNotDay), DoNot)
        save.putInt(context.getString(R.string.score_totalPoint), newTotP)
        save.putString(context.getString(R.string.prefs_dayly_check),now.toString())

        save.apply()

        /*
        val level = calculate(newTotP, 450, -450, 100)
        customView.revel.text = "               Lv. $level"
        customView.text.text = "     継続日数　  　$newCon 日"
        customView.text2.text = "     復活回数　　 $newRec 回"
        customView.score.text = "     経験値              ${point.toInt()}"

         */
        return point.toInt()

    }
    companion object{
        fun calculate(y:Int,a:Int,b:Int,c:Int):Int{
            //二次関数の解の公式
            val x = (-(b) + Math.sqrt((b * b - 4 * a * (c - y)).toDouble())) / (2 * a)
            return x.toInt()
        }
    }
}