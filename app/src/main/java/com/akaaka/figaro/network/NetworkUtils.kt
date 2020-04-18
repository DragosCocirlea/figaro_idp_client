package com.akaaka.figaro.network

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.akaaka.figaro.activities.SignIn.SignInActivity
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import org.json.JSONObject

object NetworkUtils {
    private const val serverIP = "http://ec2-3-122-94-77.eu-central-1.compute.amazonaws.com:5000/"

    fun httpRequest(type: String, action: String, body: JSONObject? = null, token: String = ""): Request {
        val ip = serverIP + action
        val req = when (type) {
            "get" -> ip.httpGet()
            "delete" -> ip.httpDelete()
            else -> ip.httpPost()
        }
        req.header("Content-Type", "application/json")

        if (body != null)
            req.jsonBody(body.toString())
        if (token.isNotEmpty())
            req.authentication().bearer(token)

        return req
    }

    fun makeRefreshingRequest(request: Request, successFunction : (String) -> Unit, activity: Activity, hasParentActivity: Boolean) {
        val prefs = activity.getSharedPreferences("com.akaaka.figaro.prefs", 0)
        val accessToken = prefs.getString("access_token", "")
        request.authentication().bearer(accessToken!!)

        request.response { _, resp, _ ->
            when {
                resp.statusCode == -1 -> Toast.makeText(activity, "Server is unreachable", Toast.LENGTH_LONG).show()

                resp.statusCode >= 500 -> Toast.makeText(activity, "Server error", Toast.LENGTH_LONG).show()

                resp.statusCode >= 400 -> { // access token has expired
                    val refreshToken = prefs.getString("refresh_token", null)
                    val reqRefreshToken = httpRequest("post", "token/refresh", token = refreshToken!!)

                    reqRefreshToken.response { _, respRefresh, _ ->
                        when {
                            respRefresh.statusCode >= 400 -> {
                                Toast.makeText(activity, "Your tokens have expired. Please sign in again.", Toast.LENGTH_LONG).show()

                                // logout
                                // remove tokens from internal memory
                                val editPrefs = prefs.edit()
                                editPrefs.remove("access_token")
                                editPrefs.remove("refresh_token")
                                editPrefs.apply()

                                // go back to the sign in activity
                                activity.startActivity(Intent(activity, SignInActivity:: class.java))
                                activity.finish()

                                if (hasParentActivity)
                                    activity.parent.finish()
                            }

                            respRefresh.statusCode >= 200 -> {
                                val jsonResp = JSONObject(String(respRefresh.data))
                                val newAccessToken = jsonResp["access_token"].toString()

                                val prefEditor = prefs.edit()
                                prefEditor.putString("access_token", newAccessToken)
                                prefEditor.apply()

                                makeRefreshingRequest(request, successFunction, activity, hasParentActivity)
                            }
                        }
                    }
                }

                resp.statusCode >= 200 -> {
                    successFunction(String(resp.data))
                }
            }
        }
    }
}
