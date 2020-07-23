package edu.rosehulman.wegroup0


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_splash.view.*
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.add_post.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.day_edit_text
import kotlinx.android.synthetic.main.fragment_profile.view.month_edit_text
import kotlinx.android.synthetic.main.fragment_profile.view.nickname_edit_text
import kotlinx.android.synthetic.main.fragment_profile.view.region_edit_text
import kotlinx.android.synthetic.main.fragment_profile.view.year_edit_text
import kotlinx.android.synthetic.main.fragment_region_descr.view.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.sql.*
import java.text.ParseException
import java.time.format.DateTimeFormatter


class ProfileFragment() : Fragment() {

    private val loginSalt = LoginSalt()
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
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        getInfo(view)
        val genders = resources.getStringArray(R.array.gender)
        view.prof_gender.onItemSelectedListener
        val adapter = ArrayAdapter(
            context!!,
            android.R.layout.simple_spinner_item, genders
        )
        view.prof_gender.adapter = adapter as SpinnerAdapter?
        var gender = ""
        view.prof_gender.onItemSelectedListener = object :
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
        view.profile_username.text = (activity as MainActivity).actUsername
        view.nickname_et.setText(tmpNickname)
        view.email_et.setText(tmpEmail)
        view.des_et.setText(tmpDes)
        view.region_et.setText(tmpReg)

        view.sc_btn.setOnClickListener {
            var nickname = view.nickname_et.text.toString()
            var region = view.region_et.text.toString()
            var email = view.email_et.text.toString()
            var des = view.des_et.text.toString()
            var year = view.year_edit_text.text.toString()
            var month = view.month_edit_text.text.toString()
            var day = view.day_edit_text.text.toString()
            if (day == "" || month == "" || year == "") {
            } else {
                if (Integer.parseInt(month) < 10) {
                    month = "0$month"
                }
                if (Integer.parseInt(day) < 10) {
                    day = "0$day"
                }
            }
            var dob = "$year-$month-$day"
            Log.d("login9", dob)
            if (year == "" || month == "" || day == "") {
                Toast.makeText(context, "Please enter a valid date", Toast.LENGTH_LONG).show()
            } else if (Integer.parseInt(year) > 2020 || Integer.parseInt(year) < 1900
                || Integer.parseInt(month) > 12 || Integer.parseInt(month) < 1
                || Integer.parseInt(day) > 31 || Integer.parseInt(day) < 1
            ) {
                Toast.makeText(context, "Please enter a valid date", Toast.LENGTH_LONG).show()
            }
            if (gender == "You would describe your gender as") {
                gender = "secret"
            }
            if (des == "") {
                des = tmpDes
            }
            if (region == "") {
                region = tmpReg
            }
            if (email == "") {
                email = tmpEmail
            }
            if (nickname == "") {
                nickname = tmpNickname
            }
            if (dob == "--") {
                dob = tmpDdate
                update(nickname, region, dob, email, des, gender)
                getInfo(view)
            } else {
                val fdob = valiDate(dob)
                update(nickname, region, fdob, email, des, gender)
                getInfo(view)
            }
        }
        return view
    }

    @SuppressLint("SimpleDateFormat")
    fun valiDate(dateStr: String): String {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = formatter.parse(dateStr)
            val fdate = formatter.format(date)
            return fdate
        } catch (e: ParseException) {
        }
        return ""
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
                if (nickname != null) {
                    view.nickname_et.setText(nickname)
                    tmpNickname = nickname
                }
                val email = rs.getString("email")
                if (email != null) {
                    view.email_et.setText(email)
                    tmpEmail = email
                }
                val bdate = rs.getString("birthday")
                tmpDdate = bdate
                val des = rs.getString("description")
                if (des != null) {
                    view.des_et.setText(des)
                    tmpDes = des
                }
                val region = rs.getString("region")
                if (region != null) {
                    view.nickname_et.setText(region)
                    tmpReg = region
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun update(nickname: String, region: String, dob: String, email: String, des: String, gender: String) {
        val connection = ConnectionClass().dbCon()
        var cs: CallableStatement? = null
        try {
            cs = connection?.prepareCall("{? = call update_profile(?,?,?,?,?,?,?)}")
            cs!!.registerOutParameter(1, Types.INTEGER)
            cs.setString(2, (activity as MainActivity).actUsername)
            cs.setString(3, region)
            cs.setString(4, dob)
            cs.setString(5, gender)
            cs.setString(6, des)
            cs.setString(7, email)
            cs.setString(8, nickname)
            cs.execute()
            cs.getInt(1)
            when (cs.getInt(1)) {
                0 -> {
                    Toast.makeText(activity, "Changes made", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(activity, "Fail", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        activity!!.nav_view.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        activity!!.nav_view.visibility = View.INVISIBLE
    }
}

