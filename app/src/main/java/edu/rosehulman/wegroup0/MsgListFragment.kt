package edu.rosehulman.wegroup0

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.SQLException
import java.sql.Statement

@SuppressLint("ValidFragment")
class MsgListFragment(var msgs: ArrayList<Message>, var chatWith: ArrayList<String>) : Fragment() {

    private var listener: OnMsgSelectedListener? = null
    var adapter: MsgListAdapter? = null

    override fun onStart() {
        super.onStart()
        activity!!.nav_view.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        activity!!.nav_view.visibility = View.INVISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        checkFri()
//        checkDuplicate()
        val recyclerView = inflater.inflate(R.layout.fragment_msg_list, container, false) as RecyclerView
        val adap = MsgListAdapter(context, listener, msgs, (activity as MainActivity).actUsername)
        adapter = adap
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context) as RecyclerView.LayoutManager?
        recyclerView.setHasFixedSize(true)
        return recyclerView
    }

//    fun checkDuplicate() {
//        val arr = ArrayList<Message>()
//        val res: ArrayList<Message> = msgs
//        for (msg1 in res) {
//            for (msg2 in res) {
//                if (msg1.sendBy == msg2.sendBy && msg1.text != msg2.text) {
//                    msgs.remove(msg1)
//                }
//            }
//        }
//        Log.d("login88", arr.toString())
//        for (a in arr) {
//            msgs.remove(a)
//        }
//    }

    fun checkFri() {
        val connection = ConnectionClass().dbCon()
        chatWith.clear()
        val query = "Select * From fn_getFriends('${(activity as MainActivity).actUsername}')"
        Log.d("login12", query)
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val su = rs.getString("friendsuname")
                val ru = rs.getString("friendruname")
                if (su == (activity as MainActivity).actUsername) {
                    chatWith.add(ru)
                } else if (ru == (activity as MainActivity).actUsername) {
                    chatWith.add(su)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMsgSelectedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnMsgSelectedListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.add_post_menu).isVisible = false
        menu.findItem(R.id.show_other_menu).isVisible = false
        menu.findItem(R.id.show_mine_menu).isVisible = false
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnMsgSelectedListener {
        fun onMsgSelected(msg: Message)
        fun onMsgDeleted(msg: Message)
    }
}