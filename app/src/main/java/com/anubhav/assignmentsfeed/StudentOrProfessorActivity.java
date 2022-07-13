package com.anubhav.assignmentsfeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class StudentOrProfessorActivity extends AppCompatActivity {

    private ProgressDialog loadingBar;
    private AppCompatSpinner spinner;
    private MaterialButton submitButton;
    private String currentUserID, personType;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_or_professor);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        spinner = findViewById(R.id.spinner);
        submitButton = findViewById(R.id.basicCredentialsSubmitButton);

        loadingBar = new ProgressDialog(StudentOrProfessorActivity.this);

        ArrayList<String> list = new ArrayList<>();
        list.add("Student or Professor?");
        list.add("Student");
        list.add("Professor");

        final ArrayAdapter adapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,list);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                personType = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        submitButton.setOnClickListener(v -> {
            loadingBar.setTitle("Saving credentials...");
            loadingBar.setMessage("Please wait while we are processing your request.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            HashMap userMap = new HashMap();
                userMap.put("personType", personType);

            mUsersReference.updateChildren(userMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserToSetupActivity();
                    Toast.makeText(StudentOrProfessorActivity.this, "Your credentials were saved successfully!", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        runOnUiThread(() -> {
                            if (! spinner.getSelectedItem().equals("Student or Professor?")) {
                                submitButton.setBackgroundTintList(ContextCompat.getColorStateList(StudentOrProfessorActivity.this, R.color.colorAccent));
                                submitButton.setEnabled(true);
                            }

                            else {
                                submitButton.setBackgroundTintList(ContextCompat.getColorStateList(StudentOrProfessorActivity.this, R.color.colorGray));
                                submitButton.setEnabled(false);
                            }

                        });
                    }

                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(StudentOrProfessorActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}