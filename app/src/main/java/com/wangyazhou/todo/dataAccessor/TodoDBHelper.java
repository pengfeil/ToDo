package com.wangyazhou.todo.dataAccessor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TodoDBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 3;
    public static final String DB_NAME = "TODO_DB";
    public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TodoItem.TABLE_NAME + " ("
            + TodoItem.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TodoItem.KEY_IS_DONE + " INTEGER, "
            + TodoItem.KEY_DESCRIPTION + " TEXT, "
            + TodoItem.KEY_THUMBNAIL + " Blob, "
            + TodoItem.KEY_FULL_IMAGE + " TEXT, "
            + TodoItem.KEY_CREATE_DATETIME + " LONG, "
            + TodoItem.KEY_DONE_DATETIME + " LONG, "
            + TodoItem.KEY_UPDATE_DATETIME + " LONG)";

    public TodoDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(this.getClass().getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TodoItem.TABLE_NAME);
        onCreate(db);
    }
}
