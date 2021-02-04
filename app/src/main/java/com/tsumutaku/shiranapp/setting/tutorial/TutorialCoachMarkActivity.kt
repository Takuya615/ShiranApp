package com.tsumutaku.shiranapp.setting.tutorial

import android.app.Activity
import android.content.Context
import android.content.Intent

import android.content.SharedPreferences
import android.provider.Settings
import android.view.MenuItem

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

import com.takusemba.spotlight.OnSpotlightStateChangedListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import com.takusemba.spotlight.target.SimpleTarget
import com.tsumutaku.shiranapp.setting.mRealm

import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.camera.CameraXActivity


class TutorialCoachMarkActivity(context:Context) {

    val prefs = context.getSharedPreferences( "preferences_key_sample", Context.MODE_PRIVATE)
    val Tuto1 : Boolean = prefs.getBoolean("Tuto1",false)
    val Tuto2 : Boolean = prefs.getBoolean("Tuto2",false)
    val Tuto3 : Boolean = prefs.getBoolean("Tuto3",false)
    val Tuto4 : Boolean = prefs.getBoolean("Tuto4",false)
    val Tuto5 : Boolean = prefs.getBoolean("Tuto5",false)
    val TutoForGallery : Boolean = prefs.getBoolean("TutoForGallery",false)
    val g : SharedPreferences.Editor = prefs.edit()

    fun TutoForGallery() {
        g.putBoolean("TutoForGallery", true)
        g.commit()
    }

    //メイン画面でのコーチマーク
    fun CoachMark1(activity: Activity,context: Context){
        if(!Tuto1){
            g.putBoolean("Tuto1", true)
            g.commit()

            val target1 = activity.findViewById<FloatingActionButton>(R.id.fab)
            val Target1 = sreateCircleUI(target1,activity,"　１日１回\n ココからカメラへ移動します","",0f,0f,-2f)

            // コーチマークを作成
            Spotlight.with(activity)
                // コーチマーク表示される時の背景の色
                .setOverlayColor(R.color.colorCoachMark)
                // 表示する時間
                .setDuration(1000L)
                // 表示するスピード
                .setAnimation(DecelerateInterpolator(1f))
                // 注目されたいところ（複数指定も可能）
                .setTargets(Target1)
                // 注目されたいところ以外をタップする時に閉じられるかどうか
                .setClosedOnTouchedOutside(true)
            // コーチマーク表示される時になんかする
            .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                override fun onStarted() {
                    //Toast.makeText(context, "spotlight is started", Toast.LENGTH_SHORT).show()
                }
                override fun onEnded() {
                    //val intent= Intent(context, CameraXActivity::class.java)
                    //activity.startActivity(intent)
                    //activity.recreate()
                }
            })
                .start()
        }
    }

//メイン画面でタスクボタンのコーチマーク
    fun CoachMark2(activity: Activity,context: Context){
        if(!Tuto2){//true
            g.putBoolean("Tuto2", true)
            g.commit()

            val target1 = activity.findViewById<BottomNavigationView>(R.id.nav_view)
            val x = target1.width.toFloat()
            val y = target1.height.toFloat()
            val Target1 = sreateUI(target1,activity,"　カンタンすぎましたか？",
                "より毎日つづけやすくするため、１回の撮影時間はほんの数秒です。\n ただし、目標時間は３日おきに伸びていきます。",-x,-y,-8f)

            val Target2 = sreateUI(target1,activity,"　継続できる人に", "週4日以上を、６０日間つづけられれば、そのまま習慣になる確率がぐっと上がります。\n" +
                    "今後、新たな習慣化テクニックも導入していくので、一緒に頑張りましょう。",-x,-y,-8f)



            // コーチマークを作成
            Spotlight.with(activity)
                // コーチマーク表示される時の背景の色
                .setOverlayColor(R.color.colorCoachMark)
                // 表示する時間
                .setDuration(500L)
                // 表示するスピード
                .setAnimation(DecelerateInterpolator(1f))
                // 注目されたいところ（複数指定も可能）
                .setTargets(Target1,Target2)//
                // 注目されたいところ以外をタップする時に閉じられるかどうか
                .setClosedOnTouchedOutside(true)
                .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                    override fun onStarted() {    }
                    override fun onEnded() { }// CoachMark2pluse(activity,context)
                })
                .start()
        }
    }




