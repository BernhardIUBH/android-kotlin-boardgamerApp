package com.example.boardgamerapp
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DB_class(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "Boardgamer_Database"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //Erstelle TBL_USER
        db?.execSQL("CREATE TABLE TBL_USER(USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT UNIQUE, PWD TEXT, GRP TEXT, TELNR TEXT)")
        db?.execSQL("INSERT INTO TBL_USER(USERNAME, PWD, GRP, TELNR) VALUES ('Max', '', 'group1', '+43 111111111')")
        db?.execSQL("INSERT INTO TBL_USER(USERNAME, PWD, GRP, TELNR) VALUES ('Lisa', '123', 'group1', '+49 222222222')")
        db?.execSQL("INSERT INTO TBL_USER(USERNAME, PWD, GRP, TELNR) VALUES ('Johan', '', 'group1', '+43 333333333')")
        db?.execSQL("INSERT INTO TBL_USER(USERNAME, PWD, GRP, TELNR) VALUES ('Anna', '', 'group1', '+49 444444444')")
        db?.execSQL("INSERT INTO TBL_USER(USERNAME, PWD, GRP, TELNR) VALUES ('Ben', '', 'group1', '+49 555555555')")

        //Erstelle Gruppen-Tabelle TBL_GAME
        db?.execSQL("CREATE TABLE TBL_GAME(GAME_ID INTEGER PRIMARY KEY AUTOINCREMENT, GRP TEXT, GRP_GAME_ID INTEGER, DATE DATE, HOST INTEGER, FOREIGN KEY (GRP) REFERENCES TBL_USER(GRP) ON DELETE CASCADE ON UPDATE CASCADE )")

        //Erstelle Tabelle TBL_GROUP_RATING_HOST
        db?.execSQL("CREATE TABLE TBL_RATING_HOST(GRP_GAME_ID PRIMARY KEY, R_HOST INTEGER DEFAULT 0, R_FOOD INTEGER DEFAULT 0, R_EVENT INTEGER DEFAULT 0, FOREIGN KEY (GRP_GAME_ID) REFERENCES TBL_GAME(GRP_GAME_ID) ON DELETE CASCADE ON UPDATE CASCADE )")


//-------------------Optional: Daten erstellen - Für Demonstration Bewertung Host---------------------//
        db?.execSQL("INSERT INTO TBL_GAME(GRP, GRP_GAME_ID, DATE, HOST) VALUES ('group1', 1, '2023-01-07', '5')")
        db?.execSQL("CREATE TABLE TBL_GROUP1_GAME1_RATING_COMPLETED(USER_ID INTEGER PRIMARY KEY, RATING_GAME_COMPLETED BOOLEAN DEFAULT 'FALSE', RATING_HOST_COMPLETED BOOLEAN DEFAULT 'FALSE')")
        for (x in 1..5) {
            db?.execSQL("INSERT INTO TBL_GROUP1_GAME1_RATING_COMPLETED(USER_ID) VALUES ($x)")
        }
        db?.execSQL("INSERT INTO TBL_RATING_HOST(GRP_GAME_ID) VALUES (1)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        onCreate(db)
    }
}