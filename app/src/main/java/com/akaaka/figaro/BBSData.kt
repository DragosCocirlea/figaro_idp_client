package com.akaaka.figaro

import org.json.JSONArray

data class BBSData(val id : Int, val name : String, val rating : Double, val price : String, val distance : Double = -1.0)

data class BarberData(val id : Int, val name : String, val haircuts : JSONArray)