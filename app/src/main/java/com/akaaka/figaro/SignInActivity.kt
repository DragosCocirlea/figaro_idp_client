package com.akaaka.figaro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.fragment_search.*
import org.json.JSONArray
import org.json.JSONObject

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Button listeners
        button_login_mail.setOnClickListener(this)
        button_login_google.setOnClickListener(this)
        register_tv.setOnClickListener(this)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            successfulSignIn(true)
        }
    }

    private fun successfulSignIn(alreadySignedIn : Boolean) {
        startActivity(Intent(this, MainActivity :: class.java))

//        if (!alreadySignedIn)
//            Toast.makeText(this, "Successfully logged in", Toast.LENGTH_LONG).show()

        finish()
    }

    private fun failedSignIn(error : String = "") {
        Toast.makeText(this, "Sign in failed: $error", Toast.LENGTH_LONG).show()
    }

    private fun signInMail() {
        val userEmail = login_username.text.toString()
        val userPassword = login_password.text.toString()

        if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
            auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    successfulSignIn(false)
                } else {
                    failedSignIn("bad username or pass")
                }
            })
        } else {
            Toast.makeText(this, "Please fill in both the email and the password", Toast.LENGTH_LONG).show()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
//                failedSignIn("apiException")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val dataGet = JSONObject().put("ID", acct.email)
                    val ipGet = "http://18.197.8.98:5070/user/getprofile"

                    val req = ipGet.httpPost().jsonBody(dataGet.toString())

                    req.header("Content-Type", "application/json")
                    req.response { _, _, _ ->}

                    successfulSignIn(false)
                } else {
                    failedSignIn("firebase auth failed")
                }
            }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.button_login_mail -> signInMail()
            R.id.button_login_google -> signInGoogle()
            R.id.register_tv -> startActivity(Intent(this, RegisterActivity :: class.java))
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}
