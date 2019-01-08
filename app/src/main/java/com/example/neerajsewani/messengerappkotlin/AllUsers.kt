package com.example.neerajsewani.messengerappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.neerajsewani.messengerappkotlin.data_class.Users
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.all_users.*
import kotlinx.android.synthetic.main.recycler_view_inflated_main_activity.view.*

class AllUsers : AppCompatActivity() {
    lateinit var firebaseFirestore: FirebaseFirestore

    companion object {
        val KEY = "USER DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_users)

        title = "All Users"
        //  adapter by GROUPIE
        var adapter = GroupAdapter<ViewHolder>()

        //  onClick
        //  when a user's profile is clicked, would get redirected to the "ChatLogActivity"
        adapter.setOnItemClickListener { item, view ->
            var userItem = item as UserItem

            intent = Intent(view.context, ChatLogActivity::class.java)
            intent.putExtra(KEY, userItem.user) //  enable the parcelable extension to send the whole class through the intent
            startActivity(intent)
        }


        //  fetching data from the Firebase at once
        //  and adding that to the recycler view
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection("users")
            .get()
            .addOnSuccessListener {
                if (it != null) {
                    it.forEach {
                        //  de-serializing the data of collection "Users" to "Users" class
                        val userData = it.toObject(Users::class.java)

                        adapter.add(UserItem(userData))
                    }

                    //  attaching the adapter to the recycler view
                    recycler_view_main_activity.adapter = adapter
                }

            }.addOnFailureListener {
                Log.e("AllUsers", "onCreate (line 30): ", it)
            }
    }

    //  GROUPIE class
    class UserItem(val user: Users) : Item<ViewHolder>() {
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
