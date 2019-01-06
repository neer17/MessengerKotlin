package com.example.neerajsewani.messengerappkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.neerajsewani.messengerappkotlin.data_class.UserData
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.recycler_view_inflated_main_activity.view.*

class MainActivity : AppCompatActivity() {
    lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        //  adapter by GROUPIE
        var adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserItem(UserData("username", "email", "imageURL")))
        recycler_view_main_activity.adapter = adapter


        /*//  fetching data from the Firebase
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection("users")
            .get()
            .addOnSuccessListener {
                if (it != null) {
                    it.forEach {
                        var username = it.get("username").toString()
                        var email = it.get("email").toString()
                        var imageURL = it.get("imageURL").toString()

                        Log.d("MainActivity", "onCreate (line 36): username ==> $username email ==> $email imageURL ==> $imageURL")

                        val user = UserData(username, email, imageURL)
                        adapter.add(UserItem(user))
                    }

                    recycler_view_main_activity.adapter = adapter

                }

            }.addOnFailureListener {
                Log.e("MainActivity", "onCreate (line 30): ", it)
            }*/
    }

    //  groupie class
    class UserItem(val user: UserData) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.recycler_view_inflated_main_activity
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_recycler_inflated_main_activity.text = user.username
            viewHolder.itemView.email_recycler_inflated_main_activity.text = user.email

            Picasso.get()
                .load(user.imageURL)
                .into(viewHolder.itemView.user_image_recycler_inflated_main_activity)

        }
    }
}
