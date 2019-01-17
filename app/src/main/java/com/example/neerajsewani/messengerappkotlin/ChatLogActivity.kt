package com.example.neerajsewani.messengerappkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import com.example.neerajsewani.messengerappkotlin.data_class.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.receivers_layout.view.*
import kotlinx.android.synthetic.main.sender_layout.view.*
import java.lang.Exception

class ChatLogActivity : AppCompatActivity() {

    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var messagesReference: DatabaseReference
    lateinit var senderImageURL: String
    lateinit var receiverImageURL: String
    lateinit var adapter: GroupAdapter<ViewHolder>
    lateinit var user: Users
    lateinit var latestMessagesCollection: CollectionReference
    lateinit var currentUserId: String
    lateinit var message: String
    lateinit var currentUserImageURL: String
    lateinit var currentUsersUsername: String

    companion object {
        val TAG = "ChatLogActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUserId = firebaseAuth.uid!!
        firebaseFirestore = FirebaseFirestore.getInstance()
        messagesReference = FirebaseDatabase.getInstance().getReference("messages")
        latestMessagesCollection = firebaseFirestore.collection("latest-messages")


        senderImageURL = "image"
        receiverImageURL = "image"

        //  setting the adapter
        adapter = GroupAdapter()
        recycler_view_chat_log_activity.adapter = adapter

        //  getting the Intent from "AllUsers" or "ChatActivity"
        if (intent != null){
            user = if (intent.getStringExtra("uniqueId") == "ChatActivity"){
                intent.getParcelableExtra(AllUsers.KEY)
            } else {
                intent.getParcelableExtra(AllUsers.KEY)
            }
        }


        //  getting sender's and receiver's imageURL
        getImageURLs()

        //  getting all messages
        getAllMessages()

        //  getting current user's details
        getCurrentUsersDetails()

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
            message = enter_message_et_chat_log_activity.text.toString()

            //  clearing the edit text and dragging the recycler view to the last position
            enter_message_et_chat_log_activity.text.clear()
            recycler_view_chat_log_activity.scrollToPosition(adapter.itemCount - 1)

            val messageMap = HashMap<String, Any>()
            messageMap["fromId"] = firebaseAuth.uid.toString()
            messageMap["receiverId"] = user.userId
            messageMap["message"] = message
            messageMap["timestamp"] = System.currentTimeMillis()
            messageMap["imageURL"] = user.imageURL
            messageMap["username"] = user.username

            //  for the sender
            val messagePushedRef = messagesReference.child(currentUserId).child(user.userId).push()
            messagePushedRef.setValue(messageMap).addOnFailureListener {
                Log.e("ChatLogActivity", "onCreate (line 104): ", it)
            }

            //  for the receiver
            messagesReference.child(user.userId).child(currentUserId).push().setValue(messageMap).addOnFailureListener {
                Log.e("ChatLogActivity", "onCreate (line 109): ", it)
            }

            //  adding or updating data in the "latest-messages" collection
            addOrUpdateLatestMessagesCollection()
        }
    }

    private fun getCurrentUsersDetails() {
        firebaseFirestore.collection("users").whereEqualTo("userId", currentUserId)
            .get().addOnSuccessListener {
                for (doc in it.documents) {
                    currentUserImageURL = doc["imageURL"].toString()
                    currentUsersUsername = doc["username"].toString()
                }
            }.addOnFailureListener {
                Log.e("ChatLogActivity", "getCurrentUsersDetails (line 132): ", it)
            }
    }

    private fun addOrUpdateLatestMessagesCollection() {

        //  this would be stored under current user's collection
        val sendersMap = HashMap<String, Any>()
        sendersMap["usersId"] = user.userId
        sendersMap["username"] = user.username
        sendersMap["userImageURL"] = user.imageURL
        sendersMap["latest_message"] = message
        sendersMap["timestamp"] = System.currentTimeMillis()

        //  this would be stored under receiver's user collection
        val receiversMap = HashMap<String, Any>()
        receiversMap["usersId"] = currentUserId
        receiversMap["username"] = currentUsersUsername
        receiversMap["userImageURL"] = currentUserImageURL
        receiversMap["latest_message"] = message
        receiversMap["timestamp"] = System.currentTimeMillis()


        firebaseFirestore.collection("latest_messages").document().collection(currentUserId)
            .whereEqualTo("usersId", user.userId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(
                        "ChatLogActivity",
                        "addOrUpdateLatestMessagesCollection (line 128): ",
                        firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }

                //  if don't find the receiver'd id then adding the document in the sender's and receiver's collection
                //  otherwise updating with the latest message
                if (querySnapshot!!.isEmpty) {
                    firebaseFirestore.collection("latest_messages").document(currentUserId).collection(currentUserId)
                        .document(user.userId).set(sendersMap).addOnFailureListener {
                            Log.e("ChatLogActivity", "addOrUpdateLatestMessagesCollection (line 152): ", it)
                        }

                    firebaseFirestore.collection("latest_messages").document(user.userId).collection(user.userId)
                        .document(currentUserId).set(receiversMap).addOnFailureListener {
                            Log.e("ChatLogActivity", "addOrUpdateLatestMessagesCollection (line 155): ", it)
                        }
                } else {
                    firebaseFirestore.collection("latest_messages").document(currentUserId).collection(currentUserId)
                        .document(user.userId).update(sendersMap).addOnFailureListener {
                            Log.e("ChatLogActivity", "addOrUpdateLatestMessagesCollection (line 152): ", it)
                        }

                    firebaseFirestore.collection("latest_messages").document(user.userId).collection(user.userId)
                        .document(currentUserId).set(sendersMap).addOnFailureListener {
                            Log.e("ChatLogActivity", "addOrUpdateLatestMessagesCollection (line 152): ", it)
                        }
                }
            }
    }

    private fun getAllMessages() {
        messagesReference.child(currentUserId).child(user.userId).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val messages = p0.getValue(Messages::class.java)
                if (messages!!.fromId == currentUserId) {
                    adapter.add(SenderUser(messages.message, messages.timestamp))
                } else {
                    adapter.add(ReceiverUser(messages.message, messages.timestamp))
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
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

    class SenderUser(private val message: String = "", private val timestamp: Long = 0) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.sender_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.last_message_sender_layout.text = message
            viewHolder.itemView.timestamp_sender_layout.text = timestamp.toString()
        }
    }

    class ReceiverUser(private val message: String = "", private val timestamp: Long = 0) : Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.receivers_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.latest_message_receivers_layout.text = message
            viewHolder.itemView.timestamp_receivers_layout.text = timestamp.toString()
        }
    }
}



