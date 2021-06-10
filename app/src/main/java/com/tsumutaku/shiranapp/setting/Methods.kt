package com.tsumutaku.shiranapp.setting

import android.content.Context
import android.graphics.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.tsumutaku.shiranapp.R

class Methods {

    fun taskTimeCaluculate(context: Context):Int{
        val prefs = context.getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
        val oneParsent = prefs.getInt(context.getString(R.string.prefs_smalltime),5)
        val totalday = prefs.getInt(context.getString(R.string.score_totalDay),0)
        var times =0//        総日数が、2日更新されるごとに、強度を上げる場合。（totalday=1なら、1/2で、times=0となる）
        var cal = oneParsent
        if(totalday>48){
            val ab = 16
            val bc = (totalday-48)/3
            times =ab+bc
        }else{
            times=totalday/3

        }
        if(times!=0 ) {//　　　割り算の演算子は整数までしか計算しないので、少数点以下は無視して出力される。
            val A = oneParsent * times
            cal = oneParsent + A
        }
        return cal
    }


    companion object{

    }
}