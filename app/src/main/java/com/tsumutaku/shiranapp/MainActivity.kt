package com.tsumutaku.shiranapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.tsumutaku.shiranapp.camera.CameraXActivity
import com.tsumutaku.shiranapp.camera.CameraXXActivity
import com.tsumutaku.shiranapp.camera.EditWorker
import com.tsumutaku.shiranapp.setting.AccountSettingActivity
import com.tsumutaku.shiranapp.setting.LoginActivity
import com.tsumutaku.shiranapp.setting.PrivacyPolicyActivity
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import com.tsumutaku.shiranapp.ui.home.HomeViewModel
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader


class MainActivity : AppCompatActivity() {
    private val home:HomeViewModel = HomeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        System.loadLibrary("opencv_java4")
        /*if (OpenCVLoader.initDebug()) {
            mLoaderCallback?.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }*/
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_medal, R.id.navigation_video_list))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            //progressbar.visibility = android.widget.ProgressBar.VISIBLE
            if(!MainActivity.debag){
                val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "DAYLY-MOVIE")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            }
            val intent= Intent(this, CameraXActivity::class.java)
            startActivity(intent)
            finish()
        }

        charenge.setOnClickListener {
            //¸イベントログ
            if(!MainActivity.debag){
                val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.METHOD, "CHARENGE-MOVIE")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            }
            val intent= Intent(this, CameraXXActivity::class.java)
            startActivity(intent)
            finish()
        }

        val user= FirebaseAuth.getInstance().currentUser
        if(user==null){//ログインしていなければ
            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        val Tuto = intent.getIntExtra("Tuto",0)
        coachMark(Tuto)


        val prefs = getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
        val progress = prefs.getInt(getString(R.string.progress),0)
        if (progress == 1){
            val file = intent.getStringExtra("file")
            val time = intent.getIntExtra("time",0)
            home.WMrequest(this,file!!,time)
        }





    }

    fun coachMark(TutoNum:Int){
        //val prefs = getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
        //val Tuto1 : Boolean = prefs.getBoolean("Tuto1",false)
        //val Tuto2 : Boolean = prefs.getBoolean("Tuto2",false)
        val Coach = TutorialCoachMarkActivity(this)
        val handler = Handler(Looper.getMainLooper())
        when(TutoNum){
            100 ->{
                handler.postDelayed({
                    Coach.CoachMark1(this,this)//activity,context
                },1000)
            }
            200 -> {
                handler.postDelayed({
                    Coach.CoachMark2(this,this)//activity,context
                },1000)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent2= Intent(this, LoginActivity::class.java)
        val intent3= Intent(this, AccountSettingActivity::class.java)
        val intent4= Intent(this, PrivacyPolicyActivity::class.java)

        when (item.itemId) {
            /*   ¸友人の検索エンジン
            R.id.action_search -> {
                startActivity(Intent(this, FriendSearchActivity::class.java))
                true
            }*/
            R.id.action_settings -> {
                startActivity(intent3)
                finish()
            }
            R.id.action_logout->{FirebaseAuth.getInstance().signOut()
                Toast.makeText(this,"ログアウトしました", Toast.LENGTH_LONG).show()
                startActivity(intent2)
                finish()
            }
            R.id.action_privacy_policy -> startActivity(intent4)
        }
        return super.onOptionsItemSelected(item)
    }
/*
    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            mLoaderCallback?.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        }

    }*/
    //opencvの初期化メソッド？
    private var mLoaderCallback: BaseLoaderCallback? = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    if(MainActivity.debag){Log.d(TAG, "OpenCV loaded successfullyせいこう")}
                }
                else -> {
                    super.onManagerConnected(status)
                    if(MainActivity.debag){Log.d(TAG, "OpenCV loaded しっぱいしてるよ")}
                }
            }
        }
    }
//
    companion object{
        val debag = false//　　　　　デバック時　全てのログとFirebaseイベントログを作動させない 本番でfalse
        val TAG = "MainActivity"
    }
}