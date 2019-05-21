package com.akaaka.figaro

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_barbershop.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_search.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class BarbershopActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    var id : Int = 0
    val barberList =  ArrayList<BarberData>()
    val barberNameList = ArrayList<String>()
    var barberCharSequence : Array<CharSequence>? = null
    val timeslotsList = ArrayList<String>()
    var timeslotsCharSequence : Array<CharSequence>? = null
    var barberID : Int = -1
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barbershop)

        val i = intent
        id = i.getIntExtra("ID", 0)

        auth = FirebaseAuth.getInstance()

        val dataBBS = JSONObject()
            .put("ID", id)
        val ipBBS = "http://18.197.8.98:5070/selectbbs"
        val req = ipBBS.httpPost().jsonBody(dataBBS.toString())

        req.header("Content-Type", "application/json")
        req.response { _, response, result ->
            when (result) {
                is Result.Success -> {

                    val arr = JSONArray(String(response.data))

                    val bbsData = arr.getJSONArray(0).getJSONArray(0)
                    barbershop_title.text = bbsData.getString(1)

                    val barberArray = arr.getJSONArray(1)
                    barberCharSequence = Array<CharSequence>(barberArray.length()) {
                        i -> "None"
                    }

                    for (i : Int in 0 until barberArray.length()) {
                        val obj = barberArray.getJSONArray(i)

                        val barberID = obj.getInt(0)
                        val barberName = obj.getString(1)
                        val barberHaircuts = obj.getJSONArray(2)

                        barberList.add(BarberData(barberID, barberName, barberHaircuts))
                        barberNameList.add(barberName)
                        barberCharSequence!![i] = barberName
                    }
                }
            }
        }


        barbershop_date.setOnClickListener(View.OnClickListener {
            showDatePickerDialog()
        })

        barbershop_barber.setOnClickListener(View.OnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pick a barber")
            builder.setItems(barberCharSequence, DialogInterface.OnClickListener{ dialog: DialogInterface?, which: Int ->
                barbershop_barber.setText(barberNameList[which])
                barberID = barberList[which].id

                val date = barbershop_date.text.split("-")

                val dataTimeslot = JSONObject()
                    .put("Barb_ID", barberID.toString())
                    .put("Year", date[0])
                    .put("Month", date[1])
                    .put("Day", date[2])
                val ipTimeslot = "http://18.197.8.98:5070/selectbarber"
                val reqTimeslot = ipTimeslot.httpPost().jsonBody(dataTimeslot.toString())

                reqTimeslot.header("Content-Type", "application/json")
                reqTimeslot.response { _, response, result ->
                    when (result) {
                        is Result.Success -> {

                            val timeslotArray = JSONArray(String(response.data))

                            timeslotsCharSequence = Array<CharSequence>(timeslotArray.length()) {
                                    i -> "None"
                            }

                            for (i : Int in 0 until timeslotArray.length()) {
                                val timeslot = timeslotArray.getString(i)
                                timeslotsList.add(timeslot)
                                timeslotsCharSequence!![i] = timeslot
                            }
                        }
                    }
                }
            })

            val alert = builder.create()
            alert.show()
        })

        barbershop_timeslot.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pick a timeslot")
            builder.setItems(timeslotsCharSequence, DialogInterface.OnClickListener{ dialog: DialogInterface?, which: Int ->
                barbershop_timeslot.setText(timeslotsList[which])
            })

            val alert = builder.create()
            alert.show()
        })

        barbershop_button.setOnClickListener(View.OnClickListener {
            if (barbershop_date.text.toString() == "" || barbershop_barber.text.toString() == "" || barbershop_timeslot.text.toString() == "") {
                Toast.makeText(this, "Please choose all the needed data", Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            println("${barbershop_date.text} | ${barbershop_barber.text} | ${barbershop_timeslot.text}")

            val date = barbershop_date.text.split("-")
            val times = barbershop_timeslot.text.split(" - ", ":")

            val dataAppointment = JSONObject()
                .put("Barb_ID", barberID)
                .put("START_Y", date[0])
                .put("START_M", date[1])
                .put("START_D", date[2])
                .put("START_H", times[0])
                .put("START_MIN", times[1])
                .put("END_Y", date[0])
                .put("END_M", date[1])
                .put("END_D", date[2])
                .put("END_H", times[2])
                .put("END_MIN", times[3])
                .put("User_ID", auth.currentUser!!.email.toString())

            val ipAppointment = "http://18.197.8.98:5070/appoint"
            val reqAppointment = ipAppointment.httpPost().jsonBody(dataAppointment.toString())

            reqAppointment.response { _, _, result ->
                when (result) {
                    is Result.Success -> {
                        onBackPressed()
                    }
                    else -> {
                        Toast.makeText(this, "Something went wrong. Try again!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun showDatePickerDialog(){
        val datePickerDialog = DatePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            this,
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = "$year-${getMonth(month)}-${getDay(dayOfMonth)}"
        barbershop_date.setText(date)
    }

    private fun getMonth(month : Int) =
        when (month) {
            0 -> "01"
            1 -> "02"
            2 -> "03"
            3 -> "04"
            4 -> "05"
            5 -> "06"
            6 -> "07"
            7 -> "08"
            8 -> "09"
            9 -> "10"
            10 -> "11"
            11 -> "12"
            else -> "ERROR"
        }

    private fun getDay(day : Int) =
        if (day < 10) "0$day"
        else "$day"
}
