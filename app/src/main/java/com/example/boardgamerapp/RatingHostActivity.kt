package com.example.boardgamerapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RatingHostActivity : AppCompatActivity() {
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating_host)

        val dbhelper = DB_class(applicationContext)
        val db = dbhelper.readableDatabase
        val userId = intent.getStringExtra("USER_ID")
        val group = intent.getStringExtra("GROUP")
        val grpGameId = intent.getStringExtra("GRP_GAME_ID")
        val prevHost = intent.getStringExtra("PREVHOST")
        val prevHostName = intent.getStringExtra("PREVHOSTNAME")
        val tvHostAcHost = findViewById<TextView>(R.id.tvHostAcHost)
        val tvHostAcDate = findViewById<TextView>(R.id.tvHostAcDate)
        val formatDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val prevGrpGameId = (grpGameId?.toInt())?.minus(1)
        val tvVoterTurnout = findViewById<TextView>(R.id.tvVoterTurnout)
        var rbResult = findViewById<RatingBar>(R.id.rbResult)
        var rbRatingHostHost = findViewById<RatingBar>(R.id.rbRatingHostEvent)
        var rbRatingHostFood = findViewById<RatingBar>(R.id.rbRatingHostHost)
        var rbRatingHostEvent = findViewById<RatingBar>(R.id.rbRatingHostFood)
        val btnAddRating = findViewById<Button>(R.id.btnAddRating)
        var flagUserVoted = 1
        var totalVoters = ""
        var voterTurnout = ""
        var avgRatingHost = 0F
        var avgRatingFood = 0F
        var avgRatingEvent = 0F

        //Prüfung ob bereits abgestimmt wurde
        val tabellenname_rating_completed = "TBL_${group.toString().uppercase()}_GAME${prevGrpGameId}_RATING_COMPLETED"
        var query = "SELECT * FROM $tabellenname_rating_completed WHERE USER_ID= $userId AND RATING_HOST_COMPLETED='FALSE' "
        var rs = db.rawQuery(query, null)
        if (rs.moveToNext()) {
            flagUserVoted = 0
        }

        //Container: Letzter Gastgeber + Datum
        var tabellenname = "TBL_GAME"
        query = "SELECT * FROM $tabellenname WHERE HOST= $prevHost "
        rs = db.rawQuery(query, null)
        if(rs.moveToNext()){
            val date = rs.getString(rs.getColumnIndex("DATE"))
            tvHostAcDate.text = LocalDate.parse(date).format(formatDate)
        }
        tvHostAcHost.text = prevHostName

        //Container: Bewertung + Beteiligung
        //Werte aus Datenbank auslesen
        tabellenname = "TBL_RATING_HOST"
        query = "SELECT * FROM $tabellenname WHERE GRP_GAME_ID= $prevGrpGameId"
        rs = db.rawQuery(query, null)
        if(rs.moveToNext()) {
            avgRatingHost = rs.getString(rs.getColumnIndex("R_HOST")).toFloat()
            avgRatingFood = rs.getString(rs.getColumnIndex("R_FOOD")).toFloat()
            avgRatingEvent = rs.getString(rs.getColumnIndex("R_EVENT")).toFloat()
        }
        rs.close()
        query = "SELECT COUNT(*) FROM $tabellenname_rating_completed WHERE RATING_HOST_COMPLETED='TRUE' "
        rs = db.rawQuery(query, null)
        if(rs.moveToNext()) {
            voterTurnout = rs.getString(rs.getColumnIndex("COUNT(*)"))
        }
        rs.close()
        query = "SELECT COUNT(*) FROM $tabellenname_rating_completed "
        rs = db.rawQuery(query, null)
        if(rs.moveToNext()) {
            totalVoters = rs.getString(rs.getColumnIndex("COUNT(*)"))
        }
        rs.close()
        rbResult.rating = ((avgRatingFood+avgRatingEvent+avgRatingHost)/3F)/voterTurnout.toFloat()
        rbResult.setIsIndicator(true)
        tvVoterTurnout.text = voterTurnout.plus("/").plus(totalVoters)

        //Rating abgeschlossen - Modus
        if(flagUserVoted==1) {
            rbRatingHostHost.rating = avgRatingHost/voterTurnout.toFloat()
            rbRatingHostFood.rating = avgRatingFood/voterTurnout.toFloat()
            rbRatingHostEvent.rating = avgRatingEvent/voterTurnout.toFloat()
            rbRatingHostHost.setIsIndicator((true))
            rbRatingHostFood.setIsIndicator((true))
            rbRatingHostEvent.setIsIndicator((true))
            btnAddRating.setVisibility(View.GONE);
        }
        //Container Bewertung abgeben
        btnAddRating.setOnClickListener {
            val valueRbRatingHostHost = rbRatingHostHost.rating
            val valueRbRatingHostFood = rbRatingHostFood.rating
            val valueRbRatingHostEvent = rbRatingHostEvent.rating
            if (flagUserVoted==0) {
                //Aktuelisieren des Wertes in der Datenbank
                db?.execSQL("UPDATE $tabellenname SET R_HOST=R_HOST+$valueRbRatingHostHost WHERE GRP_GAME_ID= $prevGrpGameId ")
                db?.execSQL("UPDATE $tabellenname SET R_EVENT=R_EVENT+$valueRbRatingHostEvent WHERE GRP_GAME_ID= $prevGrpGameId ")
                db?.execSQL("UPDATE $tabellenname SET R_FOOD=R_FOOD+$valueRbRatingHostFood WHERE GRP_GAME_ID= $prevGrpGameId ")
                db?.execSQL("UPDATE $tabellenname_rating_completed SET RATING_HOST_COMPLETED='TRUE' WHERE USER_ID== $userId ")
                Toast.makeText(
                    this@RatingHostActivity,
                    "Bewertung abgegeben!", Toast.LENGTH_SHORT
                ).show()
                rs.close()
                startActivity(getIntent())
            }
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java).putExtra("USER_ID", intent.getStringExtra("USER_ID")))
    }
}