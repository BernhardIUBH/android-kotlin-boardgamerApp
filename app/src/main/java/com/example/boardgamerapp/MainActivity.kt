package com.example.boardgamerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbhelper = DB_class(applicationContext)
        val db = dbhelper.readableDatabase
        val userId = intent.getStringExtra("USER_ID")
        val tvUserName = findViewById<TextView>(R.id.tvHostAcHost)
        val tvGroupName = findViewById<TextView>(R.id.tvHostAcDate)
        var mapUserId = mutableMapOf<Int, String>()
        val formatDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        //var dateToday = LocalDate.now()
        var dateToday = LocalDate.parse("2023-01-09")
        val tvDateToday = findViewById<TextView>(R.id.tvDateToday)
        val tvDateNextGame = findViewById<TextView>(R.id.tvDateNextGame)
        var tvHost = findViewById<TextView>(R.id.tvHost)
        var tvGameName = findViewById<TextView>(R.id.tvGameName)
        val btnVoteGame = findViewById<Button>(R.id.btnVoteGame)
        val btnRatingHost = findViewById<Button>(R.id.btnRatingHost)
        val btnChatroom = findViewById<Button>(R.id.btnMessage)
        val btnLogOut = findViewById<Button>(R.id.btnAddProposal)
        var username = ""
        var group = ""
        var amountUsers = 0
        var host = 0
        var grpGameId = 0

        //Container: User und Gruppe
        var query = "SELECT * FROM TBL_USER WHERE USER_ID = '" + userId + "' "
        var rs = db.rawQuery(query, null)
        if (rs.moveToNext()) {
            username = rs.getString(rs.getColumnIndex("USERNAME"))
            group = rs.getString(rs.getColumnIndex("GRP"))
            tvUserName.text = username
            tvGroupName.text = group
        }
        rs.close()
        //Erstelle Map mit User_ID und User
        query = "SELECT * FROM TBL_USER WHERE GRP= '$group'"
        rs = db.rawQuery(query, null)
        rs.moveToFirst()
        amountUsers = rs.count
        for (x in 1..amountUsers) {
            var usrId = rs.getInt(rs.getColumnIndex("USER_ID"))
            var usrname = rs.getString(rs.getColumnIndex("USERNAME"))
            mapUserId.put(usrId, usrname)
            rs.moveToNext()
        }
        rs.close()

        //Container: Datum Heute
        tvDateToday.text = dateToday.format(formatDate)

        //Container: Nächster Spieltermin + Pruefung ob Termin vorhanden ist
        query = "SELECT * FROM TBL_GAME WHERE DATE >= '" + dateToday.toString() + "' AND GRP== '$group'"
        rs = db.rawQuery(query, null)
        if (!rs.moveToNext()) {                          //Wenn keine nöchste Spielrunde vorhanden ist --> createGame(..)
            createGame(db, dateToday, amountUsers, group)
        }
        rs.close()
        rs = db.rawQuery(query, null)
        if (rs.moveToLast()) {
            grpGameId = rs.getInt(rs.getColumnIndex("GRP_GAME_ID"))
            val date = rs.getString(rs.getColumnIndex("DATE"))
            host = rs.getInt(rs.getColumnIndex("HOST"))
            tvDateNextGame.text = LocalDate.parse(date).format(formatDate)
        }
        rs.close()

        //Container: Gastgeber
        tvHost.text = mapUserId[host]

        //Container: Spiel
        var tabellenname = "TBL_${group.uppercase()}_GAME".plus(grpGameId.toString())
        var fstHighestValue = 0
        var scdHighestValue = 0
        query = "SELECT GAME_NAME, RATING FROM $tabellenname ORDER BY RATING DESC"
        rs = db.rawQuery(query, null)
        if(rs.moveToNext()){
            fstHighestValue = rs.getString(rs.getColumnIndex("RATING")).toInt()
            rs.moveToNext()
            scdHighestValue = rs.getString(rs.getColumnIndex("RATING")).toInt()
        }
        rs.close()
        query = "SELECT GAME_NAME, MAX(RATING) FROM $tabellenname "
        rs = db.rawQuery(query, null)
        if (fstHighestValue!=scdHighestValue) {
            rs.moveToNext()
            tvGameName.text = rs.getString(rs.getColumnIndex("GAME_NAME"))
        }
        else {
            tvGameName.text = ""
        }

        rs.close()

        btnVoteGame.setOnClickListener {
            startActivity(
                Intent(this, ProposalAndVoteActivity::class.java).putExtra("USER_ID", userId).putExtra("GROUP", group).putExtra("GAME_ID", grpGameId.toString())
            )
        }

        //Container: Bewertung Gastgeber
               btnRatingHost.setOnClickListener {
                   if(grpGameId!=1) {
                       var prevHost = host - 1
                       if (prevHost == 0) {
                           prevHost = amountUsers
                       }
                       val prevHostName = mapUserId[prevHost]
                       startActivity(
                           Intent(this, RatingHostActivity::class.java).putExtra("USER_ID", userId)
                               .putExtra("GROUP", group).putExtra("GRP_GAME_ID", grpGameId.toString())
                               .putExtra("PREVHOST", prevHost.toString()).putExtra("PREVHOSTNAME", prevHostName)
                       )
                   }
                   else {
                       Toast.makeText(
                           this@MainActivity,
                           "Keine Letzte Spielrunde vorhanden", Toast.LENGTH_SHORT
                       ).show()
                   }
               }

        //Container: WhatsApp-Nachricht absenden
        btnChatroom.setOnClickListener {
            startActivity(
                Intent(this, MessageActivity::class.java).putExtra("USER_ID", userId).putExtra("GROUP", group)
            )
        }

        //Logout
        btnLogOut.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
    override fun onBackPressed() {
        applicationContext.cacheDir.deleteRecursively()
        startActivity(Intent(this, SignInActivity::class.java))
    }
}

