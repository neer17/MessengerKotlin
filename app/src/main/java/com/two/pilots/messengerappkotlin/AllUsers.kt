package com.two.pilots.messengerappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.two.pilots.messengerappkotlin.data_class.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.all_users.*
import kotlinx.android.synthetic.main.recycler_view_inflated_main_activity.view.*

class AllUsers : AppCompatActivity() {
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth

    companion object {
        const val KEY = "USER DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_users)

        title = "All Users"

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUserId = firebaseAuth.uid

        //  adapter by GROUPIE
        val adapter = GroupAdapter<ViewHolder>()

        //  onClick
        //  when a user's profile is clicked, would get redirected to the "ChatLogActivity"
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem

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
                    val fromCache:String = if (it.metadata.isFromCache) "Cache" else "Server"

                    Log.d("AllUsers", "onCreate (line 55): $fromCache")

                    it.forEach {
                        if (it.data["userId"] != currentUserId) {
                            //  de-serializing the data of collection "Users" to "Users" class
                            val userData = it.toObject(Users::class.java)

                            adapter.add(UserItem(userData))
                        }
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
            Log.d("UserItem", "bind (line 84): user ==> ${user}")

            viewHolder.itemView.username_recycler_inflated_main_activity.text = user.username
            viewHolder.itemView.email_recycler_inflated_main_activity.text = user.email

            if (user.imageURL.isNotEmpty()) {
                Picasso.get()
                    .load(user.imageURL)
                    .error(R.drawable.ic_person_black_24dp)
                    .into(viewHolder.itemView.user_image_recycler_inflated_main_activity)
            } else {
                Picasso.get()
                    .load(R.drawable.ic_person_black_24dp)
                    .error(R.drawable.ic_person_black_24dp)
                    .into(viewHolder.itemView.user_image_recycler_inflated_main_activity)
            }
        }
    }
}
