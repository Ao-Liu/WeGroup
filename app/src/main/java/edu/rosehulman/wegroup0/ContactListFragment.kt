package edu.rosehulman.wegroup0

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_msg_list.*
import kotlinx.android.synthetic.main.friend_req_alert.view.*
import java.sql.*

class ContactListFragment(var friends: ArrayList<Friend>, var candidates: ArrayList<Friend>) : Fragment() {

    private var listener: ContactListFragment.OnFriSelectedListener? = null
    var connection: Connection? = null
    lateinit var adapter: ContactListAdapter
    var gotFr = false
    var view: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        connection = ConnectionClass().dbCon()
        Log.d("login", candidates.toString())
        for (candidate in candidates) {
            showFriAlert(candidate)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
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
        val recyclerView = inflater.inflate(R.layout.fragment_contact_list, container, false) as RecyclerView
        view = recyclerView
        val adap: ContactListAdapter = ContactListAdapter(
            context, listener,
            (activity as MainActivity).actUsername, friends, recyclerView.layoutManager
        )
        adapter = adap
        getFriends()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context) as RecyclerView.LayoutManager?
        recyclerView.setHasFixedSize(true)
        return recyclerView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFriSelectedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFriSelectedListener")
        }
    }

    override fun onStart() {
        super.onStart()
        activity!!.nav_view.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        activity!!.nav_view.visibility = View.INVISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun showFriAlert(candidate: Friend) {
        val sender = candidate.username
        val text = candidate.text
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle("You've got a friend request")
        val view = LayoutInflater.from(context).inflate(R.layout.friend_req_alert, null, false)
        builder.setView(view)
        view.req_sender.text = "from $sender"
        view.req_text.text = "'$text'"
        //add
        builder.setPositiveButton("Accept Request") { _, _ ->
            var cs: CallableStatement? = null
            try {
                cs = connection?.prepareCall("{? = call add_friend(?,?)}")
                cs!!.registerOutParameter(1, Types.INTEGER)
                cs.setString(2, sender)
                cs.setString(3, (activity as MainActivity).actUsername)
                cs.execute()
                cs.getInt(1)
                when (cs.getInt(1)) {
                    2 -> {
                        Toast.makeText(context, "No such user", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        Log.d("login", "add failed 1")
                        Toast.makeText(context, "Receiver cannot be null", Toast.LENGTH_LONG).show()
                    }
                    0 -> {
                        Log.d("login", "add failed 1")
                        Toast.makeText(context, "Friend Added", Toast.LENGTH_LONG).show()
                        candidates.remove(candidate)
                        getFriends()
                    }
                    else -> {
                        Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            var cs1: CallableStatement? = null
            try {
                cs1 = connection?.prepareCall("{? = call delete_adds(?,?)}")
                cs1!!.registerOutParameter(1, Types.INTEGER)
                cs1.setString(2, sender)
                cs1.setString(3, (activity as MainActivity).actUsername)
                cs1.execute()
                cs1.getInt(1)
                when (cs1.getInt(1)) {
                    2 -> {
                        Toast.makeText(context, "No such user", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        Log.d("login", "add failed 1")
                        Toast.makeText(context, "Receiver cannot be null", Toast.LENGTH_LONG).show()
                    }
                    0 -> {
                        Log.d("login", "add failed 1")
                        Toast.makeText(context, "Friend Added", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        //delete
        builder.setNegativeButton("Deny Request") { _, _ ->
            var cs2: CallableStatement? = null
            try {
                cs2 = connection?.prepareCall("{? = call delete_adds(?,?)}")
                cs2!!.registerOutParameter(1, Types.INTEGER)
                cs2.setString(2, sender)
                cs2.setString(3, (activity as MainActivity).actUsername)
                cs2.execute()
                cs2.getInt(1)
                when (cs2.getInt(1)) {
                    2 -> {
                        Toast.makeText(context, "No such user", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        Log.d("login", "add failed 1")
                        Toast.makeText(context, "Receiver cannot be null", Toast.LENGTH_LONG).show()
                    }
                    0 -> {
                        Log.d("login", "add failed 1")
                        candidates.remove(candidate)
                        Toast.makeText(context, "Friend Request Denied", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        builder.create().show()
    }

    fun getFriends() {
        connection = ConnectionClass().dbCon()
        val query = "Select * From fn_getFriends('${(activity as MainActivity).actUsername}')"
        Log.d("login", query)
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val su = rs.getString("friendsuname")
                val ru = rs.getString("friendruname")
                if (su == (activity as MainActivity).actUsername) {
                    var isIncluded = false
                    for (friend in friends) {
                        if (friend.username == ru) {
                            isIncluded = true
                            break
                        }
                    }
                    if (!isIncluded) {
                        val newFri = Friend(ru, "abc", false, "", (0..7).shuffled().last())
                        friends.add(newFri)
                        adapter.updateAdapter(friends)
                        view?.smoothScrollToPosition(100)
                    }
                } else if (ru == (activity as MainActivity).actUsername) {
                    var isIncluded = false
                    for (friend in friends) {
                        if (friend.username == su) {
                            isIncluded = true
                            break
                        }
                    }
                    if (!isIncluded) {
                        val newFri = Friend(su, "abc", false, "", (0..7).shuffled().last())
                        friends.add(newFri)
                        adapter.updateAdapter(friends)
                        view?.smoothScrollToPosition(100)
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    interface OnFriSelectedListener {
        fun onFriSelected(friend: Friend)
        fun onFriDeleted(friend: Friend)
    }


}

//    private val updateTextTask = object : Runnable {
//        override fun run() {
//            checkFriendRequest()
//            mainHandler.postDelayed(this, 1000)
//        }
//    }
//
//    fun checkFriendRequest() {
//        val query = "SELECT * from [adds] where RequestAccepter = '${(activity as MainActivity).actUsername}'"
//        Log.d("login", query)
//        var stmt: Statement? = null
//        try {
//            stmt = connection?.createStatement()
//            val rs = stmt?.executeQuery(query)
//            if (rs!!.next()) {
//                if (gotFr) {
//                    mainHandler.postDelayed(updateTextTask, 60000)
//
//                    handleFriReq()
//                } else {
//                    Toast.makeText(context, "you've got a friend request", Toast.LENGTH_LONG).show()
//                    gotFr = true
////                    handleFriReq()
//                }
//            } else {
//                gotFr = false
//            }
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
//    }
//
//
//    override fun onPause() {
//        super.onPause()
//        mainHandler.removeCallbacks(updateTextTask)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mainHandler.post(updateTextTask)
//    }


//    fun handleFriReq() {
//        val query = "SELECT * from [adds] where RequestAccepter = '${(activity as MainActivity).actUsername}'"
//        Log.d("login", query)
//        var stmt: Statement? = null
//        try {
//            stmt = connection?.createStatement()
//            val rs = stmt?.executeQuery(query)
////            if (rs!!.next()) {
//            while (rs!!.next()) {
//                val sender = rs.getString("requestsender")
//                val text = rs.getString("text")
//                showFriAlert(sender, text)
////                }
////            } else {
//                Log.d("login", "no friend request")
//            }
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
//    }