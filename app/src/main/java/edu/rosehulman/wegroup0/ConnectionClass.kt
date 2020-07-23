package edu.rosehulman.wegroup0

import android.content.Context
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import android.system.Os.uname
import java.sql.Statement


class ConnectionClass {
    private val serverName = "golem.csse.rose-hulman.edu"
    private val user = "liua1"
    private val pass = "Ao990818"
    private val databaseName = "WeGroupTest3"
    private var connection: Connection? = null


    fun dbCon(): Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var con: Connection? = null
        var conStr: String? = null
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            conStr = "jdbc:jtds:sqlserver://$serverName;databaseName=$databaseName;user=$user;password=$pass;"
            con = DriverManager.getConnection(conStr)
            if (con != null) {
                Log.d("login", "successful")
            }
        } catch (e: SQLException) {
            Log.e("Error1", e.message)
        } catch (exl: ClassNotFoundException) {
            Log.e("Error2", exl.message)
        } catch (e1: Exception) {
            Log.e("Error3", e1.message)
        }
        connection = con
//        test()
        return con
    }

    fun test() {
        val q1 = "select password from [user]"
        var stmt: Statement? = null
        println(q1)

        try {
            stmt = connection?.createStatement()
            val rs = stmt!!.executeQuery(q1)
            while (rs.next()) {
                Log.d("login", rs.getInt("password").toString())
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }
}