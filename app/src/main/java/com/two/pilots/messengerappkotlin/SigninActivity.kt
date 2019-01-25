package com.two.pilots.messengerappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signin.*

class SigninActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        firebaseAuth = FirebaseAuth.getInstance()

        signin_button_signin_activity.setOnClickListener {
            val email = email_et_signin_activity.text.toString()
            val password = password_et_signin_activity.text.toString()

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d("SigninActivity", "onCreate (line 25): Successfully signed in")

                    intent = Intent(this, ChatActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }.addOnFailureListener{
                    Log.e("SigninActivity", "onCreate (line 29): ", it)
                }
        }


    }
}
