package edu.rosehulman.wegroup0

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_msg_detail.*
import kotlinx.android.synthetic.main.fragment_msg_detail.view.*
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDateTime

class MsgDetailFragment(var friend: Friend, var username: String) : Fragment() {

    lateinit var adapter: UserChatListAdapter
    var msgs: ArrayList<Message> = ArrayList()
    var connection: Connection? = null
    var gview: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        adapter = UserChatListAdapter(context!!, username)
        getMsgs()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.add_friend_menu).isVisible = false
        menu.findItem(R.id.add_post_menu).isVisible = false
        menu.findItem(R.id.log_out).isVisible = false
        menu.findItem(R.id.show_other_menu).isVisible = false
        menu.findItem(R.id.show_mine_menu).isVisible = false
        menu.findItem(R.id.group_msg_menu).isVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_msg_detail, container, false)
        view.messages_view.adapter = adapter
        gview = view.messages_view
        view.sendMessage.setOnClickListener {
            val input = editText.text
            if (check(input.toString())) {
                val current = LocalDateTime.now()
                adapter.add(Message(username, friend.username, input.toString(), current.toString()))
                input.clear()
            } else {
                Toast.makeText(activity, "No injection plz", Toast.LENGTH_LONG).show()
                input.clear()
            }
        }
        return view
    }

    fun check(s: String): Boolean {
        val b1 = s.contains(";", true)
        val b2 = s.contains("drop", true)
        val b3 = s.contains("alter", true)
        val b4 = s.contains("delete", true)
        val b5 = s.contains("add", true)
        val b6 = s.contains("update", true)
        if (b1 && b2 || b1 && b3 || b1 && b4 || b1 && b5 || b1 && b6) {
            return false
        }
        return true
    }

    fun getMsgs() {
        connection = ConnectionClass().dbCon()
        val query = "Select * From fn_getChatLog('$username', '${friend.username}')"
        Log.d("login", query)
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val sender = rs.getString("sendby")
                val receiver = rs.getString("sendto")
                val newMsg = Message(sender, receiver, rs.getString("text"), rs.getString("time"))
                if (!msgs.contains(newMsg)) {
                    msgs.add(newMsg)
                    Log.d("login", msgs.toString())
                    adapter.updateAdapter(msgs)
                    gview?.smoothScrollToPosition(100)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}