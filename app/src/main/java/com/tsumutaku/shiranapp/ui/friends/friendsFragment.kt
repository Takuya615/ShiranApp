package com.tsumutaku.shiranapp.ui.friends

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.mRealm
import kotlinx.android.synthetic.main.fragment_friends.*

class friendsFragment : Fragment() {

    private lateinit var notificationsViewModel: friendsViewModel
    private lateinit var adapter : FriendListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mnameList:MutableList<String>
    private lateinit var muidList:MutableList<String>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        activity?.run { notificationsViewModel = ViewModelProvider(this).get(friendsViewModel::class.java) }

        val root = inflater.inflate(R.layout.fragment_friends, container, false)
        //val textView: TextView = root.findViewById(R.id.text_notifications)
        //notificationsViewModel.text.observe(viewLifecycleOwner, Observer { textView.text = it })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //(activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        muidList = mutableListOf<String>()
        mnameList = mutableListOf<String>()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        //フレンドリクエストが来ている場合、ダイアログを表示するYesなら、相手のUidをドキュメントとしてFirebaseに追加する
        val user = mAuth.currentUser
        progressBar_friends?.visibility=View.VISIBLE

        //val myName = mRealm().UidToName(user.toString())
        muidList.add(user!!.uid)
        mnameList.add("マイビデオ")



        //-----------------リクエストが来ていれば受け取る -------------------------
        val docRef = db.collection(user.uid).document("friendRequest")//whereEqualTo("request",true)
        docRef.get().addOnSuccessListener { document ->
            if(document.data != null){
                val friendUid = document.data!!["friendUid"]
                if(friendUid !=null){

                    val requestName = mRealm().UidToName(friendUid.toString())
                    //ダイアログ
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())
                    alertDialogBuilder.apply {
                        setTitle("$requestName　さんからフレンドリクエストが届いてます")
                        setMessage("フレンドになると、その人の日々のミッションを応援してあげることができます\n\nリクエストを承認しますか？")
                        setPositiveButton("承認する"){dialog, which ->

                            val checkSameName = muidList.contains(friendUid.toString())//すでに同じユーザーが登録されている場合はダメ
                            if(checkSameName){
                                docRef.set(hashMapOf("friendUid" to null ))
                                Toast.makeText(requireContext(), "同じひとをリストへ加えることはできません", Toast.LENGTH_LONG).show()
                            }else{
                                val docRef2 = db.collection(user.uid).document(requestName)
                                val map = hashMapOf(
                                    "friend" to true,
                                    "name" to requestName,
                                    "uid" to friendUid//.toString()
                                )
                                docRef2.set(map)
                                    .addOnSuccessListener {

                                        mnameList.add(requestName)
                                        muidList.add(friendUid.toString())
                                        adapter.notifyDataSetChanged()
                                    }
                                //保存されているデータをnullに戻さないと。。。
                                docRef.set(hashMapOf("friendUid" to null ))
                            }

                        }
                        setNegativeButton("見なかったことにする"){dialog, which ->
                            //保存されているデータをnullに戻さないと。。。
                            docRef.set(hashMapOf("friendUid" to null ))
                        }
                    }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }

            }
        }//.addOnFailureListener { e -> Log.e("TAG", "データ取得に失敗", e) }


        //-------------------------フレンドがいればリストに表示する---------------------------------
        db.collection(user.uid).whereEqualTo("friend", true).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.data["name"].toString()
                    val uid = document.data["uid"].toString()
                    mnameList.add(name)
                    muidList.add(uid)
                    adapter.notifyDataSetChanged()
                    progressBar_friends?.visibility=View.INVISIBLE

                }
            }
        //.addOnFailureListener { exception -> Log.w("TAG", "Error getting documents: ", exception) }

        adapter = FriendListAdapter(mnameList)
        layoutManager = LinearLayoutManager(requireContext())
        // アダプターとレイアウトマネージャーをセット
        val itemDecoration = DividerItemDecoration(requireActivity().applicationContext, DividerItemDecoration.VERTICAL) // こいつが必要
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        // インターフェースの実装
        adapter.setOnItemClickListener(object:FriendListAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                when(view.getId()){
                    R.id.itemTextView -> {
                        //val friendName = mnameList[position]
                        val friendUid = muidList[position]

                        notificationsViewModel.modelUid = friendUid//viewmodelで値を共有する
                        findNavController().navigate(R.id.action_friendsToVideoList)
                        //Toast.makeText(applicationContext, "${clickedText}がタップされました", Toast.LENGTH_LONG).show()
                    }
                    R.id.itemdeleate -> {

                        db.collection(user.uid).document(clickedText).delete()
                        //Toast.makeText(applicationContext, "${clickedText}を削除しました", Toast.LENGTH_LONG).show()
                        mnameList.remove(mnameList[position])
                        muidList.remove(muidList[position])
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(position, mnameList.size)

                    }
                }
            }
        })

    }

}