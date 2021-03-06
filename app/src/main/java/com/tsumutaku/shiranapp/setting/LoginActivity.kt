package com.tsumutaku.shiranapp.setting

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.tutorial.TutorialActivity
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private var mIsCreateAccount = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        title = "ログイン　/ アカウント作成"
        TutorialActivity.showIfNeeded(this,savedInstanceState)//チューとリアル
        mAuth = FirebaseAuth.getInstance()

        // アカウント作成処理のリスナー
        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                // 成功した場合

                // ログインを行う
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            } else {
                // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "アカウント作成 失敗\n", Snackbar.LENGTH_LONG).show()//あなたの運動したくないという無意識が、妨害しています。すこし時間をおいてみましょう
                // プログレスバーを非表示にする　　　　　　　　　　　
                progressBar.visibility = View.GONE
            }
        }

        // ログイン処理のリスナー
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                //ログイン
                val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
                val bundle = Bundle()
                // 成功した場合
                if (mIsCreateAccount) {
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "Sign_Up!")
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
                } else {
                    bundle.putString(FirebaseAnalytics.Param.METHOD, "Login")
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
                }

                progressBar.visibility = View.GONE
                /*
                val prefs = getSharedPreferences( "preferences_key_sample", Context.MODE_PRIVATE)
                val Tuto1 : Boolean = prefs.getBoolean("Tuto1",false)
                if(!Tuto1){//true
                    val intent = Intent(this, AccountSettingActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                 */

                val intent = Intent(this, AccountSettingActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                // 失敗した場合              //あなたの無意識がログインを妨害しています。そんなに運動したくないのですか？？
                Toast.makeText(this,"ログイン失敗\nメールアドレスかパスワードに間違いがないか確認してください", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
        }


        createButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6) {
                // ログイン時に表示名とUidを保存するようにフラグを立てる
                mIsCreateAccount = true
                createAccount(email, password)

            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }

        loginButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.length != 0 && password.length >= 6 ) {
                // フラグを落としておく
                mIsCreateAccount = false
                login(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }

        PPbtn.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createAccount(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)
    }

    private fun login(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    /*
    //戻るボタンを押すと今いるviewを削除する
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

     */
}