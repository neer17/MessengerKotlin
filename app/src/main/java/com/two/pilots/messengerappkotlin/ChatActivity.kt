package com.two.pilots.messengerappkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.two.pilots.messengerappkotlin.data_class.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    lateinit var currentUserId: String
    lateinit var latestMessagesreference: DatabaseReference
    lateinit var adapter: GroupAdapter<ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = firebaseAuth.uid!!
        latestMessagesreference = FirebaseDatabase.getInstance().getReference("latest-messages")

        var currentUser = firebaseAuth.uid

        //  setting the adapter
        adapter = GroupAdapter()
        recycler_view_chat_activity.adapter = adapter

        //  getting the latest message from all the recipients
        getRecipientLastMessages()

        //  onClick adapter
        adapter.setOnItemClickListener{item, view ->
            val messages = item as LatestMessages

            val user = Users(messages.userId, messages.username, "", messages.imageURL)

            intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(AllUsers.KEY, user)
            intent.putExtra("uniqueId", "ChatActivity")
            startActivity(intent)
        }
    }

    //  displaying recipient's last message with username and image
    private fun getRecipientLastMessages() {
        //  getting the latest messages and adding that to the adapter
        firestore.collection("latest_messages").document(currentUserId).collection(currentUserId).addSnapshotListener{
            querySnapshot, firebaseFirestoreException ->

            if (firebaseFirestoreException != null) {
                Log.e("ChatActivity", "getRecipientLastMessages (line 51): ", firebaseFirestoreException)
                return@addSnapshotListener
            }

            if (!querySnapshot!!.isEmpty) {
                //  clearing out the previous entry of data
                adapter.clear()

                querySnapshot.forEach {
                    val username = it.data["username"].toString()
                    val latestMessage = it.data["latest_message"].toString()
                    val usersImageURL = it.data["userImageURL"].toString()
                    val userId = it.data["usersId"].toString()
                    //  adding it to the adapter
                    adapter.add(LatestMessages(userId, username, latestMessage, usersImageURL))
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

    class LatestMessages(val userId: String, val username: String, val latestMessage: String, val imageURL: String): Item<ViewHolder>(){

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
