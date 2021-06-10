package com.tsumutaku.shiranapp.setting

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tsumutaku.shiranapp.MainActivity
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.camera.CameraDialogFragment2
import com.tsumutaku.shiranapp.setting.tutorial.TutorialCoachMarkActivity
import kotlinx.android.synthetic.main.activity_friend_search.*

class FriendSearchActivity : AppCompatActivity() {

    private lateinit var uid :String
    private lateinit var name :String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_search)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "サポーター検索"
        request.visibility = View.INVISIBLE
        //uid =""val list = mutableListOf<String>(name,uid)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        Handler().postDelayed({
            val Coach = TutorialCoachMarkActivity(this)
            Coach.CoachMark5(this,this)
        },1000)


        val requestedList = CameraDialogFragment2.loadArrayList(this,getString(R.string.prefs_support_requested))
        val horizontalScrollView = findViewById<LinearLayout>(R.id.horizontalView)
        for (i in 0..requestedList.size-1){
            val button = Button(this)
            button.text = requestedList[i]
            horizontalScrollView.addView(button)

            button.setOnClickListener {
                val dialog = AlertDialogs()
                dialog.title = "サポーターから外しますか？"
                dialog.message = "${requestedList[i]}さんがあなたの動画を確認できなくなります。"
                dialog.onOkClickListener = DialogInterface.OnClickListener { dialog, which ->
                    horizontalScrollView.removeView(it)
                    requestedList.remove(requestedList[i])
                    deleteSupporter(requestedList[i],requestedList)

                }
                dialog.show(supportFragmentManager, null)
            }
        }





        request.setOnClickListener{
            progressBar_search?.visibility=View.VISIBLE
            val user = mAuth.currentUser
            if(user!=null){
                val docRef = db.collection(uid).document("friendRequest")
                docRef.set(hashMapOf("friendUid" to user.uid))
                    .addOnSuccessListener {

                        Toast.makeText(this,"リクエストを送りました", Toast.LENGTH_LONG).show()
                        requestedList.add(name)
                        CameraDialogFragment2.saveArrayList(this,getString(R.string.prefs_support_requested),requestedList)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()

                        try {
                            //友人にリクエストを送ったのか、イベントログを記録する。他の人へオススメしたという指標になる
                            val bundle:Bundle = Bundle()
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "friend_request")
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "friend_request")
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "friend_request")
                            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                        }catch (e:Exception){
                            if(MainActivity.debag){Log.d("tag","リクエスト送った記録は取れてない")}
                        }


                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"リクエスト送信に失敗しました", Toast.LENGTH_LONG).show()
                        progressBar_search?.visibility=View.INVISIBLE
                    }//e -> Log.e("TAG", "ドキュメントの作成・上書きエラー", e)
            }
        }

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = findViewById<SearchView>(R.id.search1123)//menu.findItem(R.id.app_bar_search).actionView as SearchView
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        searchView?.setSearchableInfo(searchableInfo)

        searchView?.isIconified

        searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val ans = mRealm().SearchName(query)
                if(ans.AcUid.isEmpty()){
                    answer.text=ans.Name
                    request.visibility = View.INVISIBLE
                }else{
                    answer.text="${ans.Name}さんが見つかりました\nサポートリクエストを送りますか？"
                    name = ans.Name
                    uid = ans.AcUid
                    request.visibility = View.VISIBLE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }

    fun deleteSupporter(supporterName:String,requestedList:ArrayList<String>){
        val supporterUid = mRealm().NameToUid(supporterName)
        val docRef = db.collection(supporterUid)
        docRef.document("friendRequest").delete()
        docRef.document(supporterName).delete()
                .addOnSuccessListener {
                    Toast.makeText(this,"削除しました", Toast.LENGTH_LONG).show()
                    CameraDialogFragment2.saveArrayList(this,getString(R.string.prefs_support_requested),requestedList)
                }
                .addOnFailureListener {
                    Toast.makeText(this,"削除に失敗しました。\nもう一度お試しください", Toast.LENGTH_LONG).show()
                }

    }

    //戻るボタンを押すと今いるviewを削除する
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }

}