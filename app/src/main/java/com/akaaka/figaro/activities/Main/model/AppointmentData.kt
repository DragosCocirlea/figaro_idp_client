package com.akaaka.figaro.activities.Main.model

import org.json.JSONObject

class AppointmentData(val id: Int, val bbsName: String, val address: String, val lat: Double, val long: Double,
                      val barberName: String, val service: String, val date: String, val time: String)

fun jsonToAppointment(jsonAppointment: JSONObject): AppointmentData {
    val id = jsonAppointment.getInt("id")
    val bbsName = jsonAppointment.getString("bbs_name")
    val bbsAddress = jsonAppointment.getString("address")
    val lat = jsonAppointment.getDouble("lat")
    val long = jsonAppointment.getDouble("long")
    val barberName = jsonAppointment.getString("barber_name")
    val service = jsonAppointment.getString("service")
    val date = jsonAppointment.getString("date")
    val time = jsonAppointment.getString("time")

    return AppointmentData(id, bbsName, bbsAddress, lat, long, barberName, service, date, time)
}