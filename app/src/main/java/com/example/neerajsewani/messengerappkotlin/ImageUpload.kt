package com.example.neerajsewani.messengerappkotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_upload.*
import java.util.*


class ImageUpload : AppCompatActivity() {
    private val IMAGE_PICK: Int = 100

    lateinit var profileImageRef: StorageReference
    lateinit var usersReference: FirebaseFirestore
    lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_upload)
        title = "Upload Image"

        usersReference = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId")

        //  onClick
        circle_image_view_image_upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //  for image capture
        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            //  loading the image
            val imageUri = data.data

            //  generating  random id
            val filename = UUID.randomUUID().toString()

            profileImageRef = FirebaseStorage.getInstance().getReference("images/$filename")

            //  uploading image to the Storage
            profileImageRef.putFile(imageUri!!)
                .addOnSuccessListener {

                    //  getting the download Uri
                    profileImageRef.downloadUrl.addOnSuccessListener {
                        Log.d("SignUpScreen", "onActivityResult (line 77): image URL ==> $it")

                        val imageURL = it.toString()


                        //  loading the image in the image view
                        Picasso.get()
                            .load(imageURL)
                            .error(R.drawable.ic_person_black_24dp)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .into(circle_image_view_image_upload)

                        //  uploading it to the firestore
                        usersReference.collection("users").whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener {
                                if (!it.isEmpty) {
                                    it.forEach {
                                        //  uploading user's image
                                        usersReference.collection("users").document(it.id)
                                            .update("imageURL", imageURL)
                                            .addOnSuccessListener {
                                                intent = Intent(this, ChatActivity::class.java)
                                                intent.flags =
                                                        Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                startActivity(intent)
                                            }.addOnFailureListener {
                                                Log.e("ImageUpload", "onActivityResult (line 74): ", it)
                                            }
                                    }

                                }
                            }.addOnFailureListener {
                                Log.e("ImageUpload", "onActivityResult (line 67): ", it)
                            }
                    }
                        .addOnFailureListener {
                            Log.e("SignUpScreen", "onActivityResult (line 85): $it")
                        }

                }
                .addOnFailureListener {
                    Log.e("SignUpScreen", "onActivityResult (line 87): $it")
                }

        }
    }
}
