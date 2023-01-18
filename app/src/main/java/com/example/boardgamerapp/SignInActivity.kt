package com.example.boardgamerapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlin.system.exitProcess

class SignInActivity : AppCompatActivity() {
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val dbhelper=DB_class(applicationContext)
        val db=dbhelper.readableDatabase
        val etUserName = findViewById<EditText>(R.id.etUserName)
        val etUserPasswd = findViewById<EditText>(R.id.etUserPasswd)
        val btnLogin = findViewById<Button>(R.id.btnAddProposal)

       btnLogin.setOnClickListener {
           val username = etUserName.text.toString()
           val password = etUserPasswd.text.toString()
           val query = "SELECT * FROM TBL_USER WHERE USERNAME = '"+username+"' AND PWD = '"+password+"' "
           val rs = db.rawQuery(query,null)
           if(rs.moveToNext()) {
               val userId=rs.getString(rs.getColumnIndex("USER_ID"))
               rs.close()
               startActivity(Intent(this, MainActivity::class.java).putExtra("USER_ID", userId))
           }
           else {
               val ad = AlertDialog.Builder(this)
               ad.setTitle("Information")
               ad.setMessage("Benutzername oder Passwort ist falsch!")
               ad.setPositiveButton("Ok", null)
               ad.show()
           }
        }
    }
    override fun onBackPressed() {
        moveTaskToBack(true);
        exitProcess(-1)
    }
}