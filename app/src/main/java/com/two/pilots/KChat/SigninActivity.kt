package com.two.pilots.KChat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.signup_screen.*

class SigninActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        //  progress bar GONE
        progress_bar_signin_screen.visibility = ProgressBar.GONE

        title = "Sign-In"


        firebaseAuth = FirebaseAuth.getInstance()

        signin_button_signin_activity.setOnClickListener {
            val email = email_et_signin_activity.text.toString()
            val password = password_et_signin_activity.text.toString()

            if (email.isBlank() && password.isBlank()) {
                //  progress bar GONE
                progress_bar_signin_screen.visibility = ProgressBar.GONE
                if (toast != null) {
                    toast = null
                }
                toast = Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT)
                toast!!.show()
            } else {
                // progress bar VISIBLE
                progress_bar_signin_screen.visibility = ProgressBar.VISIBLE

                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        //  progress bar GONE
                        progress_bar_signin_screen.visibility = ProgressBar.GONE

                        Log.d("SigninActivity", "onCreate (line 25): Successfully signed in")

                        intent = Intent(this, ChatActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }.addOnFailureListener {
                        if (toast != null) {
                            toast = null
                        }

                        toast = Toast.makeText(this, "Email or Password doesn't match", Toast.LENGTH_SHORT)
                        toast!!.show()

                        //  progress bar GONE
                        progress_bar_signin_screen.visibility = ProgressBar.GONE

                        Log.e("SigninActivity", "onCreate (line 29): ", it)
                    }
            }
        }
    }
}
