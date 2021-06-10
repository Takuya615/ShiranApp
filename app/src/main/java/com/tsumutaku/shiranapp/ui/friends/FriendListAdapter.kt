package com.tsumutaku.shiranapp.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.tsumutaku.shiranapp.R
import kotlinx.android.synthetic.main.list_item_friends.view.*


class FriendListAdapter(private val customList: MutableList<String>) : RecyclerView.Adapter<FriendListAdapter.CustomViewHolder>()  {

    //リスナー
    lateinit var listener: OnItemClickListener

    // ViewHolderクラス(別ファイルに書いてもOK)
    class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val sampleImg = view.itemdeleate
        val sampleTxt = view.itemTextView

    }

    // getItemCount onCreateViewHolder onBindViewHolderを実装
    // 上記のViewHolderクラスを使ってViewHolderを作成
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val item = layoutInflater.inflate(R.layout.list_item_friends, parent, false)
        return CustomViewHolder(item)
    }

    // recyclerViewのコンテンツのサイズ
    override fun getItemCount(): Int {
        return customList.size
    }

    // ViewHolderに表示する画像とテキストを挿入
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.view.itemTextView.text = customList[position]
        if(position==0){
            holder.view.itemdeleate.isInvisible = true
        }


        holder.view.itemTextView.setOnClickListener {
            listener.onItemClickListener(it, position, customList[position])
        }

        holder.view.itemdeleate.setOnClickListener {
            listener.onItemClickListener(it, position, customList[position])
        }

    }
    //インターフェースの作成
    interface OnItemClickListener{
        fun onItemClickListener(view: View, position: Int, clickedText: String)
    }

    // リスナー
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

}