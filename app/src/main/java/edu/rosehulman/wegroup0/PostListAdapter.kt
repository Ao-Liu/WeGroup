package edu.rosehulman.wegroup0

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_view_post.view.*
import java.sql.CallableStatement
import java.sql.SQLException
import java.sql.Types

class PostListAdapter(
    var context: Context?,
    var posts: ArrayList<Post>,
    var username: String
) : RecyclerView.Adapter<PostListVH>() {

    override fun onBindViewHolder(holder: PostListVH, position: Int) {
        holder.bind(posts[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListVH {
        val view = LayoutInflater.from(context).inflate(R.layout.row_view_post, parent, false)
        return PostListVH(view, this, username, context!!)
    }

    fun addLike(pos: Int, time: String?, postby: String?){
        val connection = ConnectionClass().dbCon()
        var cs: CallableStatement? = null
        try {
            cs = connection?.prepareCall("{? = call add_likedby(?,?,?,?)}")
            cs!!.registerOutParameter(1, Types.INTEGER)
            cs.setString(2, postby)
            cs.setString(3, time)
            cs.setString(4, "")
            cs.setString(5, username)
            cs.execute()
            cs.getInt(1)
            when (cs.getInt(1)) {
                0 -> {
                    Toast.makeText(context, "Liked", Toast.LENGTH_LONG).show()
                    var temPost = posts[pos]
                    temPost.likedBy.add(username)
                    posts.removeAt(pos)
                    posts.add(temPost)
                    notifyItemChanged(pos)
                }
                1 -> {
                    Toast.makeText(context, "You've already liked this post", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    fun addComment(pos: Int, time: String?, postby: String?, text: String?){
        val connection = ConnectionClass().dbCon()
        var cs: CallableStatement? = null
        try {
            cs = connection?.prepareCall("{? = call add_likedby(?,?,?,?)}")
            cs!!.registerOutParameter(1, Types.INTEGER)
            cs.setString(2, postby)
            cs.setString(3, time)
            cs.setString(4, text)
            cs.setString(5, username)
            cs.execute()
            cs.getInt(1)
            when (cs.getInt(1)) {
                0 -> {
                    Toast.makeText(context, "Liked", Toast.LENGTH_LONG).show()
                    var temPost = posts[pos]
                    temPost.likedBy.add(username)
                    posts.removeAt(pos)
                    posts.add(temPost)
                    notifyItemChanged(pos)
                }
                1 -> {
                    Toast.makeText(context, "You've already commented this post", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updateAdapter(post: Post){
        posts.add(0, post)
        notifyItemChanged(0)
    }

    fun updateAdapterPosts(inPosts: ArrayList<Post>){
        posts = inPosts
        notifyItemChanged(0)
    }
}