package com.akaaka.figaro

import android.os.AsyncTask
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.json.JSONObject

class AsyncPOST : AsyncTask<Pair<String, JSONObject>, String, JSONObject>() {

    override fun doInBackground(vararg params: Pair<String, JSONObject>?) : JSONObject? {
        val input = params[0]

        val type = input!!.first
        val body = input.second
        var ret : JSONObject? = null

        val ip = when (type) {
            "search" -> "http://18.197.8.98:5070/search"
            else -> "http://18.197.8.98:5070/"
        }

        val req = ip.httpPost().jsonBody(body.toString())

        req.header("Content-Type", "application/json")
        req.response { request, response, result ->
            when (result) {
                is Result.Success -> {
                    ret = JSONObject(String(response.data))
                }
            }

        }

        return ret
    }

    override fun onPostExecute(result: JSONObject?) {
        super.onPostExecute(result)
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }
}
