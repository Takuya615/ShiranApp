package com.tsumutaku.shiranapp.camera

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import android.view.*
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.LoginActivity
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CameraDialogFragment(mTimerSec: Int): DialogFragment() {
    lateinit var customView : View
    val time = mTimerSec
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        customView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_camera, null)
        val builder = AlertDialog.Builder(activity)//Dialog(requireActivity())

        builder.setCancelable(false)
        //builder.window!!.requestFeature(Window.FEATURE_NO_TITLE);
        //builder.window!!.setFlags(WindowManager.LayoutParams.FIRST_SUB_WINDOW, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        //builder.setPositiveButton("メイン画面"){ dialog, which -> requireActivity().finish() }
        builder.setView(customView)//R.layout.dialog_camera
        //builder.findViewById<Button>(R.id.button).setOnClickListener { requireActivity().finish() }
        return builder.create()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return customView//inflater.inflate(R.layout.dialog_camera, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent= Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("Tuto",200)
        startActivity(intent)
        requireActivity().finish()
        //MainActivity().coachMark(requireActivity(),requireContext())
    }

/*




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var newCon: Int = 0//連続
        var newRec: Int = 0//復活
        var newtot: Int = 0//総日数
        var point = 0.0//スコア
        var newTotP = 0//総スコア
        val prefs = requireContext().getSharedPreferences(
                "preferences_key_sample",
                Context.MODE_PRIVATE
        )
        val save: SharedPreferences.Editor = prefs.edit()

        val setday: String? = prefs.getString("setDate", "2020-10-28")//前回利用した日
        val now = LocalDate.now() //2019-07-28T15:31:59.754
        val day1 = LocalDate.parse(setday)//2019-08-28T10:15:30.123
        val different = ChronoUnit.DAYS.between(day1, now).toInt() // diff: 30

        val continuous = prefs.getInt(getString(R.string.score_continuous), 0)
        val recover = prefs.getInt(getString(R.string.score_recover), 0)
        val totalD = prefs.getInt(getString(R.string.score_totalDay), 0)
        val totalT = prefs.getInt(getString(R.string.score_totalTime), 0)
        var DoNot = prefs.getInt(getString(R.string.score_doNotDay), 0)
        val totalPoint = prefs.getInt(getString(R.string.score_totalPoint), 100)

        val listRandam = arrayOf(1.4, 1.2, 1.0, 0.8, 0.6)//スコアのランダム要素
        val ran = listRandam.random()

        if (different == 1) {
            newCon = continuous + 1//継続日数
            newRec = recover//復活数
            newtot = totalD + 1//総日数
            point = 100 * newtot * ran//その日のスコア値

        } else if (different >= 2) {
            newCon = 0//継続リセット
            newRec = recover + 1//復活数
            newtot = totalD + 1//総日数
            DoNot = DoNot + different - 1
            point = 100 * newtot * ran//その日のスコア値

        } else if (different == 0) {
            newCon = continuous//継続日数
            newRec = recover//復活数
            newtot = totalD//総日数

        }

        //継続日数の最長値を保存する
        val MAX: Int = prefs.getInt(getString(R.string.score_MAX), 0)
        if (MAX < newCon) {
            val updatedMAX = newCon
            save.putInt(getString(R.string.score_MAX), updatedMAX)
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

        Log.e("TAG", "ランダムは$ran")
        Log.e("TAG", "総日数はは$newtot")
        Log.e("TAG", "経験値は$point")
        Log.e("TAG", "とーたる経験値は$newTotP")
        val newnum = totalT + time//総活動時間
        save.putInt(getString(R.string.score_continuous), newCon)
        save.putInt(getString(R.string.score_recover), newRec)
        save.putInt(getString(R.string.score_totalDay), newtot)
        save.putInt(getString(R.string.score_totalTime), newnum)
        save.putInt(getString(R.string.score_doNotDay), DoNot)
        save.putInt(getString(R.string.score_totalPoint), newTotP)

        save.apply()

        /*
        val level = calculate(newTotP, 450, -450, 100)
        customView.revel.text = "               Lv. $level"
        customView.text.text = "     継続日数　  　$newCon 日"
        customView.text2.text = "     復活回数　　 $newRec 回"
        customView.score.text = "     経験値              ${point.toInt()}"

         */



    }
    companion object{
        fun calculate(y:Int,a:Int,b:Int,c:Int):Int{
            //二次関数の解の公式
            val x = (-(b) + Math.sqrt((b * b - 4 * a * (c - y)).toDouble())) / (2 * a)
            return x.toInt()
        }
    }

 */
}