package com.akaaka.figaro

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(val BBSList : ArrayList<BBSData>, val context: Context) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

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

        if (bbs.distance != -1.0) {

            val dist : Double = (Math.round(bbs.distance * 100.0) / 100.0)

            holder.tvDistance.text = "$dist km"
            holder.tvDistance.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(context, BarbershopActivity::class.java).apply {
                putExtra("ID", bbs.id)
            }

            context.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById(R.id.tvBSName) as TextView
        val tvRating = itemView.findViewById(R.id.tvBSRating) as TextView
        val tvPrice = itemView.findViewById(R.id.tvBSPrice) as TextView
        val tvDistance = itemView.findViewById(R.id.tvBSDistance) as TextView
    }

}