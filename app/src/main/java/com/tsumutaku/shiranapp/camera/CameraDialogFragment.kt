package com.tsumutaku.shiranapp.camera

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import kotlinx.android.synthetic.main.dialog_camera.*


class CameraDialogFragment(val mTimerSec: Int,val IntensityPoint:Int): DialogFragment() {
    lateinit var customView : View

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
        //if (mTimerSec == -1){//チャレンジ機能から呼び出しの場合
        //}else{
            //CameraDialogFragment2().show(requireActivity().supportFragmentManager,"calender")
        //}
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (mTimerSec == -1){//チャレンジ機能から呼び出しの場合
            dialogScore.text = "動画編集中。"
            dialogExp.text = "しばらく時間がかかります。"
        }else{
            dialogScore.text = "スコア　"+IntensityPoint.toString()//IntensityPoint.toString()
            val point = Storage().editScores(requireContext(),mTimerSec,IntensityPoint)//IntensityPoint
            if (point==0){
                dialogExp.text = "デイリー消化済み"
            }else{
                dialogExp.text = "経験値　" + point.toString()
            }
        }

    }
    companion object{
        //二次関数の解の公式
        fun calculate(y:Int,a:Int,b:Int,c:Int):Int{
            val x = (-(b) + Math.sqrt((b * b - 4 * a * (c - y)).toDouble())) / (2 * a)
            return x.toInt()
        }
    }

}