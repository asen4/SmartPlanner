package com.anubhav.assignmentsfeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailsActivity extends AppCompatActivity {

    private ImageButton backButton;
    private CircleImageView profileImage;
    private TextView assignmentName, professorName, assignmentDescription, dueDate, dueTime, postedDate, postedTime;

    private String assignmentUniqueID;

    private DatabaseReference mAssignmentsListReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        initializeVariables();
    }

    private void initializeVariables() {
        backButton = findViewById(R.id.detailsBackButton);
        profileImage = findViewById(R.id.detailsProfileImage);
        assignmentName = findViewById(R.id.assignmentName);
        professorName = findViewById(R.id.professorName);
        assignmentDescription = findViewById(R.id.assignmentDescription);
        dueDate = findViewById(R.id.assignmentDueDate);
        dueTime = findViewById(R.id.assignmentDueTime);
        postedDate = findViewById(R.id.assignmentPostedDate);
        postedTime = findViewById(R.id.assignmentPostedTime);

        assignmentUniqueID = getIntent().getStringExtra("assignmentUniqueID");

        backButton.setOnClickListener(view -> onBackPressed());

        mAssignmentsListReference = FirebaseDatabase.getInstance().getReference().child("Assignments List");
        mAssignmentsListReference.child(assignmentUniqueID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String assignmentTitle = snapshot.child("assignmentTitle").getValue().toString();
                String sAssignmentDescription = snapshot.child("assignmentDescription").getValue().toString();
                String sDueDate = snapshot.child("dueDate").getValue().toString();
                String sDueTime = snapshot.child("dueTime").getValue().toString();
                String fullName = snapshot.child("fullName").getValue().toString();
                String sPostedDate = snapshot.child("postedDate").getValue().toString();
                String sPostedTime = snapshot.child("postedTime").getValue().toString();
                String sProfileImage = snapshot.child("profileImage").getValue().toString();

                if (! sProfileImage.equals("-1")) {
                    Picasso.get().load(sProfileImage).placeholder(R.drawable.ic_baseline_person_black_125).into(profileImage);

                    profileImage.setOnClickListener(view -> {
                        Intent imageViewerIntent = new Intent(DetailsActivity.this, ImageViewerActivity.class);
                        imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        imageViewerIntent.putExtra("URL", sProfileImage);
                        startActivity(imageViewerIntent);
                    });
                }

                assignmentName.setText(assignmentTitle);
                assignmentDescription.setText("Description: " + sAssignmentDescription);
                dueDate.setText("Due Date: " + sDueDate);
                dueTime.setText("Due Time: " + sDueTime);
                professorName.setText(fullName);
                postedDate.setText("Posted Date: " + sPostedDate);
                postedTime.setText("Posted Time: " + sPostedTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}