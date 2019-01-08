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
import kotlinx.android.synthetic.main.activity_chat_log.view.*

class ChatLogActivity : AppCompatActivity() {

    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth

    companion object {
        val TAG = "ChatLogActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        firebaseAuth = FirebaseAuth.getInstance()

        //  getting the Intent from "AllUsers"
        var user: Users = intent.getParcelableExtra<Users>(AllUsers.KEY)

        //  fetching data for RECEIVER
        firebaseFirestore = FirebaseFirestore.getInstance()
        var receiverQuery = firebaseFirestore.collection("messages").whereEqualTo("receiverId", user.userId)
        receiverQuery.addSnapshotListener(EventListener<QuerySnapshot> { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    return@EventListener
                }

            for (dc in snapshot!!.documentChanges) {
                when(dc.type){
                    DocumentChange.Type.ADDED -> {
                        Log.d("ChatLogActivity", "onCreate (line 41): ${dc.document.data}")
                    }
                }
            }

            })

        //  fetching data for SENDER
        firebaseFirestore = FirebaseFirestore.getInstance()
        var senderQuery = firebaseFirestore.collection("messages").whereEqualTo("fromId", firebaseAuth.uid)
        senderQuery.addSnapshotListener(EventListener<QuerySnapshot> { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                return@EventListener
            }

            var count = snapshot?.count()
            Log.d("ChatLogActivity", "onCreate (line 68): count ==> $count")
            for (dc in snapshot!!.documentChanges) {
                when(dc.type){
                    DocumentChange.Type.ADDED -> {
                        Log.d("ChatLogActivity", "onCreate (line 41): ${dc.document.data}")
                    }
                }
            }

        })

        //  enter message edit text
        enter_message_et_chat_log_activity.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()){
                    send_bt_chat_log_activity.isClickable = false
                }
            }
        })

        //  send button onClick
        send_bt_chat_log_activity.setOnClickListener{
            val message = enter_message_et_chat_log_activity.text.toString()

            var messageMap = HashMap<String, Any>()
            messageMap["fromId"] = firebaseAuth.uid.toString()
            messageMap["receiverId"] = user.userId
            messageMap["message"] = message


            var messagesReference = firebaseFirestore.collection("messages")
            messagesReference.add(messageMap).addOnSuccessListener {
                Log.d("ChatLogActivity", "onCreate (line 101): message successfully uploaded")
            }.addOnFailureListener{
                Log.e("ChatLogActivity", "onCreate (line 103): ", it)
            }
        }

        var adapter = GroupAdapter<ViewHolder>()

        recycler_view_chat_log_activity.adapter = adapter
    }

    class SenderUser : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.sender_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
        }
    }

    class ReceiverUser : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.receivers_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
        }
    }
}

