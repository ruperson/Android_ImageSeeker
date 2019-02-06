package ru.ifmo.ctddev.vanyan.imageseeker.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.ifmo.ctddev.vanyan.imageseeker.utilities.Contract.Entry;


public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "little_gallery.db";

    private static final int DATABASE_VERSION = 1;


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PHOTO_TABLE =  "CREATE TABLE " + Entry.TABLE_NAME + " ("
                + Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Entry.COLUMN_SMALL_PHOTO + " TEXT NOT NULL, "
                + Entry.COLUMN_BIG_PHOTO + " TEXT NOT NULL, "
                + Entry.COLUMN_DESCRIPTION + " TEXT);";

        db.execSQL(SQL_CREATE_PHOTO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){}
}