package com.akaaka.figaro.activities.Register

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akaaka.figaro.R
import com.akaaka.figaro.network.NetworkUtils
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.util.*


class RegisterActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        button_register_mail.setOnClickListener { register() }
        register_birthday.setOnClickListener { showDatePickerDialog() }
        register_password.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    register()
                    return true
                }

                return false
            }
        } )
    }

    private fun register() {
        val userEmail = register_username.text.toString()
        val userPassword = register_password.text.toString()
        val userName = register_fullname.text.toString()
        val userPhone = register_phone.text.toString()
        val userBirthday = register_birthday.text.toString()

        /* checking user data */
        if (userName.isEmpty() || userPhone.isEmpty() || userBirthday.isEmpty()) {
            Toast.makeText(this, "Please fill in all personal data", Toast.LENGTH_LONG).show()
            return
        }
        /* checking phone number */
        if (userPhone.length !in 10..12) {
            Toast.makeText(this, "Please insert a valid phone number", Toast.LENGTH_LONG).show()
            return
        }
        /* checking credentials */
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in both the email and the password", Toast.LENGTH_LONG).show()
            return
        }
        if (userPassword.length < 6) {
            Toast.makeText(this, "The password needs to be at least 6 characters long.", Toast.LENGTH_LONG).show()
            return
        }

        /* checking userName is all ASCII */
        val nameOnlyAscii: Boolean = userName.matches("\\A\\p{ASCII}*\\z".toRegex())
        if (!nameOnlyAscii) {
            Toast.makeText(this, "Don't use any special characters", Toast.LENGTH_LONG).show()
            return
        }

        val dataRegister = JSONObject()
            .put("email", userEmail)
            .put("password", userPassword)
            .put("name", userName)
            .put("phone", userPhone)
            .put("birthday", userBirthday)

        val reqRegister = NetworkUtils.httpRequest("post", "registration", dataRegister)
        reqRegister.response { _, respRegister, _ ->
            val jsonResp = JSONObject(String(respRegister.data))
            val msg = jsonResp["msg"].toString()
            when {
                respRegister.statusCode >= 400 -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

                respRegister.statusCode >= 200 -> {
                    val newAccessToken = jsonResp["access_token"].toString()
                    val newRefreshToken = jsonResp["refresh_token"].toString()

                    val resultIntent = Intent()
                    resultIntent.putExtra("access_token", newAccessToken)
                    resultIntent.putExtra("refresh_token", newRefreshToken)

                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
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
        register_birthday.setText(date)
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