//カメラ撮影画面でのコーチマーク
    fun CoachMark3(activity: Activity,context: Context){
        if(!Tuto3){
            g.putBoolean("Tuto3", true)
            g.commit()

            val small=prefs.getInt(activity.getString(R.string.prefs_smalltime),0)

            val target = activity.findViewById<TextView>(R.id.goal_timer)
            val Target = sreateUI(target,activity,"ここに目標時間が表示されます",
                "腹筋やスクワットなど、好きな運動を制限時間までやりましょう。",0f,0f,3f)
            /*
            val target2 = activity.findViewById<ImageButton>(R.id.capture_button)
            val Target2 = sreateCircleUI(target2,activity,"ストレージとプライバシー",
                "この動画は外部サーバーに保存されるため、端末のストレージを圧迫することはありません。" +
                        "\nまた、撮影した動画は、あなたが許可を出したユーザーしか閲覧できない仕組みになっています。",0f,0f,-3f)
            */

            // コーチマークを作成
            Spotlight.with(activity)
                // コーチマーク表示される時の背景の色
                .setOverlayColor(R.color.colorCoachMark)
                // 表示する時間
                .setDuration(1000L)
                // 表示するスピード
                .setAnimation(DecelerateInterpolator(1f))
                // 注目されたいところ（複数指定も可能）
                .setTargets(Target)//,Target2
                // 注目されたいところ以外をタップする時に閉じられるかどうか
                .setClosedOnTouchedOutside(true)
                // コーチマーク表示される時になんかする
                .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                    override fun onStarted() {
                    }
                    override fun onEnded() {
                    }
                })
                .start()
        }

    }
/*
    //フレンド　リストの
    fun CoachMark4(activity: Activity,context: Context){
        if(!Tuto4){


            val firstTarget = SimpleTarget.Builder(activity)
                .setShape(Circle(0f))//ハイライトの大きさ
                .setTitle("  フレンドリスト")
                .setDescription("ここでは信頼できる家族や友人と、\n日頃の努力を共有することができます。")
                .setOverlayPoint(2f,500f )//文字列の位置
                .build()

            val target = activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            val targetLocation = IntArray(2)
            target.getLocationInWindow(targetLocation)
            val targetX = targetLocation[0] + target.width*9f/10f
            val targetY = targetLocation[1] + target.height/2f
            val targetRadius = 100f
            val Target2 = SimpleTarget.Builder(activity)
                .setPoint(targetX,targetY)//ハイライトの位置
                .setShape(Circle(targetRadius))//ハイライトの大きさ
                .setTitle("  🔍をタップ")
                .setDescription("ここからフレンドを探して、リクエストを送りましょう")
                .setOverlayPoint(2f, targetLocation[1] + target.height * 4f)//文字列の位置
                .build()


            // コーチマークを作成
            Spotlight.with(activity)
                // コーチマーク表示される時の背景の色
                .setOverlayColor(R.color.colorCoachMark)
                // 表示する時間
                .setDuration(1000L)
                // 表示するスピード
                .setAnimation(DecelerateInterpolator(1f))
                // 注目されたいところ（複数指定も可能）
                .setTargets(firstTarget,Target2)//
                // 注目されたいところ以外をタップする時に閉じられるかどうか
                .setClosedOnTouchedOutside(true)
                // コーチマーク表示される時になんかする
                .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                    override fun onStarted() {
                    }
                    override fun onEnded() {
                    }
                })
                .start()
        }
    }

 */

    //フレンドリクエスト画面でのコーチマーク
    fun CoachMark5(activity: Activity,context: Context){
        if(!Tuto4){
            g.putBoolean("Tuto4", true)
            g.commit()

            val user= FirebaseAuth.getInstance().currentUser
            val myName= mRealm().UidToName(user!!.uid)
            val Target = SimpleTarget.Builder(activity)
                .setShape(Circle(0f))//ハイライトの大きさ
                .setTitle("  試しに使ってみよう")
                .setDescription("自分の名前「$myName」で検索し、リクエストしてみましょう\nフレンドリストに戻るとあなたからのリクエストが届きます。")
                .setOverlayPoint(2f,500f )//文字列の位置
                .build()

            // コーチマークを作成
            Spotlight.with(activity)
                // コーチマーク表示される時の背景の色
                .setOverlayColor(R.color.colorCoachMark)
                // 表示する時間
                .setDuration(1000L)
                // 表示するスピード
                .setAnimation(DecelerateInterpolator(1f))
                // 注目されたいところ（複数指定も可能）
                .setTargets(Target)
                // 注目されたいところ以外をタップする時に閉じられるかどうか
                .setClosedOnTouchedOutside(true)
                // コーチマーク表示される時になんかする
                .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                    override fun onStarted() {
                    }
                    override fun onEnded() {
                    }
                })
                .start()
        }

    }

    //スケットを呼ぶ
    fun CoachMark6(activity: Activity,context: Context){
        if(!Tuto5){
            g.putBoolean("Tuto5", true)
            g.commit()


            val firstTarget = SimpleTarget.Builder(activity)
                .setShape(Circle(0f))//ハイライトの大きさ
                .setTitle("  スケット")
                .setDescription("  ここではあなたがより続けやすくなるように、あらゆる手を使ってスケットたちが応援してくれます。" +
                        "\n自分にあった方法を彼らと共にさがしていきましょう")
                .setOverlayPoint(2f,500f )//文字列の位置
                .build()

            // コーチマークを作成
            Spotlight.with(activity)
                // コーチマーク表示される時の背景の色
                .setOverlayColor(R.color.colorCoachMark)
                // 表示する時間
                .setDuration(1000L)
                // 表示するスピード
                .setAnimation(DecelerateInterpolator(1f))
                // 注目されたいところ（複数指定も可能）
                .setTargets(firstTarget)
                // 注目されたいところ以外をタップする時に閉じられるかどうか
                .setClosedOnTouchedOutside(true)
                // コーチマーク表示される時になんかする
                .setOnSpotlightStateListener(object : OnSpotlightStateChangedListener {
                    override fun onStarted() {
                    }
                    override fun onEnded() {
                    }
                })
                .start()

        }

    }











