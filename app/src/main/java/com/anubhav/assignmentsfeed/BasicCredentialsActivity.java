package com.anubhav.assignmentsfeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BasicCredentialsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserID, personType, emailAddress, profileStatus;
    private DatabaseReference mUsersReference, mFullNameToIDReference, mProfessorsReference;

    private ProgressDialog loadingBar;
    private MaterialButton submitButton;
    private TextInputEditText textInputEditTextFirstName, textInputEditTextLastName, textInputEditTextSchoolName, textInputEditTextEmailAddress, textInputEditTextProfileStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_credentials);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        emailAddress = mAuth.getCurrentUser().getEmail();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mFullNameToIDReference = FirebaseDatabase.getInstance().getReference().child("Full Name to ID");
        mProfessorsReference = FirebaseDatabase.getInstance().getReference().child("Professors");

        loadingBar = new ProgressDialog(BasicCredentialsActivity.this);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setEnabled(false);
        textInputEditTextFirstName = findViewById(R.id.editTextFirstName);
        textInputEditTextLastName = findViewById(R.id.editTextLastName);
        textInputEditTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        textInputEditTextEmailAddress.setText(emailAddress);
        textInputEditTextEmailAddress.setEnabled(false);
        textInputEditTextSchoolName = findViewById(R.id.editTextSchoolName);
        textInputEditTextProfileStatus = findViewById(R.id.editTextProfileStatus);

        if (currentUserID != null) {
            mUsersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    personType = snapshot.child("personType").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        submitButton.setOnClickListener(v -> {
            loadingBar.setTitle("Saving credentials...");
            loadingBar.setMessage("Please wait while we are processing your request.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            String firstName = textInputEditTextFirstName.getText().toString().trim();
            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
            String lastName = textInputEditTextLastName.getText().toString().trim();
            lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();

            String fullName = firstName + " " + lastName;
            profileStatus = textInputEditTextProfileStatus.getText().toString().trim();
            String schoolName = textInputEditTextSchoolName.getText().toString().trim();

            HashMap userMap = new HashMap();
                userMap.put("fullName", fullName);
                userMap.put("emailAddress", emailAddress);
                userMap.put("userID", currentUserID);
                userMap.put("profileStatus", profileStatus);
                userMap.put("schoolName", schoolName);

            mFullNameToIDReference.child(fullName).child("userID").setValue(currentUserID);

            mUsersReference.updateChildren(userMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (personType.equals("Professor"))
                        mProfessorsReference.child(currentUserID).updateChildren(userMap);

                    sendUserToSetupActivity();
                    Toast.makeText(BasicCredentialsActivity.this, "Your other credentials were saved successfully!", Toast.LENGTH_SHORT).show();
                }

                else {
                    String errorMessage = task.getException().toString();
                    Toast.makeText(BasicCredentialsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                loadingBar.dismiss();
            });
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String firstName = textInputEditTextFirstName.getText().toString().trim();
                        final String lastName = textInputEditTextLastName.getText().toString().trim();
                        final String schoolName = textInputEditTextSchoolName.getText().toString().trim();
                        final String profileStatus = textInputEditTextProfileStatus.getText().toString().trim();

                        runOnUiThread(() -> {
                            if (! firstName.isEmpty() && ! lastName.isEmpty() && ! schoolName.isEmpty() && ! profileStatus.isEmpty()) {
                                submitButton.setBackgroundTintList(ContextCompat.getColorStateList(BasicCredentialsActivity.this, R.color.colorAccent));
                                submitButton.setEnabled(true);
                            }

                            else {
                                submitButton.setBackgroundTintList(ContextCompat.getColorStateList(BasicCredentialsActivity.this, R.color.colorGray));
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
        Intent setupIntent = new Intent(BasicCredentialsActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}