package com.anubhav.assignmentsfeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPersonProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference, mUsersReference, mMyProfessorsReference, mMyStudentsReference, mUserRatingsReference;
    private StorageReference mUserProfileImageReference;
    private String currentUserID, downloadUrl;

    private CircleImageView profileImage;
    private ImageButton backButton;
    private MaterialButton saveButton;
    private ProgressBar profileImageLoadingBar;
    private ProgressDialog loadingBar;
    private TextInputEditText firstName, lastName, emailAddress, personType, profileStatus, starRatings;
    private TextView noProfessorsYetMessage, noStudentsYetMessage, myProfessorsOrStudentsHeader, noCommentsYetMessage;
    private RecyclerView listOfUsers, listOfComments;

    private ArrayList<Comments> commentsList = new ArrayList<>();
    private CommentsAdapter commentsAdapter;

    private final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person_profile);

        initializeVariables();

        fetchComments();

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String personType = snapshot.child("personType").getValue().toString();
                if (personType.equals("Professor")) {
                    mMyStudentsReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                listOfUsers.setVisibility(View.VISIBLE);
                                noStudentsYetMessage.setVisibility(View.GONE);
                                displayAllOfMyStudents();
                            }

                            else {
                                listOfUsers.setVisibility(View.GONE);
                                noStudentsYetMessage.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                else {
                    mMyProfessorsReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                listOfUsers.setVisibility(View.VISIBLE);
                                noProfessorsYetMessage.setVisibility(View.GONE);
                                displayAllOfMyProfessors();
                            }

                            else {
                                listOfUsers.setVisibility(View.GONE);
                                noProfessorsYetMessage.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserRatingsReference = FirebaseDatabase.getInstance().getReference().child("User Ratings");
        mMyProfessorsReference = FirebaseDatabase.getInstance().getReference().child("My Professors");
        mMyStudentsReference = FirebaseDatabase.getInstance().getReference().child("My Students");
        mUserProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        currentUserID = mAuth.getCurrentUser().getUid();

        myProfessorsOrStudentsHeader = findViewById(R.id.myProfessors);

        profileImage = findViewById(R.id.editPersonProfileImage);
        profileImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_PICK);
        });

        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        emailAddress = findViewById(R.id.editTextEmailAddress);
        personType = findViewById(R.id.editPersonType);
        profileStatus = findViewById(R.id.editPersonProfileStatus);
        starRatings = findViewById(R.id.editPersonProfileRatings);
        noProfessorsYetMessage = findViewById(R.id.noProfessorsYetMessage);
        noStudentsYetMessage = findViewById(R.id.noStudentsYetMessage);

        commentsAdapter = new CommentsAdapter(commentsList);
        listOfComments = findViewById(R.id.personProfileListOfComments);
        listOfComments.setHasFixedSize(true);
        listOfComments.setAdapter(commentsAdapter);
        listOfComments.setLayoutManager(new LinearLayoutManager(EditPersonProfileActivity.this));

        noCommentsYetMessage = findViewById(R.id.noCommentsYetMessage);

        mRootReference.child("Comments").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    noCommentsYetMessage.setVisibility(View.GONE);
                    listOfComments.setVisibility(View.VISIBLE);
                }

                else {
                    noCommentsYetMessage.setVisibility(View.VISIBLE);
                    listOfComments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listOfUsers = findViewById(R.id.personProfileListOfProfessors);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(EditPersonProfileActivity.this, LinearLayoutManager.HORIZONTAL);
        listOfUsers.addItemDecoration(dividerItemDecoration);
        listOfUsers.setLayoutManager(new LinearLayoutManager(EditPersonProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

        loadingBar = new ProgressDialog(EditPersonProfileActivity.this);
        profileImageLoadingBar = findViewById(R.id.editPersonProfileLoadingBar);

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profileI = dataSnapshot.child("profileImage").getValue().toString();
                downloadUrl = profileI;

                String fullName = dataSnapshot.child("fullName").getValue().toString();
                int spaceIndex = fullName.indexOf(" ");
                String firstN = fullName.substring(0, spaceIndex);
                String lastN = fullName.substring(spaceIndex + 1);
                String emailA = dataSnapshot.child("emailAddress").getValue().toString();
                String personT = dataSnapshot.child("personType").getValue().toString();
                String profileS = dataSnapshot.child("profileStatus").getValue().toString();

                if (personT.equals("Professor"))
                    myProfessorsOrStudentsHeader.setText("My Students");

                else
                    myProfessorsOrStudentsHeader.setText("My Professors");

                firstName.setText(firstN);
                lastName.setText(lastN);
                emailAddress.setText(emailA);
                personType.setText(personT);
                profileStatus.setText(profileS);

                if (! profileI.equals("-1"))
                    Picasso.get().load(profileI).placeholder(R.drawable.ic_baseline_person_black_125).into(profileImage);

                mUserRatingsReference.child(fullName).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double sum = 0;
                        DecimalFormat format = new DecimalFormat();
                        format.setDecimalSeparatorAlwaysShown(false);

                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren())
                                sum += Double.parseDouble(childSnapshot.child("numberOfStars").getValue().toString());

                            starRatings.setText(format.format(sum / snapshot.getChildrenCount()) + "/5");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveButton = findViewById(R.id.cirSaveButton);
        saveButton.setOnClickListener(view -> saveInformation());

        backButton = findViewById(R.id.editPersonProfileBackBtn);
        backButton.setOnClickListener(view -> onBackPressed());

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String mETFirstName = firstName.getText().toString().trim();
                        final String mETLastName = lastName.getText().toString().trim();
                        final String mETProfileStatus = profileStatus.getText().toString().trim();

                        EditPersonProfileActivity.this.runOnUiThread(() -> {
                            if (! mETFirstName.isEmpty() && ! mETLastName.isEmpty() && ! mETProfileStatus.isEmpty()) {
                                saveButton.setBackgroundTintList(EditPersonProfileActivity.this.getColorStateList(R.color.colorAccent));
                                saveButton.setEnabled(true);
                            }

                            else {
                                saveButton.setBackgroundTintList(EditPersonProfileActivity.this.getColorStateList(R.color.colorGray));
                                saveButton.setEnabled(false);
                            }
                        });
                    }

                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    private void saveInformation() {
        loadingBar.setTitle("Saving...");
        loadingBar.setMessage("Please wait while we are processing your request.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        String firstNameET = firstName.getText().toString().trim();
        String revisedFirstNameET  = firstNameET.substring(0, 1).toUpperCase() + firstNameET.substring(1).toLowerCase();
        String lastNameET = lastName.getText().toString().trim();
        String revisedLastNameET = lastNameET.substring(0, 1).toUpperCase() + lastNameET.substring(1).toLowerCase();

        HashMap userMap = new HashMap();
            userMap.put("fullName", revisedFirstNameET + " " + revisedLastNameET);
            userMap.put("profileImage", downloadUrl);
            userMap.put("profileStatus", profileStatus.getText().toString().trim());

        mUsersReference.child(currentUserID).updateChildren(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendUserToMainActivity();
                Toast.makeText(EditPersonProfileActivity.this, "Your changes were saved successfully!", Toast.LENGTH_LONG).show();
            }

            else {
                String errorMessage = task.getException().toString();
                Toast.makeText(EditPersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }

            loadingBar.dismiss();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(EditPersonProfileActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Setting...");
                loadingBar.setMessage("Please wait while we are processing your request.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                profileImageLoadingBar.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();
                StorageReference filePath = mUserProfileImageReference.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.addOnSuccessListener(taskSnapshot -> {
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result1 = taskSnapshot.getStorage().getDownloadUrl();
                                    result1.addOnSuccessListener(uri -> {
                                        downloadUrl = uri.toString();

                                        Toast.makeText(EditPersonProfileActivity.this, "Your profile image was cropped successfully!", Toast.LENGTH_LONG).show();
                                        Picasso.get().load(downloadUrl).into(profileImage, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                profileImageLoadingBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError(Exception e) {

                                            }
                                        });

                                        loadingBar.dismiss();
                                    });
                                }
                            }
                        });
                    }

                    else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(EditPersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                });
            }

            else {
                Toast.makeText(EditPersonProfileActivity.this, "Your image cannot be cropped! Please try again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }

    private void displayAllOfMyStudents() {
        FirebaseRecyclerOptions<Professor> studentsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Professor>()
                .setQuery(mMyStudentsReference.child(currentUserID), Professor.class)
                .build();

        FirebaseRecyclerAdapter<Professor, ProfessorsViewHolder> professorsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Professor, ProfessorsViewHolder>(studentsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ProfessorsViewHolder professorsViewHolder, int position, @NonNull final Professor professor) {
                String personKey = getRef(position).getKey();

                mUsersReference.child(personKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fullName = dataSnapshot.child("fullName").getValue().toString();
                        professorsViewHolder.fullName.setText(fullName);

                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                        if (! profileImage.equals("-1"))
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_75).into(professorsViewHolder.profileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                professorsViewHolder.itemView.setOnClickListener(view -> {
                    Intent personProfileIntent = new Intent(EditPersonProfileActivity.this, PersonProfileActivity.class);
                    personProfileIntent.putExtra("personKey", personKey);
                    personProfileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(personProfileIntent);
                });
            }

            @NonNull
            @Override
            public ProfessorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_professors_display_layout, parent, false);
                return new ProfessorsViewHolder(view);
            }
        };

        listOfUsers.setAdapter(professorsFirebaseRecyclerAdapter);
        professorsFirebaseRecyclerAdapter.startListening();
    }

    private void displayAllOfMyProfessors() {
        FirebaseRecyclerOptions<Professor> professorsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Professor>()
                .setQuery(mMyProfessorsReference.child(currentUserID), Professor.class)
                .build();

        FirebaseRecyclerAdapter<Professor, ProfessorsViewHolder> professorsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Professor, ProfessorsViewHolder>(professorsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ProfessorsViewHolder professorsViewHolder, int position, @NonNull final Professor professor) {
                String personKey = getRef(position).getKey();

                mUsersReference.child(personKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fullName = dataSnapshot.child("fullName").getValue().toString();
                        professorsViewHolder.fullName.setText(fullName);

                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                        if (! profileImage.equals("-1"))
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_75).into(professorsViewHolder.profileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                professorsViewHolder.itemView.setOnClickListener(view -> {
                    Intent personProfileIntent = new Intent(EditPersonProfileActivity.this, PersonProfileActivity.class);
                    personProfileIntent.putExtra("personKey", personKey);
                    personProfileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(personProfileIntent);
                });
            }

            @NonNull
            @Override
            public ProfessorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_professors_display_layout, parent, false);
                return new ProfessorsViewHolder(view);
            }
        };

        listOfUsers.setAdapter(professorsFirebaseRecyclerAdapter);
        professorsFirebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static class ProfessorsViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profileImage;
        public TextView fullName;

        public ProfessorsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.professorProfileImage);
            fullName = itemView.findViewById(R.id.professorFullName);
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(EditPersonProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void fetchComments() {
        mRootReference.child("Comments").child(currentUserID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()) {
                            Comments comment = dataSnapshot.getValue(Comments.class);
                            commentsList.add(comment);
                            commentsAdapter.notifyDataSetChanged();
                            listOfComments.smoothScrollToPosition(listOfComments.getAdapter().getItemCount());
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}