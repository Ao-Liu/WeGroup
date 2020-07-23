package edu.rosehulman.wegroup0

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.add_friend.view.*
import kotlinx.android.synthetic.main.add_friend.view.text_edit_text
import kotlinx.android.synthetic.main.add_group_msg.view.*
import kotlinx.android.synthetic.main.del_alert.view.*
import java.sql.*
import java.time.LocalDateTime

class MainActivity : AppCompatActivity(),
    MsgListFragment.OnMsgSelectedListener,
    SplashFragment.OnLoginButtonPressedListener,
    RegisterFragment1.OnProfileButtonPressedListener1,
    RegisterFragment2.OnProfileButtonPressedListener2,
    ContactListFragment.OnFriSelectedListener {

    lateinit var mainHandler: Handler
    private lateinit var textMessage: TextView
    var connection: Connection? = null
    var msgs = arrayListOf<Message>()
    var fris = arrayListOf<Friend>()
    var chatsWith = arrayListOf<String>()
    var candidates = arrayListOf<Friend>()
    var posts = arrayListOf<Post>()
    var friendStr = arrayListOf<String>()
    var actUsername: String = ""
    var gotFriendReq = false
    var msgAdapter: MsgListAdapter? = null
    var isUser = false

    private val updateTextTask = object : Runnable {
        override fun run() {
            checkFri()
            Log.d("login5", friendStr.toString())
            checkFriendRequest()
            getMsgs()
            mainHandler.postDelayed(this, 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTextTask)
    }

    fun checkFriendRequest() {
        if (isUser) {
            candidates.clear()
            val query = "Select * From fn_getCurrentRequests('$actUsername')"
            Log.d("login9", query)
            var stmt: Statement? = null
            try {
                stmt = connection?.createStatement()
                val rs = stmt?.executeQuery(query)
                while (rs!!.next()) {
                    if (gotFriendReq) {
                    } else {
                        Toast.makeText(this, "you've got a friend request", Toast.LENGTH_LONG).show()
                        gotFriendReq = true
                    }
                    val newFri = Friend(
                        rs.getString("RequestSender"),
                        "abc",
                        true,
                        rs.getString("text"),
                        (0..7).shuffled().last()
                    )
                    if (!candidates.contains(newFri)) {
                        candidates.add(newFri)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    fun checkFri() {
        if (isUser) {
            friendStr.clear()
            val query = "Select * From fn_getFriends('$actUsername')"
            Log.d("login12", query)
            var stmt: Statement? = null
            try {
                stmt = connection?.createStatement()
                val rs = stmt?.executeQuery(query)
                while (rs!!.next()) {
                    val su = rs.getString("friendsuname")
                    val ru = rs.getString("friendruname")
                    if (su == actUsername) {
                        friendStr.add(ru)
                    } else if (ru == actUsername) {
                        friendStr.add(su)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var switchTo: Fragment? = null
        var type: String = ""
        when (item.itemId) {
            R.id.navigation_home -> {
//                checkFri()
                getMsgs()
                Log.d("login20", msgs.toString())
                Log.d("login20", chatsWith.toString())
                textMessage.setText(R.string.title_msg)
                switchTo = MsgListFragment(msgs, chatsWith)
                type = "1"
            }
            R.id.navigation_dashboard -> {
                textMessage.setText(R.string.title_contacts)
                val input = candidates
                switchTo = ContactListFragment(fris, input)
            }
            R.id.navigation_notifications -> {
                textMessage.setText(R.string.title_posts)
                val input = candidates
                type = "p"
                val posts = ArrayList<Post>()
                val hash = HashMap<String, String>(1)
                hash["a"] = "good"
                hash["b"] = "cool"
                val post = Post("abc", "hello world", hash, "2012-1-1", arrayListOf("abc"))
                posts.add(post)
                switchTo = PostListFragment(friendStr)
            }
            R.id.navigation_me -> {
                textMessage.setText("Me")
                switchTo = ProfileFragment()

            }
        }
        if (switchTo != null) {
            val ft = supportFragmentManager.beginTransaction()
            if (type == "p") {
                ft.addToBackStack("detail")
            }
            ft.replace(R.id.container, switchTo)
            ft.commit()
            if (type == "1") {
                msgAdapter = (switchTo as MsgListFragment)?.adapter
                getMsgs()
                (switchTo as MsgListFragment).msgs = msgs
            }
        }
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        connection = ConnectionClass().dbCon()
        mainHandler = Handler(Looper.getMainLooper())
        val ft = supportFragmentManager.beginTransaction()
        val switchTo = SplashFragment(this, connection)
        ft.replace(R.id.container, switchTo)
        ft.commit()

        textMessage = findViewById(R.id.message)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    fun getMsgs() {
        if (isUser) {
            msgs.clear()
            chatsWith.clear()
            connection = ConnectionClass().dbCon()
            val query = "Select * From fn_getRelatedMessages('$actUsername')"
            Log.d("login11", query)
            var stmt: Statement? = null
            try {
                stmt = connection?.createStatement()
                val rs = stmt?.executeQuery(query)
                while (rs!!.next()) {
                    val sender = rs.getString("sendby")
                    val receiver = rs.getString("sendto")
                    val time = rs.getString("time")
                    if (sender == actUsername) {
                        if (!chatsWith.contains(receiver)) {
                            Log.d("login20", "1")
                            val newMsg = Message(receiver, sender, rs.getString("text"), time)
                            chatsWith.add(receiver)
                            msgs.add(newMsg)
                        } else {
                            val newMsg = Message(receiver, sender, rs.getString("text"), time)
                            for (msg in msgs) {
                                if (msg.sendBy == receiver || msg.sendBy == sender || msg.sendTo == receiver || msg.sendTo == sender) {
                                    msgs.remove(msg)
                                    msgs.add(newMsg)
                                    break
                                }
                            }
                        }
                        Log.d("login2", "adapter $msgAdapter")
                        msgAdapter?.updateAdapter(msgs)
                    } else if (receiver == actUsername) {
                        if (!chatsWith.contains(sender)) {
                            val newMsg = Message(sender, receiver, rs.getString("text"), time)
                            chatsWith.add(sender)
                            msgs.add(newMsg)
                        } else {
                            val newMsg = Message(receiver, sender, rs.getString("text"), time)
                            for (msg in msgs) {
                                if (msg.sendBy == receiver || msg.sendBy == sender || msg.sendTo == receiver || msg.sendTo == sender) {
                                    msgs.remove(msg)
                                    msgs.add(newMsg)
                                    break
                                }
                            }
                        }
                        msgAdapter?.updateAdapter(msgs)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.add_friend_menu -> {
                showAddDialog()
                true
            }
            R.id.add_post_menu -> {
//                postAlert()
                true
            }
            R.id.log_out -> {
                msgs.clear()
                fris.clear()
                candidates.clear()
                actUsername = ""
                gotFriendReq = false
                isUser = false
                chatsWith.clear()
                connection = ConnectionClass().dbCon()
                val ft = supportFragmentManager.beginTransaction()
                val switchTo = SplashFragment(this, connection)
                ft.replace(R.id.container, switchTo)
                ft.commit()
                true
            }
//            R.id.shortcut -> {
//                val ft = supportFragmentManager.beginTransaction()
//                val switchTo = MsgListFragment(msgs, chatsWith)
//                ft.replace(R.id.container, switchTo)
//                ft.commit()
//                true
//            }
            R.id.group_msg_menu -> {
                sendGroupMsg()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun sendGroupMsg() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Send A Group Message")
        val view = LayoutInflater.from(this).inflate(R.layout.add_group_msg, null, false)
        builder.setView(view)
        val tempFri: ArrayList<String> = ArrayList()
        view.text_edit_text.setText("Hi! I'm $actUsername")
        view.send_group_to.setOnClickListener {
            val fri = view.fri_edit_text.text.toString()
            if (friendStr.contains(fri)) {
                tempFri.add(fri)
            } else {
                Toast.makeText(this, "No such friend", Toast.LENGTH_LONG).show()
            }
            view.fri_edit_text.text?.clear()
        }
        builder.setPositiveButton("Send") { _, _ ->
            for (fri in tempFri) {
                val text = view.text_edit_text.text.toString()
                val connection = ConnectionClass().dbCon()
                var cs: CallableStatement? = null
                try {
                    cs = connection?.prepareCall("{? = call add_message(?,?,?,?)}")
                    cs!!.registerOutParameter(1, Types.INTEGER)
                    cs.setString(2, text)
                    cs.setString(3, actUsername)
                    cs.setString(4, fri)
                    cs.setString(5, LocalDateTime.now().toString())
                    cs.execute()
                    cs.getInt(1)
                    when (cs.getInt(1)) {
                        2 -> {
                        }
                        1 -> {
                            Log.d("login", "add failed 1")
                        }
                        0 -> {
                            Toast.makeText(this, "Message send", Toast.LENGTH_LONG).show()
                            Log.d("login", "send successful")
                        }
                        else -> {
                        }
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
        builder.create().show()
    }

    fun addFriend(sender: String, text: String) {
        if (check(sender) && check(text)) {
            var cs: CallableStatement? = null
            try {
                cs = connection?.prepareCall("{? = call add_adds(?,?,?)}")
                cs!!.registerOutParameter(1, Types.INTEGER)
                cs.setString(2, actUsername)
                cs.setString(3, sender)
                cs.setString(4, text)
                cs.execute()
                cs.getInt(1)
                when (cs.getInt(1)) {
                    2 -> {
                        Toast.makeText(this, "No such user", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        Toast.makeText(this, "Receiver cannot be null", Toast.LENGTH_LONG).show()
                    }
                    0 -> {
                        Toast.makeText(this, "Request Sent", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "No injection plz", Toast.LENGTH_LONG).show()
        }
    }

    fun showAddDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add a friend")
        val view = LayoutInflater.from(this).inflate(R.layout.add_friend, null, false)
        builder.setView(view)
        view.text_edit_text.setText("Hi! I'm $actUsername")
        builder.setPositiveButton("Send Request") { _, _ ->
            val sender = view.username_edit_text.text.toString()
            val text = view.text_edit_text.text.toString()
            if (sender == "") {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG).show()
            } else {
                addFriend(sender, text)
            }
        }
        builder.create().show()
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

    //msg list
    override fun onMsgSelected(msg: Message) {
        var input = ""
        if (msg.sendTo == actUsername) {
            input = msg.sendBy
        } else if (msg.sendBy == actUsername) {
            input = msg.sendTo
        }
        var fri: Friend? = null
        for (chat in chatsWith) {
            if (chat == input) {
                fri = Friend(chat, "", false, "", (0..7).shuffled().last())
            }
        }
        val chatDetailFragment = MsgDetailFragment(fri!!, actUsername)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, chatDetailFragment)
        ft.addToBackStack("detail")
        ft.commit()
    }

    //splash
    override fun onLoginButtonPressed() {
        isUser = true
        val ft = supportFragmentManager.beginTransaction()
        val switchTo = MsgListFragment(msgs, chatsWith)
        ft.replace(R.id.container, switchTo)
        ft.commit()
    }

    override fun onRegButtonPressed() {
        isUser = true
        val ft = supportFragmentManager.beginTransaction()
        val switchTo = RegisterFragment1(this, connection)
        ft.replace(R.id.container, switchTo)
        ft.commit()
    }

    override fun getUsername(username: String) {
        this.actUsername = username
        isUser = true
    }

    //reg1
    override fun OnProfileButtonPressed1() {
        val ft = supportFragmentManager.beginTransaction()
        val switchTo = RegisterFragment2(this, connection)
        ft.replace(R.id.container, switchTo)
        ft.commit()
    }

    //reg2
    override fun OnProfileButtonPressed2() {
        val ft = supportFragmentManager.beginTransaction()
        val switchTo = MsgListFragment(msgs, chatsWith)
        ft.replace(R.id.container, switchTo)
        ft.commit()
    }

    //contact list
    override fun onFriSelected(friend: Friend) {
        val chatDetailFragment = MsgDetailFragment(friend, actUsername)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, chatDetailFragment)
        ft.addToBackStack("detail")
        ft.commit()
    }

    override fun onFriDeleted(friend: Friend) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure?")
        val view = LayoutInflater.from(this).inflate(R.layout.del_alert, null, false)
        builder.setView(view)
        view.alert_dialog.text = "You are deleting ${friend.username}"
        builder.setPositiveButton("Yes") { _, _ ->
            //            val query = "delete from [friend] where friendruname = '$actUsername' and friendsuname = '${friend.username}'"
//            Log.d("login", query)
//            var stmt: Statement? = null
//            try {
//                stmt = connection?.createStatement()
//                val rs = stmt?.executeQuery(query)
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
        }
        builder.setNegativeButton("No") { _, _ ->
            //nothing happens
        }
        builder.create().show()
//        connection
    }

    override fun onMsgDeleted(msg: Message) {

    }
}

//    fun handleFriReq() {
//        if (isUser) {
//            val query = "SELECT * from [adds] where RequestAccepter = '$actUsername'"
//            Log.d("login", query)
//            var stmt: Statement? = null
//            try {
//                stmt = connection?.createStatement()
//                val rs = stmt?.executeQuery(query)
//                if (rs!!.next()) {
//                    val sender = rs.getString("requestsender")
//                    val text = rs.getString("text")
//                    showFriAlert(sender, text)
//                } else {
//                    Log.d("login", "no friend request")
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//    }

//    fun showFriAlert(candidate: Friend) {
//        val sender = candidate.username
//        val text = candidate.text
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("You've got a friend request")
//        val view = LayoutInflater.from(this).inflate(R.layout.friend_req_alert, null, false)
//        builder.setView(view)
//        view.req_sender.text = "from $sender"
//        view.req_text.text = "'$text'"
//        //add
//        builder.setPositiveButton("Accept Request") { _, _ ->
//            var cs: CallableStatement? = null
//            try {
//                cs = connection?.prepareCall("{? = call add_friend(?,?)}")
//                cs!!.registerOutParameter(1, Types.INTEGER)
//                cs.setString(2, sender)
//                cs.setString(3, actUsername)
//                cs.execute()
//                cs.getInt(1)
//                when (cs.getInt(1)) {
//                    2 -> {
//                        Toast.makeText(this, "No such user", Toast.LENGTH_LONG).show()
//                    }
//                    1 -> {
//                        Log.d("login", "add failed 1")
//                        Toast.makeText(this, "Receiver cannot be null", Toast.LENGTH_LONG).show()
//                    }
//                    0 -> {
//                        Log.d("login", "add failed 1")
//                        Toast.makeText(this, "Friend Added", Toast.LENGTH_LONG).show()
////                        fris.add(candidate)
////                        candidates.remove(candidate)
//                    }
//                    else -> {
//                        Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show()
//                    }
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//
//            var cs1: CallableStatement? = null
//            try {
//                cs1 = connection?.prepareCall("{? = call delete_adds(?,?)}")
//                cs1!!.registerOutParameter(1, Types.INTEGER)
//                cs1.setString(2, sender)
//                cs1.setString(3, actUsername)
//                cs1.execute()
//                cs1.getInt(1)
//                when (cs1.getInt(1)) {
//                    2 -> {
//                        Toast.makeText(this, "No such user", Toast.LENGTH_LONG).show()
//                    }
//                    1 -> {
//                        Log.d("login", "add failed 1")
//                        Toast.makeText(this, "Receiver cannot be null", Toast.LENGTH_LONG).show()
//                    }
//                    0 -> {
//                        Log.d("login", "add failed 1")
//                        Toast.makeText(this, "Friend Added", Toast.LENGTH_LONG).show()
//                    }
//                    else -> {
//                        Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show()
//                    }
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//        //delete
//        builder.setNegativeButton("Deny Request") { _, _ ->
//            var cs2: CallableStatement? = null
//            try {
//                cs2 = connection?.prepareCall("{? = call delete_adds(?,?)}")
//                cs2!!.registerOutParameter(1, Types.INTEGER)
//                cs2.setString(2, sender)
//                cs2.setString(3, actUsername)
//                cs2.execute()
//                cs2.getInt(1)
//                when (cs2.getInt(1)) {
//                    2 -> {
//                        Toast.makeText(this, "No such user", Toast.LENGTH_LONG).show()
//                    }
//                    1 -> {
//                        Log.d("login", "add failed 1")
//                        Toast.makeText(this, "Receiver cannot be null", Toast.LENGTH_LONG).show()
//                    }
//                    0 -> {
//                        Log.d("login", "add failed 1")
//                        Toast.makeText(this, "Friend Request Denied", Toast.LENGTH_LONG).show()
////                        candidates.remove(candidate)
//                    }
//                    else -> {
//                        Toast.makeText(this, "Fail", Toast.LENGTH_LONG).show()
//                    }
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//        builder.create().show()
//    }
//    fun getFri() {
//        if (isUser) {
//            connection = ConnectionClass().dbCon()
//            val query = "SELECT * from [friend] where friendruname = '$actUsername' " +
//                    "or friendsuname = '$actUsername'"
//            Log.d("login", query)
//            var stmt: Statement? = null
//            try {
//                stmt = connection?.createStatement()
//                val rs = stmt?.executeQuery(query)
//                while (rs!!.next()) {
//                    val su = rs.getString("friendsuname")
//                    val ru = rs.getString("friendruname")
//                    if (su == actUsername && !fris.contains(ru)) {
//                        fris.add(ru)
//                    } else if (ru == actUsername && !fris.contains(su)) {
//                        fris.add()
//                    }
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//    }