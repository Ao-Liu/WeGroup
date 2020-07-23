package edu.rosehulman.wegroup0

import android.annotation.SuppressLint
import android.widget.TextView
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.SQLException
import java.sql.Types


class UserChatListAdapter(var context: Context, val username: String) : BaseAdapter() {

    private var messages: MutableList<Message> = ArrayList<Message>()
    var connection: Connection? = ConnectionClass().dbCon()
    var view: View? = null

    override fun getCount(): Int {
        return messages.size
    }

    override fun getItem(i: Int): Any {
        return messages[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    fun add(msg: Message) {
        messages.add(msg)
        var cs: CallableStatement? = null
        try {
            cs = connection?.prepareCall("{? = call add_message(?,?,?,?)}")
            cs!!.registerOutParameter(1, Types.INTEGER)
            cs.setString(2, msg.text)
            cs.setString(3, msg.sendBy)
            cs.setString(4, msg.sendTo)
            cs.setString(5, msg.time)
            cs.execute()
            cs.getInt(1)
            when (cs.getInt(1)) {
                2 -> {
                }
                1 -> {
                    Log.d("login", "add failed 1")
                }
                0 -> {
                    Log.d("login", "send successful")
                }
                else -> {
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        notifyDataSetChanged()
    }

    @SuppressLint("ViewHolder")
    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View {
        var convertView = convertView
        val holder = MessageViewHolder()
        view = convertView
        val messageInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val message = messages[i]
        if (message.sendBy == username) {
            convertView = messageInflater.inflate(R.layout.my_message, null)
            holder.messageBody = convertView.findViewById(R.id.message_body)
            convertView.tag = holder
            holder.messageBody!!.text = message.text
        } else {
            convertView = messageInflater.inflate(R.layout.their_message, null)
            holder.avatar = convertView.findViewById(R.id.avatar) as View
            holder.name = convertView.findViewById(R.id.name)
            holder.name!!.text = message.sendTo
            holder.messageBody = convertView.findViewById(R.id.message_body)
            convertView.tag = holder
            holder.name!!.text = message.sendBy
            holder.messageBody!!.text = message.text
        }

        return convertView
    }

    fun updateAdapter(inputMsgs: ArrayList<Message>){
        messages = inputMsgs

        notifyDataSetChanged()
    }

}

internal class MessageViewHolder {
    var avatar: View? = null
    var name: TextView? = null
    var messageBody: TextView? = null
}