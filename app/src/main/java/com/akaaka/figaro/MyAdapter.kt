package com.akaaka.figaro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

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
        holder.tvRating.text = "★ ${bbs.rating}"

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

class MyAdapterMain(val appointmentsList : ArrayList<AppointmentsData>, val context: Context) : RecyclerView.Adapter<MyAdapterMain.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapterMain.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_row_main, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return appointmentsList.size
    }

    override fun onBindViewHolder(holder: MyAdapterMain.ViewHolder, position: Int) {
        val appointment = appointmentsList[position]

        holder.tvBBSName.text = appointment.name
        holder.tvAddr.text = appointment.address
        holder.tvBarber.text = "${appointment.barberName}"
        holder.tvDate.text = "${appointment.s_year}-${get2Digit(appointment.s_month)}-${get2Digit(appointment.s_day)}"
        holder.tvBetween.text = "${get2Digit(appointment.s_hour)}:${get2Digit(appointment.s_min)} - ${get2Digit(appointment.e_hour)}:${get2Digit(appointment.e_min)}"

        holder.bMap.setOnClickListener(View.OnClickListener {
            val uri = String.format(Locale.ENGLISH, "geo:%f,%f", appointment.lat, appointment.long)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:<" + appointment.lat + ">,<" + appointment.long + ">?q=<" + appointment.lat + ">,<" + appointment.long + ">(" + appointment.name + ")"))
            context.startActivity(intent)
        })
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tvBBSName = itemView.findViewById(R.id.rv_main_bbs_name) as TextView
        val tvAddr = itemView.findViewById(R.id.rv_main_adress) as TextView
        val tvBarber = itemView.findViewById(R.id.rv_main_barber) as TextView
        val tvDate = itemView.findViewById(R.id.rv_main_date) as TextView
        val tvBetween = itemView.findViewById(R.id.rv_main_between) as TextView
        val bMap = itemView.findViewById(R.id.rv_main_map_button) as ImageButton
    }

    fun get2Digit(a : Int) : String =
        if (a < 10)
            "0$a"
        else
            "$a"

}