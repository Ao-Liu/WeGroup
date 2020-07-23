package edu.rosehulman.wegroup0

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.sql.*
import android.widget.SpinnerAdapter
import java.text.ParseException
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


//import androidx.test.orchestrator.junit.BundleJUnitUtils.getResult


class RegisterFragment1(var contex: Context, con: Connection?) : Fragment() {

    var connection = con
    var listener: RegisterFragment1.OnProfileButtonPressedListener1? = null

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
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        val genders = resources.getStringArray(R.array.gender)
        view.reg_gender.onItemSelectedListener
        val adapter = ArrayAdapter(
            contex,
            android.R.layout.simple_spinner_item, genders
        )
        view.reg_gender.adapter = adapter as SpinnerAdapter?
        var gender = ""
        view.reg_gender.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                gender = genders[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }

        }
        view.continue_button.setOnClickListener {

            val year = view.year_edit_text.text.toString()
            var month = view.month_edit_text.text.toString()
            var day = view.day_edit_text.text.toString()
            if (year.contains("??")){
                Toast.makeText(context, "No emoji", Toast.LENGTH_LONG).show()
            }
            if (year == "" || day == "" || month == "") {
                Toast.makeText(context, "Please enter a birthday date", Toast.LENGTH_LONG).show()
            } else {
                if (Integer.parseInt(month) < 10) {
                    month = "0$month"
                }
                if (Integer.parseInt(day) < 10) {
                    day = "0$day"
                }
                val dob = "$year-$month-$day"
                if (year == "" || month == "" || day == "") {
                    Toast.makeText(context, "Please enter a valid date", Toast.LENGTH_LONG).show()
                } else if (Integer.parseInt(year) > 2020 || Integer.parseInt(year) < 1900
                    || Integer.parseInt(month) > 12 || Integer.parseInt(month) < 1
                    || Integer.parseInt(day) > 31 || Integer.parseInt(day) < 1
                ) {
                    Toast.makeText(context, "Please enter a valid date", Toast.LENGTH_LONG).show()
                } else if (view.nickname_edit_text.text.toString() == "") {
                    Toast.makeText(context, "You need a nickname to proceed", Toast.LENGTH_LONG).show()
                } else {
                    val email = view.email_edit_text.text.toString()
                    val p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
                    val m = p.matcher("I am a string")
                    val b = m.find()
                    if (b) {
                        Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_LONG).show()
                    } else {
                        val nickname = view.nickname_edit_text.text.toString()
                        newProfile((activity as MainActivity).actUsername, gender, dob, email, nickname)
                    }
                }
            }
        }

        return view
    }


    override fun onStart() {
        super.onStart()
        activity!!.nav_view.visibility = View.INVISIBLE
    }

    override fun onStop() {
        super.onStop()
        activity!!.nav_view.visibility = View.VISIBLE
    }

    @SuppressLint("SimpleDateFormat")
    private fun newProfile(username: String, gender: String, bdate: String, email: String, nickname: String) {
        if (check(username) && check(gender) && check(bdate) && check(email) && check(nickname)) {
            val connection = ConnectionClass().dbCon()
            var nn = nickname
            val bdate = valiDate(bdate)
            val con = ConnectionClass().dbCon()
            var stmt: Statement? = null
            var emailFormat1 = false
            var emailFormat2 = false
            for (char in email) {
                if (char == '@') {
                    emailFormat1 = true
                } else if (char == '.') {
                    emailFormat2 = true
                }
            }
            if (emailFormat1 && emailFormat2 || email == "") {
                var finalGender = ""
                finalGender = if (gender == "You would describe your gender as") {
                    "Secret"
                } else {
                    gender
                }

                var cs: CallableStatement? = null
                try {
                    cs = connection?.prepareCall("{? = call add_profile(?,?,?,?,?)}")
                    cs!!.registerOutParameter(1, Types.INTEGER)
                    cs.setString(2, "")
                    cs.setString(3, bdate)
                    cs.setString(4, finalGender)
                    cs.setString(5, "")
                    cs.setString(6, username)
                    cs.execute()
                    cs.getInt(1)
                    when (cs.getInt(1)) {
                        0 -> {
                            Toast.makeText(context, "Profile added", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(context, "Fail 1", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }

                try {
                    cs = connection?.prepareCall("{? = call update_User_Info(?,?,?)}")
                    cs!!.registerOutParameter(1, Types.INTEGER)
                    cs.setString(2, username)
                    cs.setString(3, nn)
                    cs.setString(4, email)
                    cs.execute()
                    cs.getInt(1)
                    when (cs.getInt(1)) {
                        0 -> {
                            Toast.makeText(context, "Profile added", Toast.LENGTH_LONG).show()
//                            check(email)
                            listener?.OnProfileButtonPressed1()
                        }
                        2 -> {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(context, "Fail 2", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
//                }

            } else {
                Toast.makeText(context, "Please a valid email", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "No injection plz", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun valiDate(dateStr: String): String {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = formatter.parse(dateStr)
            val fdate = formatter.format(date)
            Log.d("login4", fdate)
            return fdate
        } catch (e: ParseException) {
            Log.d("login4", "fail")
        }
        return ""
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RegisterFragment1.OnProfileButtonPressedListener1) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnProfileButtonPressedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnProfileButtonPressedListener1 {
        fun OnProfileButtonPressed1()
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

    //        var cs: CallableStatement? = null
//        try {
//            cs = connection?.prepareCall("{? = add_profile(?,?,?,?,?)}")
//            cs!!.registerOutParameter(1, Types.INTEGER)
//            cs.setString(2, "")
//            cs.setString(3, bdate)
//            cs.setString(4, finalGender)
//            cs.setString(5, "")
//            cs.setString(6, username)
//            cs.execute()
//            cs.getInt(1)
//            Log.d("login",cs.toString())
//            when (cs.getInt(1)) {
//                2 -> {
//                    Toast.makeText(context, "Invalid email", Toast.LENGTH_LONG).show()
//                }
//                1 -> {
//                    Log.d("login", "add failed 1")
//                    Toast.makeText(context, "User not null", Toast.LENGTH_LONG).show()
//                }
//                0 -> {
//                    Log.d("login", "add failed 1")
//                    Toast.makeText(context, "Request Sent", Toast.LENGTH_LONG).show()
//                }
//                else -> {
//                    Log.d("login", "321")
//                    Toast.makeText(context, "Fail for no reason", Toast.LENGTH_LONG).show()
//                }
//            }
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
//
//        try {
//            cs = connection?.prepareCall("{? = update_User_Info(?,?,?)}")
//            cs!!.registerOutParameter(1, Types.INTEGER)
//            cs.setString(2, username)
//            cs.setString(3, nickname)
//            cs.setString(4, email)
//            cs.execute()
//            cs.getInt(1)
//            when (cs.getInt(1)) {
//                2 -> {
//                    Toast.makeText(context, "Invalid email", Toast.LENGTH_LONG).show()
//                }
//                1 -> {
//                    Log.d("login", "add failed 1")
//                    Toast.makeText(context, "User not null", Toast.LENGTH_LONG).show()
//                }
//                0 -> {
//                    Log.d("login", "add failed 1")
//                    Toast.makeText(context, "Request Sent", Toast.LENGTH_LONG).show()
//                }
//                else -> {
//                    Log.d("login", "321")
//                    Toast.makeText(context, "Fail for no reason", Toast.LENGTH_LONG).show()
//                }
//            }
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
}