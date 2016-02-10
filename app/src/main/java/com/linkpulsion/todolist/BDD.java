package com.linkpulsion.todolist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by cyprien on 09/02/16.
 */
public class BDD extends SQLiteOpenHelper {

    private final static int VERSION = 1;
    private final static String DB_NAME = "Todo.db";
    public final static String TABLE_NAME = "ToDo";
    public final static String KEY_ID = "id";
    public final static String KEY_DESC = "desc";
    private final static String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DESC + " TEXT);";

    public BDD(Context context){
        super(context,DB_NAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
