package com.example.neerajsewani.messengerappkotlin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

class SignUpScreen : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var profileImageRef: StorageReference
    lateinit var database: FirebaseFirestore

    lateinit var email: String
    lateinit var password: String
    lateinit var username: String
    lateinit var imageURL: String
    lateinit var toast: Toast

    var IMAGE_PICK: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        //  onClick
        signup_button_signup_activity.setOnClickListener{
            username = username_signup_activity.text.toString()
            email = email_signup_activity.text.toString()
            password = password_signup_activity.text.toString()

            if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && imageURL.isNotBlank()){
                uploadData(username, email, password, imageURL)
            }
        }

        //  getting the image from the gallery
        profile_image_signup_activity.setOnClickListener{
            var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK)
        }

        //  INTENT "SigninActivity"
        login_instead_signup_activity.setOnClickListener{
            var intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //  for image capture
        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null){
            //  loading the image
            var imageUri = data.data

            //  generating  random id
            var filename = UUID.randomUUID().toString()

            profileImageRef = FirebaseStorage.getInstance().getReference("images/$filename")

            //  uploading image to the Storage
            profileImageRef.putFile(imageUri!!)
                .addOnSuccessListener {

                    //  getting the download Uri
                    profileImageRef.downloadUrl.addOnSuccessListener {
                        Log.d("SignUpScreen", "onActivityResult (line 77): image URL ==> $it")

                        imageURL = it.toString()

                        //  loading the image in the image view
                        Picasso.get()
                            .load(imageUri)
                            .error(R.drawable.abc_action_bar_item_background_material)
                            .placeholder(R.drawable.abc_ic_arrow_drop_right_black_24dp)
                            .into(profile_image_signup_activity)
                    }
                        .addOnFailureListener{
                            Log.e("SignUpScreen", "onActivityResult (line 85): $it")
                        }

                }
                .addOnFailureListener{
                    Log.e("SignUpScreen", "onActivityResult (line 87): $it")
                }

        }
    }

    override fun onStart() {
        super.onStart()


        //  creating the user
        if (firebaseAuth.currentUser != null) {
            if (email.isNotBlank() && password.isNotBlank() && username.isNotBlank()){
            createUser(email, password)
            } else {
                if (toast != null) {
                    Toast.makeText(this, "Fill out the credentials", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
        }

    }

    private fun createUser(email: String, password: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task: Task<AuthResult> ->
                if (!task.isComplete)
                    return@addOnCompleteListener Toast.makeText(this, "Couldn't create user",
                        Toast.LENGTH_SHORT).show()

                //  on successful user creation
                var intent = Intent(this,  MainActivity::class.java)
                startActivity(intent)
            }
    }


    private fun uploadData(username: String, email: String, password: String, imageURL: String){
        //  getting an instance of the db
        database = FirebaseFirestore.getInstance()

        var data = HashMap<String, Any>()
        data["username"] = username
        data["email"] = email
        data["password"] = password
        data["imageURL"] = imageURL

        //  adding a new document
        database.collection("users")
            .add(data)
            .addOnSuccessListener {
                Log.d("SignUpScreen", "uploadData (line 154): Data updation successful")
            }
            .addOnFailureListener{
                Log.e("SignUpScreen", "uploadData (line 157): $it")
            }


    }
}
