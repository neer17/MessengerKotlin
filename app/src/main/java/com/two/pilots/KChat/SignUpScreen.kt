package com.two.pilots.KChat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.signup_screen.*


class SignUpScreen : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_screen)

        //  progress bar GONE
        progress_bar_signup_screen.visibility = ProgressBar.GONE

        title = "Sign-up Screen"

        //  initializing FIREBASE AUTH
        firebaseAuth = FirebaseAuth.getInstance()

        checkForNewUser()

        //  onClick
        signup_button_signup_activity.setOnClickListener {
            //  progress bar VISIBLE
            progress_bar_signup_screen.visibility = ProgressBar.VISIBLE

            username = username_signup_activity.text.toString()
            email = email_signup_activity.text.toString()
            password = password_signup_activity.text.toString()

            if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                //  making progress bar visible
                progress_bar_signup_screen.visibility = ProgressBar.VISIBLE

                createUser(email, password) //  creating the user
            } else {
                //  progress bar GONE
                progress_bar_signup_screen.visibility = ProgressBar.GONE

                if (toast != null) {
                    toast = null
                }
                toast = Toast.makeText(this, "Fill out the details", Toast.LENGTH_SHORT)
                toast!!.show()

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
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun createUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                //  making progress bar invisible
                progress_bar_signup_screen.visibility = ProgressBar.GONE

                if (!task.isComplete)
                    return@addOnCompleteListener Toast.makeText(
                        this, "Couldn't create user",
                        Toast.LENGTH_SHORT
                    ).show()

                uploadData(firebaseAuth.uid.toString(), username, email, password) //  uploading the data of the user
            }.addOnFailureListener {
                if (toast != null) {
                    toast = null
                }
                toast = Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT)
                toast!!.show()

                //  making progress bar invisible
                progress_bar_signup_screen.visibility = ProgressBar.GONE

                Log.e("SignUpScreen", "createUser (line 90): ", it)
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
