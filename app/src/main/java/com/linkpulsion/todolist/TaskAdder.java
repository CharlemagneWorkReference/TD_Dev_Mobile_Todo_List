/*
 * Copyright (c) 2016 - Cyprien Aubry
 * Tous Droits Reservés
 */

package com.linkpulsion.todolist;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * !!! CLASSE OBSOLETE !!!
 * Ancienne activité d'ajout de tâche, remplacée par un
 * alert dialog.
 * @author Cyprien
 * @version 1.0
 */
public class TaskAdder extends AppCompatActivity {

    private Button add;
    private EditText task;
    private TextInputLayout taskLay;
    private BDD bdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_adder);

        //on récupère l'instance statique de la BDD
        bdd = MainActivity.bdd;

        //on instancie les Views
        add = (Button)findViewById(R.id.add);
        task = (EditText)findViewById(R.id.task_entry);
        taskLay = (TextInputLayout)findViewById(R.id.task_layout);

        //on crée le listener du bouton d'ajout
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //on teste si les valeurs entrées sont cohérentes
                if(task.getText().toString().equals("")){
                    //si non on passe le TextInputLayout en erreur
                    taskLay.setError(getString(R.string.entry_error));
                    taskLay.setErrorEnabled(true);
                }else{
                    //si oui, on enlève le statut d'erreur si jamais on l'avait activé
                    taskLay.setErrorEnabled(false);
                    //on insère l'activité dans la BDD
                    bdd.getWritableDatabase().execSQL("INSERT INTO " + bdd.TABLE_NAME +
                    " (" + bdd.KEY_DESC + ") VALUES ('" + task.getText() + "');");
                    //on affiche un message de confirmation
                    Toast.makeText(TaskAdder.this,getString(R.string.add_success),Toast.LENGTH_LONG).show();
                    //on retourne dur l'activité de départ
                    finish();
                }
            }
        });
    }
}
