package com.example.android.myvolleyapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.myvolleyapp.data.UserContract.UserEntry;

/**
 * This is an adapter that adapts the card view to the
 * the recycler view
 */

class UserNameDbHelper extends SQLiteOpenHelper {
    // Name of the database file
    private static final String DATABASE_NAME = "Username.db";
    // Database version to be changed when schema is changed.
    private static final int DATABASE_VERSION = 2;

    UserNameDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This is executed when the database is initially created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_USER_TABLE =  "CREATE TABLE " + UserContract.UserEntry.TABLE_NAME + " ("
                + UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserEntry.COLUMN_USER_NAME + " TEXT NOT NULL, "
                + UserEntry.COLUMN_IMAGE_URL + " TEXT, "
                + UserEntry.COLUMN_USER_URL  + " TEXT, "
                + UserEntry.COLUMN_USER_INFO + " TEXT)";
        db.execSQL(SQL_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete old database, create a new one in its place
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
