package com.example.neerajsewani.messengerappkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        var adapter = GroupAdapter<ViewHolder>()
        adapter.add(SenderUser())
        adapter.add(ReceiverUser())

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

