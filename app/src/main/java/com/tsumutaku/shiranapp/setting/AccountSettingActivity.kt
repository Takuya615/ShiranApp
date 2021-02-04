package com.tsumutaku.shiranapp.setting

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import kotlinx.android.synthetic.main.activity_account_setting.*

class AccountSettingActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//表示
        title = "アカウント名設定"

        val prefs = getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
        val TutoDummy : Boolean = prefs.getBoolean("TutoDummy",false)
        val Tuto1 : Boolean = prefs.getBoolean("Tuto1",false)
        val Tuto2 : Boolean = prefs.getBoolean("Tuto2",false)
        Log.d("tag","dummy$TutoDummy tuto1が$Tuto1 tuto2が$Tuto2")

        /*
        Handler().postDelayed({
            val Coach = TutorialCoachMarkActivity(this)
            Coach.CoachMark6(this,this)
        }, 1000)
         */

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val setName: String? = mRealm().UidToName(user!!.uid)//同じUidのアカウント名を返す
        accountName.text = Editable.Factory.getInstance().newEditable(setName)


        set_button.setOnClickListener{
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val Name = accountName.text.toString()
            val uid = user.uid.toString()
            if(mRealm().SearchSameName(Name)){
                val a = mRealm().UidToName(user.uid)//
                if(a.isEmpty()){
                    mRealm().addPerson(Name,uid)
                    Toast.makeText(this,"アカウント名を登録しました", Toast.LENGTH_SHORT).show()
                    //supportFragmentManager.beginTransaction().replace(R.id.frameLayout, FriendListFragment()).commit()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("Tuto",100)
                    startActivity(intent)
                    finish()
                    //val intent= Intent(this, MainActivity::class.java)
                    //startActivity(intent)
                }else{
                    mRealm().update(uid,Name)
                    Snackbar.make(it, "アカウント名を変更しました", Snackbar.LENGTH_LONG).show()
                    finish()
                }

            }else{
                Snackbar.make(it, "このアカウント名はすでに使われています。ちがう名前を設定してください", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    //戻るボタンを押すと今いるviewを削除する
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}