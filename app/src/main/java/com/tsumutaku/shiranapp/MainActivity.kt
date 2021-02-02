package com.tsumutaku.shiranapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.tsumutaku.shiranapp.camera.CameraXActivity
import com.tsumutaku.shiranapp.setting.AccountSettingActivity
import com.tsumutaku.shiranapp.setting.LoginActivity
import com.tsumutaku.shiranapp.setting.PrivacyPolicyActivity
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import io.realm.Realm

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_video, R.id.navigation_friends))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            //progressbar.visibility = android.widget.ProgressBar.VISIBLE
            val intent= Intent(this, CameraXActivity::class.java)
            startActivity(intent)
            coachMark()
        }

        val user= FirebaseAuth.getInstance().currentUser
        if(user==null){//ログインしていなければ
            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
            coachMark()
        }

    }

    fun coachMark(){
        val prefs = getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
        val Coach = TutorialCoachMarkActivity(this)

        val Tuto1 : Boolean = prefs.getBoolean("Tuto1",false)
        val Tuto2 : Boolean = prefs.getBoolean("Tuto2",false)

        Log.d("tag","このタイミングでは、 tuto1が$Tuto1 tuto2が$Tuto2")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            if (!Tuto1){
                Coach.CoachMark1(this,this)
            } else if(Tuto1&&!Tuto2){
                Coach.CoachMark2(this,this)
            }

        },1000)

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
            R.id.action_settings -> startActivity(intent3)

            R.id.action_logout->{FirebaseAuth.getInstance().signOut()
                Toast.makeText(this,"ログアウトしました", Toast.LENGTH_LONG).show()
                startActivity(intent2)
            }
            R.id.action_privacy_policy -> startActivity(intent4)
        }
        return super.onOptionsItemSelected(item)
    }
}