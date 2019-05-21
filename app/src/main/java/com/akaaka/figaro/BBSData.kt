package com.akaaka.figaro

import org.json.JSONArray

data class BBSData(val id : Int, val name : String, val rating : Double, val price : String, val distance : Double = -1.0)

data class BarberData(val id : Int, val name : String, val haircuts : JSONArray)

data class AppointmentsData(val name : String, val address : String, val lat : Double, val long : Double, val barberName : String,
                            val s_year : Int, val s_month : Int, val s_day : Int, val s_hour : Int, val s_min : Int, val e_hour : Int, val e_min : Int)