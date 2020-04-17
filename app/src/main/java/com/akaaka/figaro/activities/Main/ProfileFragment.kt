package com.akaaka.figaro.activities.Main

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akaaka.figaro.network.NetworkUtils
import com.akaaka.figaro.activities.EditProfile.EditProfileActivity
import com.akaaka.figaro.R
import com.akaaka.figaro.activities.SignIn.SignInActivity
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {
    private lateinit var parentActivity: Activity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // listeners
        view.button_edit_account.setOnClickListener { startActivity(Intent(context, EditProfileActivity:: class.java)) }

        view.button_logout.setOnClickListener{
            AlertDialog.Builder(context)
                .setTitle("Do you want to log out?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes
                ) { _, _ ->
                    logout("false")
                }
                .setNegativeButton(android.R.string.no, null).show()
        }

        view.button_delete_account.setOnClickListener{
            AlertDialog.Builder(context)
                .setTitle("Do you really want to delete your account?")
                .setMessage("This action is irreversible!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes
                ) { _, _ ->
                    val reqDeleteAppointment = NetworkUtils.httpRequest("delete", "figaro/user")
                    NetworkUtils.makeRefreshingRequest(reqDeleteAppointment, ::logout, context as Activity, false)
                }
                .setNegativeButton(android.R.string.no, null).show()
        }
    }

    private fun logout(deleteAccount: String = "") {
        // get tokens from internal memory
        val prefs = parentActivity.getSharedPreferences("com.akaaka.figaro.prefs", 0)
        val accessToken = prefs.getString("access_token", null)
        val refreshToken = prefs.getString("refresh_token", null)

        // tell server to revoke both tokens
        if (deleteAccount == "false") {
            NetworkUtils.httpRequest("post", "logout/access", token = accessToken!!).response{ _ ->  }
            NetworkUtils.httpRequest("post", "logout/refresh", token = refreshToken!!).response{ _ ->  }
        }

        // remove tokens from internal memory
        val editPrefs = prefs.edit()
        editPrefs.remove("access_token")
        editPrefs.remove("refresh_token")
        editPrefs.apply()

        startActivity(Intent(parentActivity, SignInActivity:: class.java))
        parentActivity.finish()
    }


    override fun onResume() {
        super.onResume()

        // set user data
        val prefs = parentActivity.getSharedPreferences("com.akaaka.figaro.prefs", 0)
        profile_username.text = prefs.getString("user_name", "")
        profile_email.text = prefs.getString("user_email", "")
        profile_cellphone.text = prefs.getString("user_phone", "")
        profile_birthday.text = prefs.getString("user_birthday", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity)
            parentActivity = context
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
