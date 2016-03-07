/*
 * Copyright (c) 2016 - Cyprien Aubry
 * Tous Droits Reservés
 */

package com.linkpulsion.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static BDD bdd;
    private ListView todoList;
    private SwipeRefreshLayout refresher;
    private ArrayAdapter<String> taskList;
    private FloatingActionButton fabAdd;

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
                refreshList();
            }
        });

        taskList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getTasks());
        todoList.setAdapter(taskList);
        registerForContextMenu(todoList);

        fabAdd = (FloatingActionButton)findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.add_dialog, null);
                dialogBuilder.setView(dialogView);

                final EditText edt = (EditText) dialogView.findViewById(R.id.task);
                final TextInputLayout edtLay = (TextInputLayout)dialogView.findViewById(R.id.adder_lay);

                dialogBuilder.setTitle(R.string.task_add);
                dialogBuilder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(edt.getText().toString().equals("")){
                            edtLay.setError(getString(R.string.entry_error));
                            edtLay.setErrorEnabled(true);
                        }else{
                            edtLay.setErrorEnabled(false);
                            bdd.getWritableDatabase().execSQL("INSERT INTO " + bdd.TABLE_NAME +
                                    " (" + bdd.KEY_DESC + ") VALUES ('" + edt.getText() + "');");
                            Toast.makeText(MainActivity.this,getString(R.string.add_success),Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //NOTHING
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
            }
        });
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
        if(id == R.id.void_item){
            this.voidList();
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

    private void voidList(){
        bdd.getWritableDatabase().execSQL("DELETE FROM ToDo");
        bdd.getWritableDatabase().execSQL("VACUUM");
        bdd.getWritableDatabase().execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='ToDo';");
        refreshList();
    }

    private void refreshList(){
        taskList = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,getTasks());
        todoList.setAdapter(taskList);
        refresher.setRefreshing(false);
    }

    private void deleteItem(int id){
        bdd.getWritableDatabase().execSQL("DELETE FROM ToDO WHERE id=" + id);
        refreshList();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(getString(R.string.delete_task) + info.toString());
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.menu);
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = String.valueOf(info.position);

        if(menuItemIndex == 0){
            deleteItem(info.position + 1);
        }else{
            //NOTHING
        }
        bdd.getWritableDatabase().execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='ToDo';");

        Toast.makeText(this,String.format("Selected %s for item %s", menuItemName, listItemName),Toast.LENGTH_LONG).show();
        return true;
    }
}
