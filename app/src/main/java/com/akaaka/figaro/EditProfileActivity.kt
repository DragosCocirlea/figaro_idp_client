package com.akaaka.figaro

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.mobile.client.AWSMobileClient
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File

class EditProfileActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var auth: FirebaseAuth
    var currImageURI : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        AWSMobileClient.getInstance().initialize(this).execute()

        edit_profile_back.setOnClickListener { view ->
            onBackPressed()
        }

        edit_profile_save.setOnClickListener { view ->


//            if (currImageURI != null) {
//                uploadWithTransferUtility(getPathFromUri(currImageURI!!))
//            }

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

        edit_profile_change_picture.setOnClickListener(View.OnClickListener {
            intent = Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        })


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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            currImageURI = data!!.data
            Toast.makeText(this, "PATH: ${currImageURI!!.path}", Toast.LENGTH_LONG).show()
        }
    }

    fun getPathFromUri(uri : Uri) : String {
        val cursor : Cursor = this.contentResolver.query(uri, null, null, null, null)
        if (cursor == null)
            return uri.path
        else {
            cursor.moveToFirst()
            val idx : Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            return cursor.getString(idx)
        }
    }

    fun uploadWithTransferUtility(path : String) {
        val transferUtility = TransferUtility.builder()
            .context(this.applicationContext)
            .awsConfiguration(AWSMobileClient.getInstance().configuration)
            .s3Client(AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider))
            .build()

        val uploadObserver = transferUtility.upload("public/icon_${auth.currentUser!!.email}", File(path))

        // Attach a listener to the observer
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (((current.toDouble() / total) * 100.0).toInt())
                Log.d("AWS_TAG", "UPLOAD - - ID: $id, percent done = $done")
            }

            override fun onError(id: Int, ex: Exception) {
                Log.d("AWS_TAG", "UPLOAD ERROR - - ID: $id - - EX: ${ex.message.toString()}")
            }
        })

        // If you prefer to long-poll for updates
        if (uploadObserver.state == TransferState.COMPLETED) {
            /* Handle completion */
        }

        val bytesTransferred = uploadObserver.bytesTransferred
    }

}
