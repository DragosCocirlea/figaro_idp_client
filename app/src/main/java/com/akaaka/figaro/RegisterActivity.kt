package com.akaaka.figaro

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.util.*

class RegisterActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        button_register_mail.setOnClickListener(View.OnClickListener {
            view -> register()
        })

        register_birthday.setOnClickListener(View.OnClickListener {
            showDatePickerDialog()
        })

        auth = FirebaseAuth.getInstance()
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
        } else {

            /* checking phone number */
            if (userPhone.length !in 10..12) {
                Toast.makeText(this, "Please insert a valid phone number", Toast.LENGTH_LONG).show()
                return
            }
        }


        if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {

            if (userPassword.length < 6) {
                Toast.makeText(this, "The password needs to be at least 6 characters long.", Toast.LENGTH_LONG).show()
                return
            }

            auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created. You can now log in.", Toast.LENGTH_LONG).show()

                    val data = JSONObject()
                        .put("ID", userEmail)
                        .put("Name", userName)
                        .put("Sex", "Nope")
                        .put("Age", "0")
                        .put("Picture", "Nope")
                        .put("Phone", userPhone)
                        .put("BirthDay", userBirthday)

                    val ip = "http://18.197.8.98:5070/user/edit"
                    val req = ip.httpPost().jsonBody(data.toString())
                    req.header("Content-Type", "application/json")
                    req.response { _, _, _ ->}

                    onBackPressed()
                } else {
                    val error = task.exception.toString().split(":")[1]
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            })
        } else {
            Toast.makeText(this, "Please fill in both the email and the password", Toast.LENGTH_LONG).show()
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
        val date = "$year-${getMonth(month)}-${getDay(dayOfMonth)}"
        register_birthday.setText(date)
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