//引数（ボタン、アクティビティ、タイトル、本文、光X軸より広く、光Y軸より広く、文字列下なら正、文字列上なら負のFloat）　　四角の場合
    fun sreateUI(target:View,activity:Activity,title:String?,scrip:String,plusX:Float,plusY:Float,plusP:Float):SimpleTarget{

        val targetLocation = IntArray(2)
        target.getLocationInWindow(targetLocation)
        val targetX = targetLocation[0] + target.width/2f
        val targetY = targetLocation[1] + target.height/2f

        val UIwidth = target.width.toFloat() + plusX
        val UIheight = target.height.toFloat() + plusY

        //val OverLayPointY = targetLocation[1] + target.height * plusP

        // 注目されたいところを設定する
        val firstTarget = SimpleTarget.Builder(activity)
            .setPoint(targetX,targetY)//ハイライトの位置
            .setShape(RoundedRectangle(UIheight,UIwidth,25f))//ハイライトの大きさ
            .setTitle(title)
            .setDescription(scrip)
            .setOverlayPoint(2f,targetLocation[1] + target.height * plusP )//文字列の位置
            .build()

        return firstTarget
    }

    //引数（ボタン、アクティビティ、タイトル、本文、光円半径、なし、文字列下なら正、文字列上なら負のFloat）　　円の場合
    fun sreateCircleUI(target:View,activity:Activity,title:String?,scrip:String,plusX:Float,plusY:Float,plusP:Float):SimpleTarget{

        val targetLocation = IntArray(2)
        target.getLocationInWindow(targetLocation)
        val targetX = targetLocation[0] + target.width/2f
        val targetY = targetLocation[1] + target.height/2f
        val targetRadius = 78f + plusX
        val firstTarget = SimpleTarget.Builder(activity)
            .setPoint(targetX,targetY)//ハイライトの位置
            .setShape(Circle(targetRadius))//ハイライトの大きさ
            .setTitle(title)
            .setDescription(scrip)
            .setOverlayPoint(2f, targetLocation[1] + target.height * plusP)//文字列の位置
            .build()


        return firstTarget
    }


    fun reset(){
        //g.putBoolean("Tuto0", false)
        g.putBoolean("Tuto1", false)
        g.putBoolean("Tuto2", false)
        g.putBoolean("Tuto3", false)
        g.putBoolean("Tuto4", false)
        g.putBoolean("Tuto5", false)
        g.commit()
    }
}