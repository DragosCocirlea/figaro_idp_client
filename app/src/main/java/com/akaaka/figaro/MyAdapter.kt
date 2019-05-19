package com.akaaka.figaro

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(val BBSList : ArrayList<BBSData>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return BBSList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bbs : BBSData = BBSList[position]

        holder.tvName.text = bbs.name
        holder.tvPrice.text = bbs.price
        holder.tvRating.text = bbs.rating.toString()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById(R.id.tvBSName) as TextView
        val tvRating = itemView.findViewById(R.id.tvBSRating) as TextView
        val tvPrice = itemView.findViewById(R.id.tvBSPrice) as TextView
    }

}