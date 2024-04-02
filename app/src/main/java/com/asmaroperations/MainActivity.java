package com.asmaroperations;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btn_add;
    private DatabaseReference databaseReference;
    private List<TaskModel> taskList;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        btn_add = findViewById(R.id.add_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaskModel task = snapshot.getValue(TaskModel.class);
                    taskList.add(task);
                }
                if (taskAdapter == null) {
                    taskAdapter = new TaskAdapter(MainActivity.this, taskList, new TaskAdapter.OnTaskClickListener() {
                        @Override
                        public void onUpdateClick(TaskModel task) {
                            showUpdateTaskDialog(task);
                        }

                        @Override
                        public void onDeleteClick(TaskModel task) {
                            deleteTask(task);
                        }

                        @Override
                        public void onTaskClick(TaskModel task) {
                            // Handle task click if needed (optional)
                        }
                    });
                    recyclerView.setAdapter(taskAdapter);
                } else {
                    taskAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateTaskDialog(TaskModel task) {
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Task");

        final EditText inputField = new EditText(this);
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputField);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskName = inputField.getText().toString().trim();
                if (!TextUtils.isEmpty(taskName)) {
                    String id = databaseReference.push().getKey();
                    TaskModel task = new TaskModel(id, taskName);
                    databaseReference.child(id).setValue(task);
                    Toast.makeText(MainActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteTask(TaskModel task) {
        DatabaseReference reference = databaseReference.child(task.getId());
        reference.removeValue();
    }}