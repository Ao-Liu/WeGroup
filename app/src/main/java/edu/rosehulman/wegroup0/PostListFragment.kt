package edu.rosehulman.wegroup0

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_friend.view.*
import java.sql.CallableStatement
import java.sql.SQLException
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime

class PostListFragment(var friends: ArrayList<String>) : Fragment() {

    var myPosts: ArrayList<Post> = ArrayList()
    var friPosts: ArrayList<Post> = ArrayList()
    var adapter: PostListAdapter? = null
    var v: RecyclerView? = null

    override fun onStart() {
        super.onStart()
        activity!!.nav_view.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        activity!!.nav_view.visibility = View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        friPosts.clear()
        for (fri in friends) {
            getPosts(fri)
        }
        adapter = PostListAdapter(context, friPosts, (activity as MainActivity).actUsername)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.add_friend_menu).isVisible = false
        menu.findItem(R.id.log_out).isVisible = false
        menu.findItem(R.id.group_msg_menu).isVisible = false
        menu.findItem(R.id.add_post_menu).setOnMenuItemClickListener {
            postAlert()
            true
        }
        menu.findItem(R.id.show_mine_menu).setOnMenuItemClickListener {
            myPosts.clear()
            getMyPosts()
            val newAdapter = PostListAdapter(context, myPosts, (activity as MainActivity).actUsername)
            v?.adapter = newAdapter
            true
        }
        menu.findItem(R.id.show_other_menu).setOnMenuItemClickListener {
            friPosts.clear()
            for (fri in friends) {
                getPosts(fri)
            }
            val newAdapter = PostListAdapter(context, friPosts, (activity as MainActivity).actUsername)
            v?.adapter = newAdapter
            true
        }
    }


    fun postAlert() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Make a Post")
        val view = LayoutInflater.from(activity).inflate(R.layout.add_post, null, false)
        builder.setView(view)
        builder.setPositiveButton("Post!") { _, _ ->
            val text = view.text_edit_text.text.toString()
            if (text == "") {
                Toast.makeText(activity, "Please enter something to post", Toast.LENGTH_LONG).show()
            } else {
                addPost(text)
            }
        }
        builder.setNegativeButton("Cancel") { _, _ ->
        }
        builder.create().show()
    }

    fun addPost(text: String) {
        if (check(text)) {
            val connection = ConnectionClass().dbCon()
            var cs: CallableStatement? = null
            try {
                cs = connection?.prepareCall("{? = call add_post(?,?,?)}")
                cs!!.registerOutParameter(1, Types.INTEGER)
                val current = LocalDateTime.now()
                cs.setString(2, text)
                cs.setString(3, current.toString())
                cs.setString(4, (activity as MainActivity).actUsername)
                cs.execute()
                cs.getInt(1)
                when (cs.getInt(1)) {
                    0 -> {
                        Toast.makeText(activity, "Post successful", Toast.LENGTH_LONG).show()
                        val hash = HashMap<String, String>(1)
                        val post =
                            Post((activity as MainActivity).actUsername, text, hash, current.toString(), ArrayList(1))
                        adapter?.updateAdapter(post)
                        Log.d("login4", post.toString())
                    }
                    else -> {
                        Toast.makeText(activity, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                Toast.makeText(activity, "Fail", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(activity, "No injection plz", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recyclerView = inflater.inflate(R.layout.fragment_msg_list, container, false) as RecyclerView
        v = recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context) as RecyclerView.LayoutManager?
        recyclerView.setHasFixedSize(true)
        return recyclerView
    }

    fun getMyPosts() {
        val connection = ConnectionClass().dbCon()
        val query = "Select * From fn_getPosts('${(activity as MainActivity).actUsername}')"
        Log.d("login5", "my $query")
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val su = rs.getString("postby")
                val text = rs.getString("text")
                val time = rs.getString("posttime")
                val hash = HashMap<String, String>(1)
                val post = Post(su, text, getCommentby(su, time), time, getLikedby(su, time))
                myPosts.add(0, post)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getPosts(fri: String) {
        val connection = ConnectionClass().dbCon()
        val query = "Select * From fn_getPosts('$fri')"
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val su = rs.getString("postby")
                val text = rs.getString("text")
                val time = rs.getString("posttime")
                val hash = HashMap<String, String>(1)
                val post =
                    Post(su, text, getCommentby(su, time), time, getLikedby(su, time))
                friPosts.add(0, post)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getLikedby(postby: String, time: String): ArrayList<String> {
        var likedby: ArrayList<String> = ArrayList()
        val connection = ConnectionClass().dbCon()
        val query = "Select * From fn_getMyLikes('$postby', '$time')"
        Log.d("login6", query)
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val su = rs.getString("likedby")
                likedby.add(su)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return likedby
    }

    fun getCommentby(postby: String, time: String): HashMap<String, String> {
        var likedby: HashMap<String, String> = HashMap(1)
        val connection = ConnectionClass().dbCon()
        val query = "Select * From fn_getMyLikes('$postby', '$time')"
        Log.d("login6", query)
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val su = rs.getString("likedby")
                val text = rs.getString("text")
                val isLike = rs.getString("islike")
                if (text != "" && isLike == "no") {
                    if (likedby[su] == null) {
                        likedby[su] = text
                    } else {
                        likedby[su] += "\n $text"
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return likedby
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


}