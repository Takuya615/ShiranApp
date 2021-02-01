package com.tsumutaku.shiranapp.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tsumutaku.shiranapp.R
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.list_item_home.view.*


class HomeFragment : Fragment() {

    class SecondFragmentAdapter(private val customList: MutableList<String>,
                                private val customList2: MutableList<String>) : RecyclerView.Adapter<SecondFragmentAdapter.CustomViewHolder>() {

        // ViewHolderクラス(別ファイルに書いてもOK)
        class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view) {
            val sampleImg = view.content2
            val sampleTxt = view.record
        }

        // getItemCount onCreateViewHolder onBindViewHolderを実装
        // 上記のViewHolderクラスを使ってViewHolderを作成
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val item = layoutInflater.inflate(R.layout.list_item_home, parent, false)
            return CustomViewHolder(item)
        }

        // recyclerViewのコンテンツのサイズ
        override fun getItemCount(): Int {
            return customList.size
        }

        // ViewHolderに表示する画像とテキストを挿入
        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.view.content2.text = customList[position]
            holder.view.record.text = customList2[position]

        }
    }






    private lateinit var homeViewModel: HomeViewModel
    private lateinit var prefs : SharedPreferences
    private lateinit var adapter: SecondFragmentAdapter
    private lateinit var list1 :MutableList<String>
    private lateinit var list2 : MutableList<String>


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //(activity as AppCompatActivity).supportActionBar?.hide()
        /*
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
         */
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireActivity().getSharedPreferences("preferences_key_sample", Context.MODE_PRIVATE)
        list1 = mutableListOf<String>()
        list2 = mutableListOf<String>()

        val ncd = prefs.getInt(getString(R.string.score_continuous),0)//連続日数
        val revival = prefs.getInt(getString(R.string.score_recover),0)//復活回数
        val totalday = prefs.getInt(getString(R.string.score_totalDay),0)//総日数
        val TT = prefs.getInt(getString(R.string.score_totalTime),0)//総時間
        val totalPoint = prefs.getInt(getString(R.string.score_totalPoint),0)
        val MAX : Int = prefs.getInt(getString(R.string.score_MAX),0)//最長連続日数

        list1.add("継続日数")
        list1.add("これまでの最長継続日数")
        list1.add("復活回数")
        list1.add("総活動日数")
        list1.add("総活動時間")
        list1.add("総経験値")

        list2.add("$ncd 日")
        list2.add("$MAX 日")
        list2.add("$revival 回")
        list2.add("$totalday 日")
        val seconds =TT%60;
        val minite =(TT/60)%60;
        val totaltime="$minite"+"分"+"$seconds"+"秒"
        list2.add(totaltime)
        list2.add("$totalPoint")

        adapter = SecondFragmentAdapter(list1,list2)
        val layoutManager = LinearLayoutManager(requireContext())
        // アダプターとレイアウトマネージャーをセット
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

    }

}