@SuppressLint("Range")
fun createGame(db: SQLiteDatabase, dateToday: LocalDate, amountUsers: Int, group: String) {
    //val grpNr = group.filter { it.isDigit() }
    var grpGameId = 0
    var date = dateToday.toString()
    var lastHost = 0
    var newDate = LocalDate.parse(date).plusDays(7)
    var newHost = 1
    var query = "SELECT * FROM TBL_GAME WHERE DATE < '" + dateToday.toString() + "' AND GRP= '$group'"
    var rs = db.rawQuery(query, null)
    //Ueberprüfung ob bereits eine Spielrunde erstellt wurde
    if (rs.moveToLast()) {
        grpGameId = rs.getInt(rs.getColumnIndex("GRP_GAME_ID"))
        date = rs.getString(rs.getColumnIndex("DATE"))
        lastHost = rs.getInt(rs.getColumnIndex("HOST"))
        newDate = LocalDate.parse(date).plusDays(7)
        //Bestimme neuen Host fuer neue Spielrunde
        newHost = lastHost + 1
        if (newHost == amountUsers + 1) {
            newHost = 1
        }
    }
    db?.execSQL("INSERT INTO TBL_GAME(GRP, GRP_GAME_ID, DATE, HOST) VALUES ('$group', ${grpGameId+1}, '$newDate', '$newHost')")
    rs.close()
    db?.execSQL("INSERT INTO TBL_RATING_HOST(GRP_GAME_ID) VALUES ($grpGameId+1)")

    //Erstelle neue Tabelle für neue Spielrunde - TBL_GROUP*_GAME*
    var tabellenname = "TBL_${group.uppercase()}_GAME${grpGameId + 1}"
    db?.execSQL("CREATE TABLE $tabellenname(PROP_ID INTEGER PRIMARY KEY AUTOINCREMENT, GAME_NAME STRING, RATING INTEGER DEFAULT 0)")
    //Erzeuge Brettspiel - Vorschläge:
    db?.execSQL("INSERT INTO $tabellenname(GAME_NAME) VALUES ('Uno')")
    db?.execSQL("INSERT INTO $tabellenname(GAME_NAME) VALUES ('Scrabble')")
    db?.execSQL("INSERT INTO $tabellenname(GAME_NAME) VALUES ('Risiko')")
    db?.execSQL("INSERT INTO $tabellenname(GAME_NAME) VALUES ('Monopoly')")

    //Erstelle neue Tabelle für neue Spielrunde - TBL_GROUP*_GAME*_RATING_COMPLETED
    tabellenname = "TBL_${group.uppercase()}_GAME${grpGameId + 1}_RATING_COMPLETED"
    db?.execSQL("CREATE TABLE $tabellenname(USER_ID INTEGER PRIMARY KEY, RATING_GAME_COMPLETED BOOLEAN DEFAULT 'FALSE', RATING_HOST_COMPLETED BOOLEAN DEFAULT 'FALSE')")
    for (x in 1..amountUsers) {
        db?.execSQL("INSERT INTO $tabellenname(USER_ID) VALUES ($x)")
    }
}