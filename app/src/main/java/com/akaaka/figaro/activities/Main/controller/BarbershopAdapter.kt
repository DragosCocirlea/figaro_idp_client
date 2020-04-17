package com.akaaka.figaro.activities.Main.controller

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akaaka.figaro.activities.MakeAppointment.MakeAppointmentActivity
import com.akaaka.figaro.R
import com.akaaka.figaro.activities.Main.model.BarbershopData
import java.util.*

class BarbershopAdapter(val BarbershopList : ArrayList<BarbershopData>, val context: Context) : RecyclerView.Adapter<BarbershopAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return BarbershopList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val barbershop : BarbershopData = BarbershopList[position]

        holder.tvName.text = barbershop.name
        holder.tvRating.text = "★ ${barbershop.rating}"

        if (barbershop.distance != -1.0) {
            val dist : Double = (Math.round(barbershop.distance * 100.0) / 100.0)

            holder.tvDistance.text = "$dist km"
            holder.tvDistance.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(context, MakeAppointmentActivity::class.java).apply {
                putExtra("id", barbershop.id)
                putExtra("name", barbershop.name)
            }

            context.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById(R.id.tvBSName) as TextView
        val tvRating = itemView.findViewById(R.id.tvBSRating) as TextView
        val tvDistance = itemView.findViewById(R.id.tvBSDistance) as TextView
    }

}