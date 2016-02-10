package com.linkpulsion.todolist;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TaskAdder extends AppCompatActivity {

    private Button add;
    private EditText task;
    private TextInputLayout taskLay;
    private BDD bdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_adder);

        bdd = MainActivity.bdd;

        add = (Button)findViewById(R.id.add);
        task = (EditText)findViewById(R.id.task_entry);
        taskLay = (TextInputLayout)findViewById(R.id.task_layout);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(task.getText().toString().equals("")){
                    taskLay.setError(getString(R.string.entry_error));
                    taskLay.setErrorEnabled(true);
                }else{
                    taskLay.setErrorEnabled(false);
                    bdd.getWritableDatabase().execSQL("INSERT INTO " + bdd.TABLE_NAME +
                    " (" + bdd.KEY_DESC + ") VALUES ('" + task.getText() + "');");
                    Toast.makeText(TaskAdder.this,getString(R.string.add_success),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
