/*
 * Copyright (c) 2016 - Cyprien Aubry
 * Tous Droits Reservés
 */

package com.linkpulsion.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Activité principale de l'application, c'est celle
 * qui est lancée au démarrage de l'app
 *
 * @author Cyprien
 * @version 1.3
 */
public class MainActivity extends AppCompatActivity {

    public static BDD bdd;
    private ListView todoList;
    private SwipeRefreshLayout refresher;
    private ArrayAdapter<String> taskList;
    private FloatingActionButton fabAdd;
    private boolean tri;
    private CoordinatorLayout mainLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MediaPlayer troll = MediaPlayer.create(this,R.raw.troll);

        //on crée une instance de la BDD
        bdd = new BDD(this);

        //on instancie le coordinator layout
        mainLay = (CoordinatorLayout) findViewById(R.id.main_lay);

        //on instancie la liste
        todoList = (ListView) findViewById(R.id.listView);

        //on instancie le refresher
        refresher = (SwipeRefreshLayout) findViewById(R.id.refresher);

        //on crée le listener du refresher, il est chargé de rafraîchir
        //la liste
        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        //on instancie l'adapter de la liste en lui donnant une taille
        //prédéfinie par Android
        taskList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getTasks());

        //on l'associe à la liste
        todoList.setAdapter(taskList);

        //et on l'enregistre pour pourvoir utiliser le menu contextuel
        registerForContextMenu(todoList);

        //on instancie le FAB
        fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);

        //et on crée son listener qui est chargé d'ajouter unet tâche
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on instancie le constructeur de boîtes de dialogue
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                //on récupère l'inflater de layout da l'activité courante
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                //on inflate le layout personnalisé
                final View dialogView = inflater.inflate(R.layout.add_dialog, null);
                //on l'utilise pour personnaliser la boîte de dialogue
                dialogBuilder.setView(dialogView);

                //on instancie les elements de la boîte de dialogue
                final EditText edt = (EditText) dialogView.findViewById(R.id.task);
                final TextInputLayout edtLay = (TextInputLayout) dialogView.findViewById(R.id.adder_lay);
                final CheckBox moreOptions = (CheckBox) dialogView.findViewById(R.id.more);
                final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_selector);

                //on crée le listener de la check box
                moreOptions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            datePicker.setVisibility(View.VISIBLE);
                        } else {
                            datePicker.setVisibility(View.INVISIBLE);
                        }
                    }
                });

                //on choisit un titre
                dialogBuilder.setTitle(R.string.task_add);

                //un bouton de réponse positive
                dialogBuilder.setNeutralButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (edt.getText().toString().equals("")) {
                            edtLay.setError(getString(R.string.entry_error));
                            edtLay.setErrorEnabled(true);
                        } else {
                            edtLay.setErrorEnabled(false);

                            String deadline = null;

                            if(moreOptions.isChecked()){
                                deadline = String.valueOf(datePicker.getDayOfMonth()) + "-" + String.valueOf(datePicker.getMonth()+1) + "-" + String.valueOf(datePicker.getYear());
                            }

                            bdd.getWritableDatabase().execSQL("INSERT INTO " + bdd.TABLE_NAME +
                                    " (" + bdd.KEY_DESC + "," + bdd.KEY_DATE + ") VALUES ('" + edt.getText() + "', '" + deadline + "');");

                            //Toast.makeText(MainActivity.this,getString(R.string.add_success),Toast.LENGTH_LONG).show();

                            Snackbar snackbar = Snackbar.make(mainLay, getString(R.string.add_success), Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }
                    }
                });

                //un bouton de réponse négative
                dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //on ne fait rien, juste dismiss le dialogue
                    }
                });

                //on crée la boite de dialogue
                AlertDialog b = dialogBuilder.create();

                //on l'affiche
                b.show();
            }
        });

        //les tâche ne sont pas triées de base
        tri = false;

        todoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //on instancie le constructeur de boîtes de dialogue
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                //on récupère l'inflater de layout da l'activité courante
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                //on inflate le layout personnalisé
                final View dialogView = inflater.inflate(R.layout.view_dialog, null);
                //on l'utilise pour personnaliser la boîte de dialogue
                dialogBuilder.setView(dialogView);

                //on instancie les deux textviews
                TextView desc = (TextView)dialogView.findViewById(R.id.desc);
                TextView deadLine = (TextView)dialogView.findViewById(R.id.deadline);

                desc.setText("Description : " + getTaskDesc(position + 1));
                if(getTaskDeadline(position + 1) == null || getTaskDeadline(position + 1).equals("null")){
                    deadLine.setText("Deadline : aucune");
                }else{
                    deadLine.setText("Deadline : " + getTaskDeadline(position + 1));
                }


                dialogBuilder.setNeutralButton(android.R.string.ok,null);

                //on choisit un titre
                dialogBuilder.setTitle(getString(R.string.view_item) + " " + position);

                //on crée la boite de dialogue
                AlertDialog b = dialogBuilder.create();

                //on l'affiche
                b.show();
            }
        });

        todoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        //on affiche les crédits
        Snackbar snackbar = Snackbar.make(mainLay, getString(R.string.credits), Snackbar.LENGTH_LONG)
                .setAction(R.string.action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            if(troll.isPlaying() || troll.isLooping()){
                                troll.pause();
                                troll.seekTo(0);
                                troll.prepare();
                            }else{
                                troll.start();
                            }
                        }catch(IOException e){
                            Log.e("EXCEPTION", "Error while playing sound");
                        } catch (IllegalStateException e) {
                            Log.e("EXCEPTION", "Error while playing sound");
                        }
                    }
                });
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.debug_item) {
            //on lance le debugger
            startActivity(new Intent(this, Debugger.class));
        }
        if (id == R.id.void_item) {
            //on vide la liste
            this.voidList();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Cette méthode compte le nombre de tâches présentes
     * dans la base de données
     *
     * @return int
     */
    private int getTaskCount() {
        String countQuery = "SELECT  * FROM " + bdd.TABLE_NAME;
        SQLiteDatabase db = bdd.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    /**
     * Cette méthode retourne la description d'une tâche ayant
     * l'id passé en paramètre
     * @param id int
     * @return String
     */
    private String getTaskDesc(int id){
        String[] params = {String.valueOf(id)};
        String res = null;
        Cursor cursor = null;
        try {
            cursor = bdd.getReadableDatabase().rawQuery("SELECT desc FROM " + bdd.TABLE_NAME + " WHERE id=?",params);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                res = cursor.getString(cursor.getColumnIndex("desc"));
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    /**
     * Cette méthode retourne la deadline d'une tâche ayant
     * l'id passé en paramètre
     * @param id int
     * @return GregorianCalendar
     */
    private String getTaskDeadline(int id){
        String[] params = {String.valueOf(id)};
        String res = null;
        Cursor cursor = null;
        try {
            cursor = bdd.getReadableDatabase().rawQuery("SELECT date FROM " + bdd.TABLE_NAME + " WHERE id=?",params);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                res = cursor.getString(cursor.getColumnIndex("date"));
            }
        } finally {
            cursor.close();
        }
        return res;
    }

    /**
     * Cette méthode retourne la liste de tâches présentes
     * dans la base de données
     *
     * @return int
     */
    private ArrayList<String> getTasks() {
        ArrayList<String> res = new ArrayList<>();
        int count = getTaskCount();
        for (int i = 1; i <= count; i++) {
            String item = "";
            Cursor cursor = null;
            try {
                cursor = bdd.getReadableDatabase().rawQuery("SELECT desc FROM " + bdd.TABLE_NAME + " WHERE id=?", new String[]{i + ""});
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    item = cursor.getString(cursor.getColumnIndex("desc"));
                }
            } finally {
                res.add(item);
                cursor.close();
            }
        }
        return res;
    }

    @Override
    protected void onResume() {
        //comme ça on peut reprendre là où on s'est arrêté
        super.onResume();
    }

    /**
     * Cette méthode est chargé de vider la table des
     * tâches dans la base de données
     */
    private void voidList() {
        //on vide
        bdd.getWritableDatabase().execSQL("DELETE FROM ToDo");
        bdd.getWritableDatabase().execSQL("VACUUM");
        //on remet le compteur d'auto incrément à 0
        bdd.getWritableDatabase().execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='ToDo';");
        //on actualise la liste
        refreshList();
    }

    /**
     * Cette méthode sert à rafraîchir la liste des tâches
     */
    private void refreshList() {
        //on met à jour l'adapter
        taskList = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getTasks());
        //on le réassigne à la liste de tâches
        todoList.setAdapter(taskList);
        //on stoppe l'animation de rafraîchissment
        refresher.setRefreshing(false);
    }

    /**
     * Cette méthode sert à supprimer un item en particulier
     * dans la liste
     *
     * @param id int
     */
    private void deleteItem(int id) {
        //on le supprime de la base
        bdd.getWritableDatabase().execSQL("DELETE FROM ToDo WHERE id=" + id);
        //et on met à jour
        refreshList();
        //on confirme
        Snackbar snackbar = Snackbar.make(mainLay, getString(R.string.delete_success), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * Cette méthode sert à modifier un item en particulier
     * dans la liste
     *
     * @param id int
     */
    private void editItem(final int id) {
        //on instancie le constructeur de boîtes de dialogue
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        //on récupère l'inflater de layout da l'activité courante
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        //on inflate le layout personnalisé
        final View dialogView = inflater.inflate(R.layout.edit_dialog, null);
        //on l'utilise pour personnaliser la boîte de dialogue
        dialogBuilder.setView(dialogView);

        //on instancie les elements de la boîte de dialogue
        final EditText edt = (EditText) dialogView.findViewById(R.id.task);
        final TextInputLayout edtLay = (TextInputLayout) dialogView.findViewById(R.id.editor_lay);

        //on choisit un titre
        dialogBuilder.setTitle(getString(R.string.edit_item) + " " + id);

        //un bouton de réponse positive
        dialogBuilder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (edt.getText().toString().equals("")) {
                    edtLay.setError(getString(R.string.entry_error));
                    edtLay.setErrorEnabled(true);
                } else {
                    edtLay.setErrorEnabled(false);
                    bdd.getWritableDatabase().execSQL("UPDATE ToDo SET desc = '" + edt.getText()
                            + "' WHERE id=" + id);

                    //Toast.makeText(MainActivity.this,getString(R.string.edit_success),Toast.LENGTH_LONG).show();

                    Snackbar snackbar = Snackbar.make(mainLay, getString(R.string.edit_success), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    refreshList();
                }
            }
        });

        //un bouton de réponse négative
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //on ne fait rien, juste dismiss le dialogue
            }
        });

        //on crée la boite de dialogue
        AlertDialog b = dialogBuilder.create();

        //on l'affiche
        b.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listView) {
            AdapterView.AdapterContextMenuInfo options = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(getString(R.string.delete_task) + (((AdapterView.AdapterContextMenuInfo) menuInfo).id + 1) + ":");
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //on récupère l'adapter pour gérer la liste d'options du menu
        AdapterView.AdapterContextMenuInfo options = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.menu);
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = String.valueOf(options.position);

        if (menuItemIndex == 0) {
            //si on choisi l'option supprimer
            deleteItem(options.position + 1);
        }

        if (menuItemIndex == 1) {
            //si on choisit l'option modifier
            editItem(options.position + 1);
        }

        bdd.getWritableDatabase().execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='ToDo';");

        Log.d("CONTEXTMENU", "Option selectionnée : " + menuItemName + " pour l'item " + listItemName);
        return true;
    }
}
