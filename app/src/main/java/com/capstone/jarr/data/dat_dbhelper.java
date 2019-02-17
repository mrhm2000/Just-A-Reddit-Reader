package com.capstone.jarr.data;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.capstone.jarr.data.dat_contract;


/**
 * Created on 10/15/2018.
 */


public class dat_dbhelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    public dat_dbhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + dat_contract.posts.TABLE_NAME + " (" +
                    dat_contract.posts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    dat_contract.posts.DATA + " STRING)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + dat_contract.posts.TABLE_NAME;
}