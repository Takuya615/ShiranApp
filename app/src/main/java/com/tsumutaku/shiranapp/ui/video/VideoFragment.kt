package com.tsumutaku.shiranapp.ui.video

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tsumutaku.shiranapp.R
import com.tsumutaku.shiranapp.setting.mRealm
import kotlinx.android.synthetic.main.fragment_video.*

class VideoFragment : Fragment() {

    private lateinit var dashboardViewModel: VideoViewModel
    private lateinit var adapter : VideoListAdapter
    private lateinit var mdateList:MutableList<String>
    private lateinit var muriList:MutableList<String>
    private lateinit var mlikeList:MutableList<String>
    private lateinit var mpathList:MutableList<String>
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var coll : CollectionReference
    private lateinit var userId:String
    private lateinit var UriString:String
    var isfriend:Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(VideoViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_video, container, false)
        //val textView: TextView = root.findViewById(R.id.text_dashboard)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        muriList = mutableListOf<String>()
        mdateList = mutableListOf<String>()
        mlikeList = mutableListOf<String>()
        mpathList = mutableListOf<String>()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = mAuth.currentUser

        val friendUid = requireActivity().intent.getStringExtra("friendUid")
        if(friendUid == null){
            coll =db.collection(user!!.uid)
            userId = user.uid
            isfriend = false
        }else{
            coll =db.collection(friendUid)
            userId = friendUid
            isfriend = true
        }

        val title = mRealm().UidToName(userId)
        (activity as AppCompatActivity).supportActionBar?.title = "$title さんの動画リスト"

        coll.whereEqualTo("friend", false).get()//.orderBy("date", Query.Direction.DESCENDING)
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val date = document.data["date"].toString()
                    val uri = document.data["uri"].toString()
                    val like = document.data["like"].toString()
                    val path = document.data["path"].toString()
                    mdateList.add(date)
                    muriList.add(uri)
                    mlikeList.add(like)
                    mpathList.add(path)

                }
                mdateList.reverse()
                muriList.reverse()
                mlikeList.reverse()
                mpathList.reverse()
                adapter.notifyDataSetChanged()
                progressBar2.visibility = View.INVISIBLE
            }
        //.addOnFailureListener { exception -> Log.w("TAG", "Error getting documents: ", exception) }

        adapter = VideoListAdapter(mdateList, mlikeList,isfriend)
        val layoutManager = LinearLayoutManager(requireContext())

        // アダプターとレイアウトマネージャーをセット
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        // インターフェースの実装
        adapter.setOnItemClickListener(object : VideoListAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                when (view.getId()) {
                    R.id.itemTextView -> {

                        UriString = muriList[position]
                        val intent = Intent(requireActivity(), GalleryActivity::class.java)
                        intent.putExtra("selectedName", UriString)
                        intent.putExtra("friendUid", friendUid)
                        intent.putExtra("date", mdateList[position])
                        startActivity(intent)
                        //Toast.makeText(applicationContext, "${clickedText}がタップされました", Toast.LENGTH_LONG).show()
                    }
                    R.id.itemdeleate -> {

                        coll.document(mdateList[position]).delete()

                        Toast.makeText(requireContext(), "${clickedText}を削除しました", Toast.LENGTH_LONG).show()

                        val aaa = mpathList[position]
                        val storage = Firebase.storage.reference
                        val desertRef = storage.child("$userId/$aaa.mp4")//
                        desertRef.delete()

                        mdateList.remove(mdateList[position])
                        muriList.remove(muriList[position])
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(position, mdateList.size)
                        //adapter.notifyDataSetChanged()

                    }
                }
            }
        })

    }
}