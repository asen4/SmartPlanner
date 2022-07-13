package com.anubhav.assignmentsfeed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SetupActivity extends AppCompatActivity {

    private ArrayList<Task> unfinishedTasksList, finishedTasksList;
    private UnfinishedTaskAdapter unfinishedTaskAdapter;
    private FinishedTaskAdapter finishedTaskAdapter;
    private MaterialButton submitButton;
    private RecyclerView unfinishedTasksRV, finishedTasksRV;

    private TextView whatToDoMessage, completedMessage;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference mUsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        unfinishedTasksList = new ArrayList<>();
        finishedTasksList = new ArrayList<>();
        unfinishedTaskAdapter = new UnfinishedTaskAdapter(unfinishedTasksList);
        finishedTaskAdapter = new FinishedTaskAdapter(finishedTasksList);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setEnabled(false);
        unfinishedTasksRV = findViewById(R.id.unfinishedTasksList);
        finishedTasksRV = findViewById(R.id.finishedTasksList);

        whatToDoMessage = findViewById(R.id.whatToDoMessage);
        completedMessage = findViewById(R.id.completedMessage);

        unfinishedTasksRV.setLayoutManager(new LinearLayoutManager(SetupActivity.this));
        unfinishedTasksRV.setAdapter(unfinishedTaskAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(SetupActivity.this, LinearLayout.VERTICAL);
        unfinishedTasksRV.addItemDecoration(dividerItemDecoration);

        finishedTasksRV.setLayoutManager(new LinearLayoutManager(SetupActivity.this));
        finishedTasksRV.setAdapter(finishedTaskAdapter);
        finishedTasksRV.addItemDecoration(dividerItemDecoration);

        unfinishedTasksList.add(new Task("Profile Image", R.drawable.ic_baseline_arrow_forward_ios_24));
        unfinishedTasksList.add(new Task("Student or Professor?", R.drawable.ic_baseline_arrow_forward_ios_24));
        unfinishedTasksList.add(new Task("Basic Credentials", R.drawable.ic_baseline_arrow_forward_ios_24));

        mUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("profileImage") && unfinishedTasksList.size() != 0) {
                    unfinishedTasksList.remove(0);
                    unfinishedTaskAdapter.notifyItemRemoved(0);
                    finishedTasksList.add(new Task("Profile Image", R.drawable.ic_baseline_check_24));
                    finishedTaskAdapter.notifyItemInserted(0);
                    completedMessage.setVisibility(View.VISIBLE);
                    finishedTasksRV.setVisibility(View.VISIBLE);
                }

                if (dataSnapshot.hasChild("personType")  && unfinishedTasksList.size() != 0) {
                    unfinishedTasksList.remove(0);
                    unfinishedTaskAdapter.notifyItemRemoved(0);
                    finishedTasksList.add(new Task("Student or Professor?", R.drawable.ic_baseline_check_24));
                    finishedTaskAdapter.notifyItemInserted(1);
                }

                if (dataSnapshot.hasChild("fullName")  && unfinishedTasksList.size() != 0) {
                    unfinishedTasksList.remove(0);
                    unfinishedTaskAdapter.notifyItemRemoved(0);
                    finishedTasksList.add(new Task("Basic Credentials", R.drawable.ic_baseline_check_24));
                    finishedTaskAdapter.notifyItemInserted(2);
                    whatToDoMessage.setVisibility(View.GONE);
                    unfinishedTasksRV.setVisibility(View.GONE);
                }

                if (dataSnapshot.hasChild("profileImage") && dataSnapshot.hasChild("personType") && dataSnapshot.hasChild("fullName")) {
                    submitButton.setEnabled(true);
                    submitButton.setBackgroundTintList(ContextCompat.getColorStateList(SetupActivity.this, R.color.colorAccent));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        submitButton.setOnClickListener(v -> sendUserToMainActivity());
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}