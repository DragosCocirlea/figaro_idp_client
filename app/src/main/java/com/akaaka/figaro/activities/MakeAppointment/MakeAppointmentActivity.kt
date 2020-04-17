package com.akaaka.figaro.activities.MakeAppointment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import com.akaaka.figaro.R
import com.akaaka.figaro.activities.MakeAppointment.model.BarberData
import com.akaaka.figaro.activities.MakeAppointment.model.ServiceData
import com.akaaka.figaro.network.NetworkUtils
import kotlinx.android.synthetic.main.activity_make_appointment.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class MakeAppointmentActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private var barbershopID : Int = 0
    private val barberList =  ArrayList<BarberData>()
    private var barberCharSequence : Array<CharSequence>? = null
    private val serviceList = ArrayList<ServiceData>()
    private var serviceCharSequence : Array<CharSequence>? = null
    private var timeslotsCharSequence : Array<CharSequence>? = null
    private var barberID : Int = -1
    private var serviceID : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_appointment)

        // get barbershop id and name
        barbershopID = intent.getIntExtra("id", 0)
        val barbershopName = intent.getStringExtra("name")
        barbershop_title.text = barbershopName

        // get data about barbers and service in this barbershop
        val jsonData = JSONObject()
            .put("bbs_id", barbershopID)
        val reqBarbershopData = NetworkUtils.httpRequest("post", "figaro/barbers_services", jsonData)
        NetworkUtils.makeRefreshingRequest(reqBarbershopData, ::setBarbershopData,this, true)

        barbershop_date.setOnClickListener{ showDatePickerDialog() }
        barbershop_barber.setOnClickListener { showChoiceAlertDialog("barber") }
        barbershop_service.setOnClickListener { showChoiceAlertDialog("service") }
        barbershop_timeslot.setOnClickListener { showChoiceAlertDialog("timeslot") }
        barbershop_button.setOnClickListener { makeAppointment() }
    }

    private fun setBarbershopData(responseData: String) {
        val jsonResp = JSONObject(responseData)
        val barberArray = jsonResp.getJSONArray("barbers")
        val serviceArray = jsonResp.getJSONArray("services")

        barberCharSequence = Array(barberArray.length()) { "None" }
        for (i : Int in 0 until barberArray.length()) {
            val jsonBarber = barberArray.getJSONObject(i)

            val barberID = jsonBarber.getInt("id")
            val barberName = jsonBarber.getString("name")
            val barberRating = jsonBarber.getDouble("rating")

            barberList.add(BarberData(barberID, barberName, barberRating))
            barberCharSequence!![i] = "$barberName - ★$barberRating"
        }

        serviceCharSequence = Array(serviceArray.length()) { "None" }
        for (i : Int in 0 until serviceArray.length()) {
            val jsonService = serviceArray.getJSONObject(i)

            val serviceID = jsonService.getInt("id")
            val serviceName = jsonService.getString("name")
            val servicePrice = jsonService.getInt("price")

            serviceList.add(ServiceData(serviceID, serviceName, servicePrice))
            serviceCharSequence!![i] = "$serviceName - $servicePrice lei"
        }
    }

    private fun showChoiceAlertDialog(field: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick a $field")

        when (field) {
            "barber" -> {
                builder.setItems(barberCharSequence) { _: DialogInterface?, which: Int ->
                    barbershop_barber.setText(barberCharSequence!![which])
                    barberID = barberList[which].id
                    getSetTimeSlots()
                }
            }

            "service" -> {
                builder.setItems(serviceCharSequence) { _: DialogInterface?, which: Int ->
                    barbershop_service.setText(serviceCharSequence!![which])
                    serviceID = serviceList[which].id
                }
            }

            "timeslot" -> {
                builder.setItems(timeslotsCharSequence) { _: DialogInterface?, which: Int ->
                    barbershop_timeslot.setText(timeslotsCharSequence!![which])
                }
            }
        }

        val alert = builder.create()
        alert.show()
    }

    private fun getSetTimeSlots() {
        val barber = barbershop_barber.text.toString()
        val date = barbershop_date.text.toString()

        if (barber.isEmpty() || date.isEmpty())
            return

        // get data about barbers and service in this barbershop
        val jsonData = JSONObject()
            .put("barber_id", barberID)
            .put("date", date)
        val reqTimeSlots = NetworkUtils.httpRequest("post", "figaro/time", jsonData)
        NetworkUtils.makeRefreshingRequest(reqTimeSlots, ::setTimeSlots,this, true)
    }

    private fun setTimeSlots(responseData: String) {
        val timeslotsArray = JSONArray(responseData)

        timeslotsCharSequence = Array(timeslotsArray.length()) { "None" }
        for (i : Int in 0 until timeslotsArray.length()) {
            val time = timeslotsArray.getString(i)
            timeslotsCharSequence!![i] = time
        }
    }

    private fun makeAppointment() {
        if (barbershop_date.text.toString().isEmpty() || barbershop_barber.text.toString().isEmpty() ||
            barbershop_timeslot.text.toString().isEmpty() || barbershop_service.text.toString().isEmpty()) {
            Toast.makeText(this, "Please input all the data", Toast.LENGTH_LONG).show()
            return
        }

        val jsonAppointment = JSONObject()
            .put("barber_id", barberID)
            .put("service_id", serviceID)
            .put("date", barbershop_date.text.toString())
            .put("time", barbershop_timeslot.text.toString())

        val reqAppointment = NetworkUtils.httpRequest("post", "figaro/appointment", jsonAppointment)
        NetworkUtils.makeRefreshingRequest(reqAppointment, ::makeAppointmentResult,this, true)
    }

    private fun makeAppointmentResult(responseData: String) {
        val jsonResp = JSONObject(responseData)
        val code = jsonResp.getInt("code")
        val msg = jsonResp.getString("msg")

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

        if (code == 1) {
            onBackPressed()
        }

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
        val date = "${getDay(dayOfMonth)}-${getMonth(month)}-$year"
        barbershop_date.setText(date)

        getSetTimeSlots()
    }

    private fun getMonth(month : Int) =
        when (month) {
            0 -> "Jan"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Apr"
            4 -> "May"
            5 -> "Jun"
            6 -> "Jul"
            7 -> "Aug"
            8 -> "Sep"
            9 -> "Oct"
            10 -> "Noc"
            11 -> "Dec"
            else -> "ERROR"
        }

    private fun getDay(day : Int) =
        if (day < 10) "0$day"
        else "$day"
}
