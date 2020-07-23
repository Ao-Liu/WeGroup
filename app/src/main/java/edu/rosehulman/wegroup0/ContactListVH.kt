package edu.rosehulman.wegroup0

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_view_friend.view.*

class ContactListVH(itemView: View, adapter: ContactListAdapter) : RecyclerView.ViewHolder(itemView) {

    private val nnVIew = itemView.friend_name_card as TextView
    private val iView = itemView.card_initial as ImageView

    init {
        itemView.setOnClickListener {
            adapter.selectFriAt(adapterPosition)
        }
        itemView.setOnLongClickListener {
            adapter.deleteFriAt(adapterPosition)
            true
        }
    }

    fun bind(fri: Friend) {
        nnVIew.text = fri.username
        when (fri.index) {
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