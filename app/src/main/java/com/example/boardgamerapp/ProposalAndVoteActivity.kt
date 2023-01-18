package com.example.boardgamerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class   ProposalAndVoteActivity : AppCompatActivity() {
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proposal_and_vote)

        val dbhelper = DB_class(applicationContext)
        val db = dbhelper.readableDatabase
        val userId = intent.getStringExtra("USER_ID")
        val group = intent.getStringExtra("GROUP")
        val gameId = intent.getStringExtra("GAME_ID")
        val fabAddProposal = findViewById<FloatingActionButton>(R.id.fabAddProposal)


        val tabellenname = "TBL_${group.toString().uppercase()}_GAME${gameId}"
        val query = "SELECT * FROM $tabellenname "
        val rs = db.rawQuery(query, null)
        rs.moveToLast()
        val amountRows = rs.getInt(rs.getColumnIndex("PROP_ID"))
        //val list = ArrayList<String>()
        val proposalList = mutableListOf<RecyclerViewEntry>()
        for (x in 0..amountRows) {
            if (rs.moveToPosition(x)) {
                val gameName = rs.getString(rs.getColumnIndex("GAME_NAME"))
                val rating = rs.getInt(rs.getColumnIndex("RATING"))
                //list.add("$amountRows $game_name + $rating")
                proposalList += RecyclerViewEntry(gameName, rating)
            }
        }
        rs.close()

        val recyclerView = findViewById<RecyclerView>(R.id.myRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyRecyclerViewAdapter(
            proposalList,
        ) { selectedItem: RecyclerViewEntry ->
            if (userId != null) {
                listItemClicked(selectedItem, db, tabellenname, userId)
            }
        }
        fabAddProposal.setOnClickListener {
            startActivity(Intent(this,AddProposalActivity::class.java).putExtra("TABELLENNAME", tabellenname))
        }
    }

    private fun listItemClicked(
        RecyclerViewEntry: RecyclerViewEntry,
        db: SQLiteDatabase,
        tabellenname: String,
        userId: String?) {
        //Prüfen ob bereits abgestimmt wurde
        val tabellenname_rating_completed = tabellenname.plus("_RATING_COMPLETED")
        val query = "SELECT * FROM $tabellenname_rating_completed WHERE USER_ID== $userId AND RATING_GAME_COMPLETED=='FALSE' "
        val rs = db.rawQuery(query,null)
        if(rs.moveToNext()) {
            //Aktuelisieren des Wertes in der Datenbank
                db.execSQL("UPDATE $tabellenname SET RATING=RATING+1 WHERE GAME_NAME=='${RecyclerViewEntry.game_name}' ")
                db.execSQL("UPDATE $tabellenname_rating_completed SET RATING_GAME_COMPLETED='TRUE' WHERE USER_ID== $userId ")
            rs.close()
            startActivity(getIntent())
        }
        else {
            Toast.makeText(
                this@ProposalAndVoteActivity,
                "Du hast bereits abgestimmt",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java).putExtra("USER_ID", intent.getStringExtra("USER_ID")))
    }
}