package com.akaaka.figaro.activities.SignIn

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.akaaka.figaro.activities.Main.MainActivity
import com.akaaka.figaro.activities.Register.RegisterActivity
import com.akaaka.figaro.R
import com.akaaka.figaro.network.NetworkUtils
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.json.JSONObject

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        checkTokens()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Button listeners
        button_sign_in.setOnClickListener { signIn() }
        register_tv.setOnClickListener {
            startActivityForResult(Intent(this, RegisterActivity:: class.java), 1001)
        }
        login_password.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signIn()
                    return true
                }

                return false
            }
        } )
    }

    private fun signIn() {
        val userEmail = login_username.text.toString()
        val userPassword = login_password.text.toString()

        val dataSignIn = JSONObject()
            .put("email", userEmail)
            .put("password", userPassword)

        val reqSignIn = NetworkUtils.httpRequest("post","login", dataSignIn)
        reqSignIn.response { _, respSignIn, _ ->
            if (respSignIn.statusCode == -1) {
                Toast.makeText(this, "Server is unreachable", Toast.LENGTH_LONG).show()
                return@response
            }

            val jsonResp = JSONObject(String(respSignIn.data))
            val msg = jsonResp["msg"].toString()
            when {
                respSignIn.statusCode >= 400 -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

                respSignIn.statusCode >= 200 -> {
                    val newAccessToken = jsonResp["access_token"].toString()
                    val newRefreshToken = jsonResp["refresh_token"].toString()

                    successfulSignIn(false, newAccessToken, newRefreshToken)
                }
            }
        }
    }

    private fun checkTokens() {
        val prefs = this.getSharedPreferences("com.akaaka.figaro.prefs", 0)
        val accessToken = prefs.getString("access_token", null)

        if (accessToken != null) {
            val reqCheckAccessToken = NetworkUtils.httpRequest("get", "figaro/user", token = accessToken)
            NetworkUtils.makeRefreshingRequest(reqCheckAccessToken, ::tokensStillValid, this, false)
        }
    }

    private fun tokensStillValid(responseData: String) {
        val jsonUser = JSONObject(responseData)
        val userEmail = jsonUser.getString("id")
        val userName = jsonUser.getString("name")
        val userPhone = jsonUser.getString("phone")
        val userBirthday = jsonUser.getString("birthday")

        val prefsEdit = this.getSharedPreferences("com.akaaka.figaro.prefs", 0).edit()
        prefsEdit.putString("user_email", userEmail)
        prefsEdit.putString("user_name", userName)
        prefsEdit.putString("user_phone", userPhone)
        prefsEdit.putString("user_birthday", userBirthday)
        prefsEdit.apply()

        successfulSignIn(true)
    }

    private fun successfulSignIn(alreadySignedIn : Boolean, access: String = "", refresh: String = "") {
        if (!alreadySignedIn) {
            // when not already signed in, save user data in device memory for easier fetch
            val reqUserData = NetworkUtils.httpRequest("get", "figaro/user", token = access)
            reqUserData.response { _ , response, _ ->
                when {
                    response.statusCode >= 200 -> {
                        val jsonUser = JSONObject(String(response.data))
                        val userEmail = jsonUser.getString("id")
                        val userName = jsonUser.getString("name")
                        val userPhone = jsonUser.getString("phone")
                        val userBirthday = jsonUser.getString("birthday")

                        val prefsEdit = applicationContext.getSharedPreferences("com.akaaka.figaro.prefs", 0).edit()
                        prefsEdit.putString("user_email", userEmail)
                        prefsEdit.putString("user_name", userName)
                        prefsEdit.putString("user_phone", userPhone)
                        prefsEdit.putString("user_birthday", userBirthday)
                        prefsEdit.apply()
                    }
                }
            }

            val prefsEditor = this.getSharedPreferences("com.akaaka.figaro.prefs", 0).edit()
            prefsEditor.putString("access_token", access)
            prefsEditor.putString("refresh_token", refresh)
            prefsEditor.apply()
        }

        val prefs = getSharedPreferences("com.akaaka.figaro.prefs", 0)
        val acc = prefs.getString("access_token", "fmm")
        val refr = prefs.getString("refresh_token", "fmm")

        Log.d("figaro_access", acc!!)
        Log.d("figaro_refresh", refr!!)

        startActivity(Intent(this, MainActivity:: class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val accessToken = data!!.getStringExtra("access_token")
            val refreshToken = data.getStringExtra("refresh_token")

            successfulSignIn(false, accessToken!!, refreshToken!!)
        }
    }

}
