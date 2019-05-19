package com.akaaka.figaro

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class EditProfileActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        edit_profile_back.setOnClickListener { view ->
            onBackPressed()
        }

        edit_profile_save.setOnClickListener { view ->
            val dataSet = JSONObject()
                .put("ID", auth.currentUser!!.email)
                .put("Name", edit_profile_name_input.text)
                .put("Sex", "Nope")
                .put("Age", "0")
                .put("Picture", "Nope")
                .put("Phone", edit_profile_cellphone_input.text)
                .put("BirthDay", edit_profile_birthday_input.text)
            val ipSet = "http://18.197.8.98:5070/user/edit"

            val req = ipSet.httpPost().jsonBody(dataSet.toString())

            req.header("Content-Type", "application/json")
            req.response { _, _, _ ->}

            onBackPressed()
        }

        edit_profile_birthday_input.setOnClickListener(View.OnClickListener {
            showDatePickerDialog()
        })

        auth = FirebaseAuth.getInstance()

        val dataGet = JSONObject().put("ID", auth.currentUser!!.email)
        val ipGet = "http://18.197.8.98:5070/user/getprofile"

        val req = ipGet.httpPost().jsonBody(dataGet.toString())

        req.header("Content-Type", "application/json")
        req.response { request, response, result ->
            when (result) {
                is Result.Success -> {

                    val arr = JSONArray(String(response.data)).getJSONArray(0)

                    edit_profile_email_input.setText(arr.getString(0))
                    edit_profile_name_input.setText(arr.getString(1))
                    edit_profile_cellphone_input.setText(arr.getString(4))
                    edit_profile_birthday_input.setText(arr.getString(8))
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
        val date = "$year-${getMonth(month)}-${getDay(dayOfMonth)}"
        edit_profile_birthday_input.setText(date)
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
