package com.linkpulsion.todolist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Debugger extends AppCompatActivity {

    private BDD bdd;
    private TextView dbCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debugger);

        bdd = MainActivity.bdd;

        dbCount = (TextView)findViewById(R.id.db_count);
        dbCount.setText(getString(R.string.db_count) + " " + getTaskCount());
    }

    private int getTaskCount() {
        String countQuery = "SELECT  * FROM " + bdd.TABLE_NAME;
        SQLiteDatabase db = bdd.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }
}
