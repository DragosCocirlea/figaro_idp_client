package com.akaaka.figaro.activities.Main.controller

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.akaaka.figaro.R
import com.akaaka.figaro.activities.Main.model.AppointmentData
import com.akaaka.figaro.network.NetworkUtils
import org.json.JSONObject
import java.util.*

class AppointmentAdapter(private val appointmentList : ArrayList<AppointmentData>, val context: Context) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {
    var position : Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_row_main, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointmentList[position]

        holder.tvBBSName.text = appointment.bbsName
        holder.tvAddress.text = appointment.address
        holder.tvBarber.text = appointment.barberName
        holder.tvService.text = appointment.service
        holder.tvDate.text = appointment.date
        holder.tvBetween.text = appointment.time

        holder.bMap.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:<" + appointment.lat + ">,<" + appointment.long + ">?q=<" + appointment.lat + ">,<" + appointment.long + ">(" + appointment.bbsName + ")"))
            context.startActivity(intent)
        }

        holder.bDelete.setOnClickListener {
            this.position = position

            AlertDialog.Builder(context)
                .setTitle("Do you want to cancel your appointment?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes
                ) { _, _ ->
                    val jsonDelete = JSONObject().put("appointment_id", appointment.id)
                    val reqDeleteAppointment = NetworkUtils.httpRequest("delete", "figaro/appointment", jsonDelete)
                    NetworkUtils.makeRefreshingRequest(reqDeleteAppointment, ::postDeleteRequest, context as Activity, false)
                }
                .setNegativeButton(android.R.string.no, null).show()

        }
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tvBBSName = itemView.findViewById(R.id.rv_main_bbs_name) as TextView
        val tvAddress = itemView.findViewById(R.id.rv_main_address) as TextView
        val tvBarber = itemView.findViewById(R.id.rv_main_barber) as TextView
        val tvDate = itemView.findViewById(R.id.rv_main_date) as TextView
        val tvService = itemView.findViewById(R.id.rv_main_service) as TextView
        val tvBetween = itemView.findViewById(R.id.rv_main_time) as TextView
        val bMap = itemView.findViewById(R.id.rv_main_map_button) as ImageButton
        val bDelete = itemView.findViewById(R.id.rv_main_delete_button) as ImageButton
    }

    private fun postDeleteRequest(responseData: String) {
        val resp = JSONObject(responseData)
        val code = resp.getInt("code")
        val msg = resp.getString("msg")

        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        if (code == 1) {
            appointmentList.removeAt(position)
            if (position == 0) {
                notifyDataSetChanged()
            }
            else {
                notifyItemRemoved(position)
            }
        }
    }
}