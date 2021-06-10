package com.tsumutaku.shiranapp.camera

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.applandeo.materialcalendarview.CalendarUtils
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import org.json.JSONArray
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class CameraDialogFragment2: DialogFragment(){

    lateinit var customView: View
    lateinit var calendarView:CalendarView
    lateinit var numbers:ArrayList<String>
    private var year = 0
    private var month = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        customView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_camera2, null)

        val builder = AlertDialog.Builder(activity)
        //alertDialogBuilder.setTitle("活動の記録")
        //builder.setItems(list){ dialog, which -> Log.e("TAG", "${list[which]} が選択されました") }
        //builder.setPositiveButton("メイン画面") { dialog, which -> requireActivity().finish() }
        builder.setView(customView)

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return customView//inflater.inflate(R.layout.dialog_camera, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        calendarView = customView.findViewById<CalendarView>(R.id.calendarView)
        val cal = Calendar.getInstance()
        year = cal.get(Calendar.YEAR)
        month= cal.get(Calendar.MONTH)//　　注意　　1月が　0　１２月が　11

        //----------------活動記録をリストへ保存------------
        numbers = loadArrayList(requireActivity(),"$year-$month")//saveDailyResults()
        getMarkDays(calendarView,numbers,year,month)//まず今月の結果を表示する

        calendarView.setOnForwardPageChangeListener{ //翌月の記録
            //Toast.makeText(requireContext(), "翌月の記録", Toast.LENGTH_SHORT).show()
            changePage(1)
        }

        calendarView.setOnPreviousPageChangeListener { //先月の記録
            //Toast.makeText(requireContext(), "先月の記録", Toast.LENGTH_SHORT).show()
            changePage(0)
        }

        //カレンダーの日付のクリックイベント設定
        calendarView.setOnDayClickListener(OnDayClickListener { eventDay ->
            val nowCalendar = eventDay.calendar
            val date = nowCalendar.get(Calendar.DATE)//.toString()
            val score = numbers[date-1]
            if(score.isNotEmpty()){
                Toast.makeText(
                        requireContext(),
                        score,
                        Toast.LENGTH_SHORT
                ).show()
            }

        })


    }

    //-------------------------ここからカレンダーにイベントを記述するメソッド-----------------------------
    private fun getMarkDays(calendarView: CalendarView?, list: ArrayList<String>,Year:Int,Month:Int) {
        val loadlist = editNumbers(list)
        val events = ArrayList<EventDay>()

        if(MainActivity.debag){Log.e("tag", "カレンダーに反映するリストは$loadlist ") }

        for (i in 0..loadlist.size - 1) {
            val calendar = Calendar.getInstance()
            calendar.set(Year,Month,i+1)//monthの指定は、月-1 で指定する   Year,Month-1,i+5
            val text = CalendarUtils.getDrawableText(requireContext(), loadlist[i].toString(), null, android.R.color.holo_green_dark, 15)
            events.add(EventDay(calendar, text))
        }
        calendarView?.setEvents(events)
    }

    //500を.5K  5,000を 5K　50,000= 50K  500,000= .5M
    fun editNumbers(numbers:ArrayList<String>):ArrayList<String>{
        val list = arrayListOf<String>()
        val K = 1000..999999
        val M = 1000000..9999999
        for (i in 0..numbers.size-1){
            if (numbers[i].isEmpty()){
                list.add(numbers[i])
            }else{
                val number = numbers[i].toInt()
                if (number<1000){
                    list.add(number.toString())
                }
                if( number in K){
                    list.add("${number/1000}K")
                }
                if( number in M){
                    list.add("${number/1000000}M")
                }
                if(number>9999999){
                    list.add("E${number.toString().length}")
                }
            }
        }
        return list
    }

    //
    fun changePage(pass:Int){//pass 0=前月 1=後月
        when(pass){
            0 -> month = month - 1
            1 -> month = month + 1
        }
        if(month==-1){
            year = year - 1
            month = 12-1
        }
        if(month==12){
            year = year + 1
            month = 1-1
        }
        numbers = loadArrayList(requireActivity(),"$year-$month")
        getMarkDays(calendarView,numbers,year,month)
    }




    override fun onDestroy() {
        super.onDestroy()
        val intent= Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("Tuto",200)
        startActivity(intent)
        requireActivity().finish()
    }


    companion object{
        // リストの保存
        fun saveArrayList(context: Context,key: String, arrayList:List<Any> ) {//ArrayList<String>
            if(MainActivity.debag){Log.d("tag","保存されたリスト名は$key")}

            val prefs = context.getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
            val shardPrefEditor = prefs.edit()

            val jsonArray = JSONArray(arrayList)
            shardPrefEditor.putString(key, jsonArray.toString())
            shardPrefEditor.apply()
            if(MainActivity.debag){Log.d("tag","保存されたリスト$jsonArray")}

        }

        // リストの読み込み     初日の時間も記録したうえでlistで返す
        fun loadArrayList(context: Context,key:String): ArrayList<String> {
            if(MainActivity.debag){Log.d("tag","呼び出された　リスト名は$key")}

            val shardPreferences = context.getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)//this.getPreferences(Context.MODE_PRIVATE)

            val jsonArray = JSONArray(shardPreferences.getString(key, "[]"))//

            val list = ArrayList<String>()
            //val jsonArray = jsonObject as JSONArray?
            val len = jsonArray.length()
            for (i in 0 until len) {
                list.add(jsonArray[i].toString())
            }
            if(MainActivity.debag){Log.d("tag","呼び出された　リスト名は$list")}

            return list
        }
    }
}