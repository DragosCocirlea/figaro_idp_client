package com.akaaka.figaro.activities.Main.model

import org.json.JSONObject

data class BarbershopData(val id : Int, val name : String, val rating : Double, val distance : Double)

fun jsonToBarbershop(jsonBarbershop: JSONObject): BarbershopData {
    val id = jsonBarbershop.getInt("id")
    val name = jsonBarbershop.getString("name")
    val rating = jsonBarbershop.getDouble("rating")
    val distance = jsonBarbershop.getDouble("distance")

    return BarbershopData(id, name, rating, distance)
}