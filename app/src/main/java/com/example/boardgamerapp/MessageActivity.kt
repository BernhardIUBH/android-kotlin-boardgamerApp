package com.example.boardgamerapp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MessageActivity : AppCompatActivity() {
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val dbhelper = DB_class(applicationContext)
        val db = dbhelper.readableDatabase
        val userId = intent.getStringExtra("USER_ID")
        val group = intent.getStringExtra("GROUP")
        val etMsg = findViewById<TextView>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSendMessage)
        val mapUserIdTel = mutableMapOf<Int, String>()

        //Text - Vorlage
        etMsg.text = "Hi ich werde mich heute leider etwas verspäten!"

        //Erstelle Map mit User_IDs und TelNr der anderen Gruppenteilnehmer
        val query = "SELECT * FROM TBL_USER WHERE GRP= '$group'"
        val rs = db.rawQuery(query, null)
        val amountUsers = rs.count
        rs.moveToNext()
        for (x in 1..amountUsers) {
            val usrId = rs.getInt(rs.getColumnIndex("USER_ID"))
            val telnr = rs.getString(rs.getColumnIndex("TELNR"))
            mapUserIdTel.put(usrId, telnr)
            rs.moveToNext()
        }
        mapUserIdTel.remove(userId!!.toInt())
        rs.close()

        //Senden
        val text = etMsg.text.toString()
        btnSend.setOnClickListener{
            for ((k, v) in mapUserIdTel) {
                sendMessage(this@MessageActivity, mapUserIdTel.get(key = k).toString(), text)
            }
        }
    }

    fun sendMessage(context: Context, telNr: String, msgText: String) {
        //Nur dieses Format erlaubt = "+XX XXXXXXXXXX"
        val url = "https://api.whatsapp.com/send?phone=${telNr}&text=${msgText}"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            this.data = Uri.parse(url)
            this.`package` = "com.whatsapp"
        }
        try {
            context.startActivity(intent)
        } catch (ex : ActivityNotFoundException){
            //whatsapp nicht Installiert
            Toast.makeText(
                this@MessageActivity,
                "WhatsApp auf diesem Gerät nicht installiert",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}