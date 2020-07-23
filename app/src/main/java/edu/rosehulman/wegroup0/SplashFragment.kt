package edu.rosehulman.wegroup0


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_splash.view.*
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import java.sql.*


class SplashFragment(context: Context, con: Connection?) : Fragment() {

    var connection = con
    private val loginSalt = LoginSalt()
    var listener: OnLoginButtonPressedListener? = null
    var callBackValue: OnLoginButtonPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.add_friend_menu).isVisible = false
        menu.findItem(R.id.add_post_menu).isVisible = false
        menu.findItem(R.id.show_other_menu).isVisible = false
        menu.findItem(R.id.show_mine_menu).isVisible = false
        menu.findItem(R.id.group_msg_menu).isVisible = false
        menu.findItem(R.id.log_out).isVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        view.login_button.setOnClickListener {
            login(view.username_edit_text.text.toString(), view.password_edit_text.text.toString())
        }
        view.register_button.setOnClickListener {
            val username = view.username_edit_text.text.toString()
            val password = view.password_edit_text.text.toString()
            if (username.length > 20) {
                Toast.makeText(context, "Username cannot be longer than 20", Toast.LENGTH_LONG).show()
            }
            if (password.length > 20) {
                Toast.makeText(context, "Password cannot be longer than 20", Toast.LENGTH_LONG).show()
            }
            if (username.length <= 20 && password.length <= 20){
                register(username, password)
                callBackValue?.getUsername(view.username_edit_text.text.toString())
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        activity!!.nav_view.visibility = View.INVISIBLE
        activity!!
    }

    override fun onStop() {
        super.onStop()
        activity!!.nav_view.visibility = View.VISIBLE
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

    private fun register(username: String, password: String) {
        if (check(username) && check(password)) {
            if (username == "" || password == "") {
                Toast.makeText(context, "Please enter a username and a password", Toast.LENGTH_LONG).show()
            } else {
                val ps = loginSalt.getNewSalt()
                val salt = loginSalt.getStringFromBytes(ps)
                val hash: String = loginSalt.hashPassword(ps, password)
                var cs: CallableStatement? = null
                try {
                    cs = connection?.prepareCall("{? = call Add_User(?,?,?)}")
                    cs!!.registerOutParameter(1, Types.INTEGER)
                    cs.setString(2, username)
                    cs.setString(4, salt)
                    cs.setString(3, hash)
                    cs.execute()
                    cs.getInt(1)
                    when (cs.getInt(1)) {
                        2 -> {
                            Log.d("login", "reg failed 2")
                            Toast.makeText(
                                context,
                                "This username has been taken; please choose another one",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        1 -> {
                            Log.d("login", "Add User Successful")
                            Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
                            listener?.onRegButtonPressed()
                        }
                        0 -> {
                            Toast.makeText(context, "Please enter a username and a password", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(context, "Please enter a username and a password", Toast.LENGTH_LONG).show()
                        }
                    }
                    callBackValue?.getUsername(username)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(context, "No injection plz", Toast.LENGTH_LONG).show()
        }
    }

    private fun login(username: String, password: String) {
        if (check(username) && check(password)) {
            if (username == "" || password == "") {
                Toast.makeText(context, "Please enter a username and a password", Toast.LENGTH_LONG).show()
            } else {
                var salt: String? = null
                var hash: String? = null
                val query = "Select * From fn_getLoginInfo('$username')"
                Log.d("login", query)
                var stmt: Statement? = null
                try {
                    stmt = connection?.createStatement()
                    val rs = stmt?.executeQuery(query)
                    if (rs!!.next()) {
                        salt = rs.getString("passwordsalt")
                        hash = rs.getString("passwordhash")
                        val realSalt = loginSalt.getBytesFromString(salt)
                        val pw = loginSalt.hashPassword(realSalt, password)
                        if (pw == hash) {
                            Toast.makeText(context, "Welcome Back", Toast.LENGTH_LONG).show()
                            //login fragment
                            listener?.onLoginButtonPressed()
                            callBackValue?.getUsername(username)
                        } else {
                            Toast.makeText(context, "Your username and password don't match", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "No such user", Toast.LENGTH_LONG).show()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(context, "No injection plz", Toast.LENGTH_LONG).show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBackValue = activity as OnLoginButtonPressedListener?
        if (context is OnLoginButtonPressedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnLoginButtonPressedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnLoginButtonPressedListener {
        fun onLoginButtonPressed()
        fun onRegButtonPressed()
        fun getUsername(username: String)
    }
}

