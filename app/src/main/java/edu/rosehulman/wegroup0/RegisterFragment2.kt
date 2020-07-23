package edu.rosehulman.wegroup0

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.android.synthetic.main.fragment_splash.view.*
import java.sql.*
import android.os.AsyncTask.execute
import android.provider.ContactsContract
import android.view.*
import android.widget.SpinnerAdapter
import kotlinx.android.synthetic.main.fragment_msg_detail.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_region_descr.view.*
import kotlinx.android.synthetic.main.fragment_region_descr.view.des_edit_text
import kotlinx.android.synthetic.main.fragment_region_descr.view.region_edit_text
import kotlinx.android.synthetic.main.fragment_register.view.continue_button


//import androidx.test.orchestrator.junit.BundleJUnitUtils.getResult


class RegisterFragment2(var contex: Context, con: Connection?) : Fragment() {

    var connection = con
    var listener: RegisterFragment2.OnProfileButtonPressedListener2? = null
    var tmpDdate: String = ""
    var tmpReg: String = ""
    var tmpDes: String = ""
    var tmpEmail: String = ""
    var tmpNickname: String = ""

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
        val view = inflater.inflate(R.layout.fragment_region_descr, container, false)
        view.continue_button.setOnClickListener {
            newProfile(
                (activity as MainActivity).actUsername, view.region_edit_text.text.toString(),
                view.des_edit_text.text.toString()
            )
        }
        return view
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

    override fun onStart() {
        super.onStart()
        activity!!.nav_view.visibility = View.INVISIBLE
    }

    override fun onStop() {
        super.onStop()
        activity!!.nav_view.visibility = View.VISIBLE
    }

    fun getInfo(view: View) {
        val connection = ConnectionClass().dbCon()
        val query = "Select * From User_Detail Where Username = '${(activity as MainActivity).actUsername}'"
        var stmt: Statement? = null
        try {
            stmt = connection?.createStatement()
            val rs = stmt?.executeQuery(query)
            while (rs!!.next()) {
                val nickname = rs.getString("nickname")
                Log.d("login10", nickname)
                view.nickname_et.setText(nickname)
                tmpNickname = nickname
                val email = rs.getString("email")
                view.email_et.setText(email)
                Log.d("login10", email)
                tmpEmail = email
                val bdate = rs.getString("birthday")
                Log.d("login10", bdate)
                tmpDdate = bdate
                val des = rs.getString("description")
                view.des_et.setText(des)
                Log.d("login10", des)
                tmpDes = des
                val region = rs.getString("region")
                view.nickname_et.setText(region)
                Log.d("login10", region)
                tmpReg = region
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun newProfile(username: String, region: String, des: String) {
        if (check(username) && check(region) && check(des)) {
            val con = ConnectionClass().dbCon()

            val connection = ConnectionClass().dbCon()
            var cs: CallableStatement? = null
            try {
                cs = connection?.prepareCall("{? = call update_reg_des(?,?,?)}")
                cs!!.registerOutParameter(1, Types.INTEGER)
                cs.setString(2, (activity as MainActivity).actUsername)
                cs.setString(3, region)
                cs.setString(4, des)
                cs.execute()
                cs.getInt(1)
                when (cs.getInt(1)) {
                    0 -> {
                        Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(activity, "Fail", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            listener?.OnProfileButtonPressed2()


//            var stmt: Statement? = null
//            when {
//                des == "" -> {
////                    val query1 = "exec update_Profile @username = '$username', @region = '$region'"
////                    Log.d("login", query1)
////                    stmt = con?.createStatement()
////                    val result1 = stmt?.execute(query1)
//
//                    val connection = ConnectionClass().dbCon()
//                    var cs: CallableStatement? = null
//                    try {
//                        cs = connection?.prepareCall("{? = call update_profile(?,?,?,?,?,?,?)}")
//                        cs!!.registerOutParameter(1, Types.INTEGER)
//                        cs.setString(2, (activity as MainActivity).actUsername)
//                        cs.setString(3, region)
//                        cs.setString(6, des)
//                        cs.execute()
//                        cs.getInt(1)
//                        when (cs.getInt(1)) {
//                            0 -> {
//                                Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
//                            }
//                            else -> {
//                                Toast.makeText(activity, "Fail", Toast.LENGTH_LONG).show()
//                            }
//                        }
//                    } catch (e: SQLException) {
//                        e.printStackTrace()
//                    }
//
//
//                    listener?.OnProfileButtonPressed2()
//                    Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
//
//                }
//                region == "" -> {
//                    val query1 = "exec update_Profile @username = '$username', @description = '$des'"
//                    Log.d("login", query1)
//                    stmt = con?.createStatement()
//                    val result1 = stmt?.execute(query1)
//                    listener?.OnProfileButtonPressed2()
//                    Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
//
//                }
//                else -> {
//                    val query1 =
//                        "exec update_Profile @username = '$username', @region = '$region', @description = '$des'"
//                    Log.d("login", query1)
//                    stmt = con?.createStatement()
//                    val result1 = stmt?.execute(query1)
//                    listener?.OnProfileButtonPressed2()
//                    Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(context, "No injection plz", Toast.LENGTH_LONG).show()
//        }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RegisterFragment2.OnProfileButtonPressedListener2) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnProfileButtonPressedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnProfileButtonPressedListener2 {
        fun OnProfileButtonPressed2()
    }

//        try {
//            cs = con?.prepareCall("{? = call Add_User(?,?,?)}")
//            cs!!.registerOutParameter(1, Types.INTEGER)
//            cs.setString(2, "ABCCCCC")
//            cs.setString(4, "59UfrtcJVCrdtyMCAZjkvQ==")
//            cs.setString(3, "Ph+cjSPOmu9fhiuffhnJaQ==")
//            cs.execute()
//            cs.getInt(1)
//            Log.d("login", "Adding 1")
//
//            when (cs.getInt(1)) {
//                1 -> {
//                    Log.d("login", "Add User Successful")
//                    Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
//                    listener?.onRegButtonPressed()
//                }
//                0 -> {
//                    Log.d("login", "123")
//                    Toast.makeText(context, "Please choose another username", Toast.LENGTH_LONG).show()
//                }
//                else -> {
//                    Log.d("login", "321")
//                    Toast.makeText(context, "Please choose another username", Toast.LENGTH_LONG).show()
//                }
//            }
//            Log.d("login", "Adding 2")
//            callBackValue?.getUsername(username)
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }


//            cs = connection?.prepareCall("{? = call add_profile(?,?,?)}")
//            cs!!.registerOutParameter(1, Types.INTEGER)
//            cs.setString(2, bdate)
//            cs.setString(3, gender)
//            cs.setString(4, username)
//            cs.execute()
//            cs.getInt(1)
//            when (cs.getInt(1)) {
//                0 -> {
//                    Log.d("login", "Add Profile Successful")
//                    Toast.makeText(context, "Welcome to WeGroup", Toast.LENGTH_LONG).show()
//                    listener?.onRegButtonPressed()
//                }
//                1 -> {
//                    Log.d("login", "123")
//                    Toast.makeText(context, "Please choose another profile", Toast.LENGTH_LONG).show()
//                }
//                else -> {
//                    Log.d("login", "321")
//                    Toast.makeText(context, "Please choose another profile", Toast.LENGTH_LONG).show()
//                }
//            }
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }

}