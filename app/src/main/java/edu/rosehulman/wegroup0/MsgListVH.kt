package edu.rosehulman.wegroup0

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_view_msg.view.*

class MsgListVH(itemView: View, adapter: MsgListAdapter, var username: String) : RecyclerView.ViewHolder(itemView) {

    private val titleTextView = itemView.chatsWith as TextView
    private val brief = itemView.brief as TextView
    private val iView = itemView.card_initial as ImageView

    init {
        itemView.setOnClickListener {
            adapter.selectMsgAt(adapterPosition)
        }
        itemView.setOnLongClickListener {
            adapter.deleteMsgAt(adapterPosition)
            true
        }
    }

    fun bind(msg: Message) {
        if (msg.sendBy == username) {
            titleTextView.text = msg.sendTo
        } else if (msg.sendTo == username) {
            titleTextView.text = msg.sendBy
        }
        brief.text = msg.text
        when ((0..7).shuffled().last()) {
            0 -> {
                iView.setImageResource(R.drawable.random1)
            }
            1 -> {
                iView.setImageResource(R.drawable.random2)
            }
            2 -> {
                iView.setImageResource(R.drawable.random3)
            }
            3 -> {
                iView.setImageResource(R.drawable.random4)
            }
            4 -> {
                iView.setImageResource(R.drawable.random5)
            }
            5 -> {
                iView.setImageResource(R.drawable.random6)
            }
            6 -> {
                iView.setImageResource(R.drawable.random7)
            }
            7 -> {
                iView.setImageResource(R.drawable.random8)
            }
        }
    }


}

//        when (friend.index) {
//            0 -> {
//                iView.setImageResource(R.drawable.random1)
//            }
//            1 -> {
//                iView.setImageResource(R.drawable.random2)
//            }
//            2 -> {
//                iView.setImageResource(R.drawable.random3)
//            }
//            3 -> {
//                iView.setImageResource(R.drawable.random4)
//            }
//            4 -> {
//                iView.setImageResource(R.drawable.random5)
//            }
//            5 -> {
//                iView.setImageResource(R.drawable.random6)
//            }
//            6 -> {
//                iView.setImageResource(R.drawable.random7)
//            }
//            7 -> {
//                iView.setImageResource(R.drawable.random8)
//            }
//        }