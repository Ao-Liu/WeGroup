package edu.rosehulman.wegroup0

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.del_alert.view.*
import java.sql.*

class ContactListAdapter(
    var context: Context?,
    var listener: ContactListFragment.OnFriSelectedListener?,
    var username: String,
    var friends: ArrayList<Friend>,
    var layoutManager: RecyclerView.LayoutManager?
) : RecyclerView.Adapter<ContactListVH>() {

    var connection: Connection? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactListVH {
        val view = LayoutInflater.from(context).inflate(R.layout.row_view_friend, parent, false)
        return ContactListVH(view, this)
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(holder: ContactListVH, position: Int) {
        holder.bind(friends[position])
    }

    fun selectFriAt(adapterPosition: Int) {
        val fri = friends[adapterPosition]
        listener?.onFriSelected(fri)
    }

    fun deleteFriAt(adapterPosition: Int) {
        val connection = ConnectionClass().dbCon()
        val friend = friends[adapterPosition]
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Are you sure?")
        val view = LayoutInflater.from(context).inflate(R.layout.del_alert, null, false)
        builder.setView(view)
        view.alert_dialog.text = "You are deleting ${friend.username}"
        builder.setPositiveButton("Yes") { _, _ ->
            var cs: CallableStatement? = null
            try {
                cs = connection?.prepareCall("{? = call delete_friend(?,?)}")
                cs!!.registerOutParameter(1, Types.INTEGER)
                cs.setString(2, username)
                cs.setString(3, friend.username)
                cs.execute()
                cs.getInt(1)
                when (cs.getInt(1)) {
                    0 -> {
                        Toast.makeText(context, "Friend deleted", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            try {
                cs = connection?.prepareCall("{? = call delete_ChatHistory(?,?)}")
                cs!!.registerOutParameter(1, Types.INTEGER)
                cs.setString(2, username)
                cs.setString(3, friend.username)
                cs.execute()
                cs.getInt(1)
                when (cs.getInt(1)) {
                    0 -> {
//                        Toast.makeText(context, "msg deleted", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            friends.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
        }
        builder.setNegativeButton("No") { _, _ ->
            //nothing happens
        }
        builder.create().show()
    }


    fun updateAdapter(inputFriends: ArrayList<Friend>) {
        friends = inputFriends
        layoutManager?.scrollToPosition(friends.size - 1)
        notifyItemChanged(0)
    }
}