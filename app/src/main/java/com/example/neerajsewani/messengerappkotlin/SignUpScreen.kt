package com.example.neerajsewani.messengerappkotlin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.signup_screen.*
import java.util.*
import kotlin.collections.HashMap

class SignUpScreen : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: FirebaseFirestore

    lateinit var email: String
    lateinit var password: String
    lateinit var username: String
    lateinit var imageURL: String
    lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_screen)

        title = "Sign-up Screen"

        //  initializing FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance()

        checkForNewUser()

        //  onClick
        signup_button_signup_activity.setOnClickListener {

            username = username_signup_activity.text.toString()
            email = email_signup_activity.text.toString()
            password = password_signup_activity.text.toString()

            if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                createUser(email, password) //  creating the user
            }
        }



        //  INTENT "SigninActivity"
        login_instead_signup_activity.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkForNewUser(){
        if (firebaseAuth.currentUser != null) {
            intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (!task.isComplete)
                    return@addOnCompleteListener Toast.makeText(
                        this, "Couldn't create user",
                        Toast.LENGTH_SHORT
                    ).show()

                uploadData(firebaseAuth.uid.toString(), username, email, password) //  uploading the data of the user
            }
    }


    private fun uploadData(userId: String, username: String, email: String, password: String) {
        //  getting an instance of the db
        database = FirebaseFirestore.getInstance()

        val userDetails = HashMap<String, Any>()
        userDetails["userId"] = userId
        userDetails["username"] = username
        userDetails["email"] = email
        userDetails["password"] = password

        //  adding a new document
        database.collection("users")
            .add(userDetails)
            .addOnSuccessListener {
                Log.d("SignUpScreen", "uploadData (line 154): Data updation successful")

                intent = Intent(this, ImageUpload::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.e("SignUpScreen", "uploadData (line 157): $it")
            }
    }
}
