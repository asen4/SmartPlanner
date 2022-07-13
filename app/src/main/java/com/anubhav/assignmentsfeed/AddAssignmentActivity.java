package com.anubhav.assignmentsfeed;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddAssignmentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String currentUserID, assignmentUniqueID;
    private DatabaseReference mUsersReference, mAssignmentsListReference;

    private EditText assignmentName;
    private TextInputEditText assignmentDescription;
    private ImageButton backButton;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private MaterialButton cancelButton, addButton;
    private ProgressDialog loadingBar;

    private Calendar cDueDate, cDueTime, cPostedDate, cPostedTime;
    private SimpleDateFormat sdfDueDate, sdfDueTime, sdfPostedDate, sdfPostedTime;
    private String saveDueDate, saveDueTime, savePostedDate, savePostedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assignment);

        initializeVariables();

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String name = assignmentName.getText().toString().trim();
                        final String description = assignmentDescription.getText().toString().trim();

                        runOnUiThread(() -> {

                            if (! name.equals("") && ! description.equals("")) {
                                addButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                addButton.setEnabled(true);
                            }

                            else {
                                addButton.setBackgroundColor(getResources().getColor(R.color.colorGray));
                                addButton.setEnabled(false);
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

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAssignmentsListReference = FirebaseDatabase.getInstance().getReference().child("Assignments List");

        assignmentName = findViewById(R.id.assignmentName);
        assignmentDescription = findViewById(R.id.editTextAssignmentDescription);
        addButton = findViewById(R.id.addAssignmentButton);
        backButton = findViewById(R.id.assignmentBackButton);
        cancelButton = findViewById(R.id.assignmentCancelButton);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);
        loadingBar = new ProgressDialog(AddAssignmentActivity.this);

        cDueDate = Calendar.getInstance();
        cDueTime = Calendar.getInstance();
        cPostedDate = Calendar.getInstance();
        cPostedTime = Calendar.getInstance();

        addButton.setOnClickListener(view -> {
            loadingBar.setTitle("Adding...");
            loadingBar.setMessage("Please wait while we are processing your request.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            savingAssignmentToFirebaseDatabase();
        });

        backButton.setOnClickListener(view -> onBackPressed());
        cancelButton.setOnClickListener(view -> onBackPressed());
    }

    private void savingAssignmentToFirebaseDatabase() {
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();
        int year = datePicker.getYear();
        cDueDate.set(year, month, day);
        sdfDueDate = new SimpleDateFormat("MMMM dd, yyyy");
        saveDueDate = sdfDueDate.format(cDueDate.getTime());

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        cDueTime.set(Calendar.HOUR, hour);
        cDueTime.set(Calendar.MINUTE, minute);
        sdfDueTime = new SimpleDateFormat("h:mm aa");
        saveDueTime = sdfDueTime.format(cDueTime.getTime());

        assignmentUniqueID = cDueDate.getTime() + "|" + cDueTime.getTime();

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fullName = dataSnapshot.child("fullName").getValue().toString();
                String name = assignmentName.getText().toString().trim();
                String description = assignmentDescription.getText().toString().trim();

                cPostedDate = Calendar.getInstance();
                sdfPostedDate = new SimpleDateFormat("MMMM dd, yyyy");
                savePostedDate = sdfPostedDate.format(cPostedDate.getTime());
                cPostedTime = Calendar.getInstance();
                sdfPostedTime = new SimpleDateFormat("h:mm aa");
                savePostedTime = sdfPostedTime.format(cPostedTime.getTime());

                HashMap assignmentMap = new HashMap();
                    assignmentMap.put("userID", currentUserID);
                    assignmentMap.put("assignmentID", assignmentUniqueID);
                    assignmentMap.put("postedDate", savePostedDate);
                    assignmentMap.put("postedTime", savePostedTime);
                    assignmentMap.put("dueDate", saveDueDate);
                    assignmentMap.put("dueTime", saveDueTime);
                    assignmentMap.put("assignmentTitle", name);
                    assignmentMap.put("assignmentDescription", description);
                    assignmentMap.put("fullName", fullName);

                if (! dataSnapshot.child("profileImage").getValue().equals("-1")) {
                    String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                    assignmentMap.put("profileImage", profileImage);
                }

                else
                    assignmentMap.put("profileImage", "-1");

                mAssignmentsListReference.child(assignmentUniqueID).updateChildren(assignmentMap)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                sendUserToMainActivity();
                                Toast.makeText(AddAssignmentActivity.this, "Your new assignment reminder was created successfully!", Toast.LENGTH_SHORT).show();
                            }

                            else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(AddAssignmentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }

                            loadingBar.dismiss();
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(AddAssignmentActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainIntent);
        finish();
    }
}