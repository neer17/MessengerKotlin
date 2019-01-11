package com.example.neerajsewani.messengerappkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import com.example.neerajsewani.messengerappkotlin.data_class.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat_log.view.*
import kotlinx.android.synthetic.main.receivers_layout.view.*
import kotlinx.android.synthetic.main.sender_layout.view.*

class ChatLogActivity : AppCompatActivity() {

    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var senderImageURL: String
    lateinit var receiverImageURL: String
    lateinit var adapter: GroupAdapter<ViewHolder>
    lateinit var user: Users

    companion object {
        val TAG = "ChatLogActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        senderImageURL = "image"
        receiverImageURL = "image"


        adapter = GroupAdapter()

        recycler_view_chat_log_activity.adapter = adapter

        //  getting the Intent from "AllUsers"
        user = intent.getParcelableExtra<Users>(AllUsers.KEY)

        //  getting sender's and receiver's imageURL
        getImageURLs()

        //  getting receiver's messages
        getReceiversMessages()

        //  getting sender's message
        getSendersMessages()

        //  enter message edit text
        enter_message_et_chat_log_activity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                send_bt_chat_log_activity.isEnabled = !s.isNullOrBlank()
            }
        })

        //  send button onClick
        send_bt_chat_log_activity.setOnClickListener {
            val message = enter_message_et_chat_log_activity.text.toString()

            //  clearing the edit text and dragging the recycler view to the last position
            enter_message_et_chat_log_activity.text.clear()
            recycler_view_chat_log_activity.scrollToPosition(adapter.itemCount - 1)

            var messageMap = HashMap<String, Any>()
            messageMap["fromId"] = firebaseAuth.uid.toString()
            messageMap["receiverId"] = user.userId
            messageMap["message"] = message
            messageMap["timestamp"] = System.currentTimeMillis()
            messageMap["imageURL"] = user.imageURL
            messageMap["username"] = user.username

            //  uploading the data to the "messages" collection
            var messagesReference = firebaseFirestore.collection("messages")

            messagesReference.add(messageMap).addOnSuccessListener {

            }.addOnFailureListener {
                Log.e("ChatLogActivity", "onCreate (line 103): ", it)
            }
        }
    }

    private fun getSendersMessages() {
        //  fetching data for SENDER
        var senderQuery = firebaseFirestore.collection("messages").whereEqualTo("receiverId", user.userId)
            .whereEqualTo("fromId", firebaseAuth.uid)
        senderQuery.addSnapshotListener(EventListener<QuerySnapshot> { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                return@EventListener
            }

            var count = snapshot?.count()
            Log.d("ChatLogActivity", "onCreate (line 68): count ==> $count")
            for (dc in snapshot!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        var senderMessage: String = dc.document.data["message"].toString()
                        adapter.add(SenderUser(senderMessage, senderImageURL))
                    }
                }
            }

        })
    }

    private fun getReceiversMessages() {
        //  fetching data for RECEIVER
        var receiverQuery = firebaseFirestore.collection("messages").whereEqualTo("receiverId", firebaseAuth.uid)
            .whereEqualTo("fromId", user.userId)
        receiverQuery.addSnapshotListener(EventListener<QuerySnapshot> { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                return@EventListener
            }

            for (dc in snapshot!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        Log.d("ChatLogActivity", "onCreate (line 41): ${dc.document.data["message"]}")

                        var receiverMessage: String = dc.document.data["message"].toString()
                        adapter.add(ReceiverUser(receiverMessage, receiverImageURL))
                    }
                }
            }
        })
    }

    private fun getImageURLs() {
        //  getting sender's data from the firestore
        firebaseFirestore.collection("users").whereEqualTo("userId", firebaseAuth.uid).get()
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "onCreate (line 93): sender's data ==> $it")

                it.forEach { it ->
                    senderImageURL = it.data["imageURL"].toString()
                    Log.d("ChatLogActivity", "onCreate (line 58): senders image ==> $senderImageURL")
                }
            }.addOnFailureListener {
                Log.e("ChatLogActivity", "onCreate (line 95): ", it)
            }

        //  getting receiver's data from the firestore
        firebaseFirestore.collection("users").whereEqualTo("userId", user.userId).get()
            .addOnSuccessListener {
                Log.d("ChatLogActivity", "onCreate (line 93): receiver's data ==> ${it}")

                it.forEach { it ->
                    receiverImageURL = it.data["imageURL"].toString()
                    Log.d("ChatLogActivity", "getImageURLs (line 172): receivers image URL ==> $receiverImageURL")
                }
            }.addOnFailureListener {
                Log.e("ChatLogActivity", "onCreate (line 95): ", it)
            }
    }

    class SenderUser(val message: String, val imageURL: String) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.sender_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.tv_sender_layout.text = message

            Picasso.get().load(imageURL).into(viewHolder.itemView.user_image_sender_layout)
        }
    }

    class ReceiverUser(val message: String, val imageURL: String) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.receivers_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.tv_receivers_layout.text = message

            Picasso.get().load(imageURL).into(viewHolder.itemView.iv_receivers_layout)
        }
    }
}

