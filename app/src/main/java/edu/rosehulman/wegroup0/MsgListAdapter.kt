package edu.rosehulman.wegroup0

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.del_alert.view.*
import java.sql.CallableStatement
import java.sql.SQLException
import java.sql.Types

class MsgListAdapter(
    var context: Context?,
    var listener: MsgListFragment.OnMsgSelectedListener?,
    var msgs: ArrayList<Message>,
    var username: String
) :
    RecyclerView.Adapter<MsgListVH>() {

    override fun onBindViewHolder(holder: MsgListVH, pos: Int) {
        holder.bind(msgs[pos])
    }

    override fun getItemCount(): Int = msgs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgListVH {
        val view = LayoutInflater.from(context).inflate(R.layout.row_view_msg, parent, false)
        return MsgListVH(view, this, username)
    }

    fun updateAdapter(imsgs: ArrayList<Message>) {
        msgs = imsgs
        notifyItemChanged(0)
    }

    fun selectMsgAt(adapterPosition: Int) {
        val msg = msgs[adapterPosition]
        listener?.onMsgSelected(msg)
    }

    fun deleteMsgAt(adapterPosition: Int) {
        val connection = ConnectionClass().dbCon()
        val msg = msgs[adapterPosition]
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Are you sure?")
        val view = LayoutInflater.from(context).inflate(R.layout.del_alert, null, false)
        builder.setView(view)
        if (msg.sendBy == username) {
            view.alert_dialog.text = "You are deleting chat history with ${msg.sendTo}"
        } else if (msg.sendTo == username) {
            view.alert_dialog.text = "You are deleting chat history with ${msg.sendBy}"
        }
        builder.setPositiveButton("Yes") { _, _ ->
            var cs: CallableStatement? = null
            try {
                cs = connection?.prepareCall("{? = call delete_ChatHistory(?,?)}")
                cs!!.registerOutParameter(1, Types.INTEGER)
                cs.setString(2, username)
                cs.setString(3, msg.sendBy)
                cs.execute()
                cs.getInt(1)
                when (cs.getInt(1)) {
                    0 -> {
                        Toast.makeText(context, "msg deleted", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
                Log.d("login99", "3")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            msgs.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
        }
        builder.setNegativeButton("No") { _, _ ->
            //nothing happens
        }
        builder.create().show()
    }

}