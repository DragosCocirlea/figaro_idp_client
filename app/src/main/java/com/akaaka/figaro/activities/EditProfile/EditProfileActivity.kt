package com.akaaka.figaro.activities.EditProfile

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akaaka.figaro.network.NetworkUtils
import com.akaaka.figaro.R
import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.json.JSONObject
import java.util.*

class EditProfileActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // set user data
        val prefs = this.getSharedPreferences("com.akaaka.figaro.prefs", 0)
        edit_profile_email_input.setText(prefs.getString("user_email", ""))
        edit_profile_name_input.setText(prefs.getString("user_name", ""))
        edit_profile_cellphone_input.setText(prefs.getString("user_phone", ""))
        edit_profile_birthday_input.setText(prefs.getString("user_birthday", ""))

        // listeners
        edit_profile_back.setOnClickListener { onBackPressed() }
        edit_profile_birthday_input.setOnClickListener { showDatePickerDialog() }
        edit_profile_save.setOnClickListener { checkAndSaveUserData() }
    }

    private fun checkAndSaveUserData() {
        val userName = edit_profile_name_input.text.toString()
        val userPhone = edit_profile_cellphone_input.text.toString()
        val userBirthday = edit_profile_birthday_input.text.toString()

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
        /* checking userName is all ASCII */
        val nameOnlyAscii: Boolean = userName.matches("\\A\\p{ASCII}*\\z".toRegex())
        if (!nameOnlyAscii) {
            Toast.makeText(this, "Don't use any special characters", Toast.LENGTH_LONG).show()
            return
        }

        val jsonUser = JSONObject()
            .put("name", userName)
            .put("phone", userPhone)
            .put("birthday", userBirthday)

        val reqUpdate = NetworkUtils.httpRequest("post", "figaro/user", jsonUser)
        NetworkUtils.makeRefreshingRequest(reqUpdate, ::saveUserDataToPrefs, this, true)
    }

    private fun saveUserDataToPrefs(responseData: String) {
        val jsonUser = JSONObject(responseData)
        val userName = jsonUser.getString("name")
        val userPhone = jsonUser.getString("phone")
        val userBirthday = jsonUser.getString("birthday")

        val prefsEdit = this.getSharedPreferences("com.akaaka.figaro.prefs", 0).edit()
        prefsEdit.putString("user_name", userName)
        prefsEdit.putString("user_phone", userPhone)
        prefsEdit.putString("user_birthday", userBirthday)
        prefsEdit.apply()

        onBackPressed()
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
        edit_profile_birthday_input.setText(date)
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
