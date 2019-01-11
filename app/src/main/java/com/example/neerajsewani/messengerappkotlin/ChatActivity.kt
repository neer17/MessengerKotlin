package com.example.neerajsewani.messengerappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_layout_inflated.view.*

class ChatActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var adapter: GroupAdapter<ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        var currentUser = firebaseAuth.uid

        //  setting the adapter
        adapter = GroupAdapter()
        recycler_view_chat_activity.adapter = adapter

        //  getting all the users to whom have I sent messages
        var query = firestore.collection("messages")
            .whereEqualTo("fromId", currentUser)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        query.addSnapshotListener { snapshots, e ->
            if (e != null){
                Log.e("ChatActivity", "onCreate (line 32): ", e)
                return@addSnapshotListener
            }

            for (data in snapshots!!.documentChanges){
                when(data.type){
                    DocumentChange.Type.ADDED -> {
                        val username = data.document.data["username"].toString()
                        val imageURL = data.document.data["imageURL"].toString()
                        val latestMessage = data.document.data["message"].toString()

                        Log.d("ChatActivity", "onCreate (line 55):" +
                                " username ==> $username imageURL ==> $imageURL latetstMessage ==> $latestMessage")

                        adapter.add(LatestMessages(username, latestMessage, imageURL))
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.all_users_chat_activity_menu -> {
                intent = Intent(this, AllUsers::class.java)
                startActivity(intent)
            }

            R.id.logout_chat_activity_menu -> {
                if (firebaseAuth.currentUser != null) {
                    firebaseAuth.signOut()

                    intent = Intent(this, SignUpScreen::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class LatestMessages( val username: String, val latestMessage: String, val imageURL: String): Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.chat_layout_inflated
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_tv_chat_layout_inflated.text = username
            viewHolder.itemView.last_message_tv_chat_layout_inflated.text = latestMessage

            Picasso.get().load(imageURL).into(viewHolder.itemView.user_image_chat_layout_inflated)
        }
    }
}
