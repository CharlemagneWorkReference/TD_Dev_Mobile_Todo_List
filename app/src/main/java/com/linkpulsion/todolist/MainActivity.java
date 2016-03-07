/*
 * Copyright (c) 2016 - Cyprien Aubry
 * Tous Droits Reservés
 */

package com.linkpulsion.todolist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static BDD bdd;
    private ListView todoList;
    private SwipeRefreshLayout refresher;
    private ArrayAdapter<String> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bdd = new BDD(this);

        todoList = (ListView)findViewById(R.id.listView);

        refresher = (SwipeRefreshLayout)findViewById(R.id.refresher);
        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                taskList = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,getTasks());
                todoList.setAdapter(taskList);
                refresher.setRefreshing(false);
            }
        });

        taskList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getTasks());
        todoList.setAdapter(taskList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.debug_item){
            //on lance le debugger
            startActivity(new Intent(this,Debugger.class));
        }
        if(id == R.id.task_add_item){
            //on ajoute un tache
            startActivity(new Intent(this,TaskAdder.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private int getTaskCount() {
        String countQuery = "SELECT  * FROM " + bdd.TABLE_NAME;
        SQLiteDatabase db = bdd.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    private ArrayList<String> getTasks(){
        ArrayList<String> res = new ArrayList<>();
        int count = getTaskCount();
        for(int i=1;i<=count;i++){
            String item = "";
            Cursor cursor = null;
            try{
                cursor = bdd.getReadableDatabase().rawQuery("SELECT desc FROM " + bdd.TABLE_NAME + " WHERE id=?",new String[] {i + ""});
                if(cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    item = cursor.getString(cursor.getColumnIndex("desc"));
                }
            }finally {
                res.add(item);
                cursor.close();
            }
        }
        return res;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
