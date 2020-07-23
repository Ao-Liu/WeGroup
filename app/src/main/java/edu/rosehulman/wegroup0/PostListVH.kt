package edu.rosehulman.wegroup0

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.add_friend.view.*
import kotlinx.android.synthetic.main.row_view_msg.view.*
import kotlinx.android.synthetic.main.row_view_msg.view.card_initial
import kotlinx.android.synthetic.main.row_view_post.view.*

class PostListVH(itemView: View, var adapter: PostListAdapter, var username: String, val context: Context) :
    RecyclerView.ViewHolder(itemView) {

    private var posterTextView = itemView.poster as TextView
    private val main = itemView.post_main as TextView
    private val iView = itemView.card_initial as ImageView
    private val time = itemView.post_time as TextView
    private val likedBy = itemView.liked_by as TextView
    private val comment = itemView.post_comment as TextView
    var post: Post? = null
    var tempLikes: ArrayList<String> = ArrayList()

    init {
        itemView.comment_btn.setOnClickListener {
            adapter.addLike(adapterPosition, post?.time, post?.owner)
        }
        itemView.comment_btn.setOnLongClickListener {
            postAlert()
            true
        }
    }

    fun postAlert() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add a Comment")
        val view = LayoutInflater.from(context).inflate(R.layout.add_comment, null, false)
        builder.setView(view)
        var text = ""
        builder.setPositiveButton("Comment!") { _, _ ->
            text = view.text_edit_text.text.toString()
            if (text == "") {
                Toast.makeText(context, "Please enter something to post", Toast.LENGTH_LONG).show()
            } else {
                adapter.addComment(adapterPosition, post?.time, post?.owner, text)
            }
            Log.d("login6", text)
        }
        builder.setNegativeButton("Cancel") { _, _ ->
        }
        builder.create().show()

    }

    fun bind(p: Post) {
        post = p
        posterTextView.text = p.owner
        main.text = p.text
        time.text = p.time
        var likes: String = "liked by: "
        for (like in p.likedBy) {
            if (!tempLikes.contains(like)) {
                likes += "$like "
                tempLikes.add(like)
            } else {
                continue
            }
        }
        likedBy.text = likes
        var cmts = ""
        for (c in p.comment.keys) {
            cmts += "$c: ${p.comment[c]}\n"
        }
        comment.text = cmts
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