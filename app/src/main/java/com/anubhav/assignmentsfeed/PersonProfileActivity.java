package com.anubhav.assignmentsfeed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private String currentUserID, receiverUserID, saveCurrentDate, saveCurrentTime, commentID, myURL;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference, mUsersReference, mUserRatingsReference, mMyProfessorsReference, mMyStudentsReference;

    private CircleImageView profileImage;
    private ImageButton backButton, addImageButton, sendCommentButton;
    private TextView headerName, fullName, emailAddress, schoolName, profileStatus, starRatings, howWouldYouRateTheUser, noCommentsYetMessage, myProfessorsOrStudentsHeader, noProfessorsYetMessage, noStudentsYetMessage;

    private AppCompatRatingBar ratingBar;
    private ProgressBar loadingBar;

    private ArrayList<Comments> commentsList = new ArrayList<>();
    private CommentsAdapter commentsAdapter;
    private RecyclerView listOfUsers, listOfComments;
    private TextInputEditText inputComment;

    private StorageTask uploadTask;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        initializeVariables();

        fetchComments();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        receiverUserID = getIntent().getStringExtra("personKey");

        mRootReference = FirebaseDatabase.getInstance().getReference();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserRatingsReference = FirebaseDatabase.getInstance().getReference().child("User Ratings");
        mMyProfessorsReference = FirebaseDatabase.getInstance().getReference().child("My Professors");
        mMyStudentsReference = FirebaseDatabase.getInstance().getReference().child("My Students");

        myProfessorsOrStudentsHeader = findViewById(R.id.popMyProfessors);
        noProfessorsYetMessage = findViewById(R.id.popNoProfessorsYetMessage);
        noStudentsYetMessage = findViewById(R.id.popNoStudentsYetMessage);
        listOfUsers = findViewById(R.id.popListOfUsers);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(PersonProfileActivity.this, LinearLayoutManager.HORIZONTAL);
        listOfUsers.addItemDecoration(dividerItemDecoration);
        listOfUsers.setLayoutManager(new LinearLayoutManager(PersonProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

        headerName = findViewById(R.id.personProfileHeaderName);
        backButton = findViewById(R.id.personProfileBackButton);
        profileImage = findViewById(R.id.personProfileImage);
        fullName = findViewById(R.id.personProfileFullName);
        emailAddress = findViewById(R.id.personProfileEmailAddress);
        schoolName = findViewById(R.id.personSchoolName);
        profileStatus = findViewById(R.id.personProfileStatus);
        starRatings = findViewById(R.id.starRatings);
        ratingBar = findViewById(R.id.ratingBar);
        howWouldYouRateTheUser = findViewById(R.id.howWouldYouRateTheUser);

        backButton.setOnClickListener(view -> onBackPressed());
        loadingBar = findViewById(R.id.commentsLoadingBar);

        commentsAdapter = new CommentsAdapter(commentsList);
        listOfComments = findViewById(R.id.commentsList);
        listOfComments.setHasFixedSize(true);
        listOfComments.setAdapter(commentsAdapter);
        listOfComments.setLayoutManager(new LinearLayoutManager(PersonProfileActivity.this));

        noCommentsYetMessage = findViewById(R.id.noCommentsToDisplayMessage);
        sendCommentButton = findViewById(R.id.sendCommentButton);
        inputComment = findViewById(R.id.inputComment);
        inputComment.setMaxHeight(100);

        mRootReference.child("Comments").child(receiverUserID).addValueEventListener(new ValueEventListener() {
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

        sendCommentButton.setOnClickListener(v -> sendComment());

        addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
        });

        inputComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    sendCommentButton.setBackground(getResources().getDrawable(R.drawable.gray_round_button));
                    sendCommentButton.setEnabled(false);
                }

                else {
                    sendCommentButton.setBackground(getResources().getDrawable(R.drawable.accent_round_button));
                    sendCommentButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pI = snapshot.child("profileImage").getValue().toString();
                String fN = snapshot.child("fullName").getValue().toString();
                String eA = snapshot.child("emailAddress").getValue().toString();
                String pS = snapshot.child("profileStatus").getValue().toString();
                String pT = snapshot.child("personType").getValue().toString();
                String sN = snapshot.child("schoolName").getValue().toString();

                if (pT.equals("Student")) {
                    howWouldYouRateTheUser.setText("How would you rate this student?");
                    myProfessorsOrStudentsHeader.setText(fN + "'s Professors");

                    mMyProfessorsReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
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

                else {
                    myProfessorsOrStudentsHeader.setText(fN + "'s Students");

                    mMyStudentsReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
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

                ratingBar.setOnRatingBarChangeListener((ratingBar, v, b) -> {
                    float numOfStars = ratingBar.getRating();
                    mUserRatingsReference.child(fN).child(currentUserID).child("numberOfStars").setValue(numOfStars);
                });

                mUserRatingsReference.child(fN).addValueEventListener(new ValueEventListener() {
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

                mUserRatingsReference.child(fN).child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("numberOfStars")) {
                            float numOfStars = Float.valueOf(snapshot.child("numberOfStars").getValue().toString());
                            ratingBar.setRating(numOfStars);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if (! pI.equals("-1")) {
                    Picasso.get().load(pI).placeholder(R.drawable.ic_baseline_person_90).into(profileImage);

                    profileImage.setOnClickListener(view -> {
                        Intent imageViewerIntent = new Intent(PersonProfileActivity.this, ImageViewerActivity.class);
                        imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        imageViewerIntent.putExtra("URL", pI);
                        startActivity(imageViewerIntent);
                    });
                }

                fullName.setText(fN);
                emailAddress.setText(eA);
                profileStatus.setText(pS);
                headerName.setText(fN + "'s Profile");
                schoolName.setText(sN);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadingBar.setVisibility(View.VISIBLE);
            fileUri = data.getData();

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("EEE, MMM dd, yyyy");
            saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
            saveCurrentTime = currentTime.format(callForTime.getTime());

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

            DatabaseReference userCommentKey = mRootReference.child("Comments").child(receiverUserID).push();

            final String commentPushID = userCommentKey.getKey();

            final StorageReference filePath = storageReference.child(commentPushID + ".jpg");

            uploadTask = filePath.putFile(fileUri);

            uploadTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful())
                    throw task.getException();

                return filePath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadURL = (Uri) task.getResult();
                    myURL = downloadURL.toString();

                    mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String currentUserFullName = snapshot.child("fullName").getValue().toString();
                            String currentUserProfileImage = snapshot.child("profileImage").getValue().toString();

                            Map commentMap = new HashMap();
                                commentMap.put("comment", myURL);
                                commentMap.put("commentID", commentPushID);
                                commentMap.put("date", saveCurrentDate);
                                commentMap.put("time", saveCurrentTime);
                                commentMap.put("fullName", currentUserFullName);
                                commentMap.put("profileImage", currentUserProfileImage);
                                commentMap.put("from", currentUserID);
                                commentMap.put("recipient", receiverUserID);
                                commentMap.put("type", "image");

                            mRootReference.child("Comments").child(receiverUserID).child(commentPushID).updateChildren(commentMap).addOnCompleteListener(commentsTask -> {
                                if (! commentsTask.isSuccessful()) {
                                    String errorMessage = commentsTask.getException().getMessage();
                                    Toast.makeText(PersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }

                                inputComment.setText("");
                                loadingBar.setVisibility(View.GONE);
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }
    }

    private void fetchComments() {
        mRootReference.child("Comments").child(receiverUserID)
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

    private void sendComment() {
        final String comment = inputComment.getText().toString().trim();

        DatabaseReference pushedCommentReference = mRootReference.child("Comments").child(receiverUserID).push();
        commentID = pushedCommentReference.getKey();

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentUserFullName = dataSnapshot.child("fullName").getValue().toString();

                    Map commentMap = new HashMap();
                        commentMap.put("comment", comment);
                        commentMap.put("commentID", commentID);
                        commentMap.put("date", saveCurrentDate);
                        commentMap.put("time", saveCurrentTime);
                        commentMap.put("fullName", currentUserFullName);
                        commentMap.put("from", currentUserID);
                        commentMap.put("recipient", receiverUserID);
                        commentMap.put("type", "text");

                    String profileImage = dataSnapshot.child("profileImage").getValue().toString();

                    if (! profileImage.equals("-1"))
                        commentMap.put("profileImage", profileImage);
                    else
                        commentMap.put("profileImage", "-1");

                    mRootReference.child("Comments").child(receiverUserID).child(commentID).updateChildren(commentMap).addOnCompleteListener(task -> {
                        if (! task.isSuccessful()) {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(PersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        inputComment.setText("");
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayAllOfMyStudents() {
        FirebaseRecyclerOptions<Professor> studentsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Professor>()
                .setQuery(mMyStudentsReference.child(receiverUserID), Professor.class)
                .build();

        FirebaseRecyclerAdapter<Professor, EditPersonProfileActivity.ProfessorsViewHolder> professorsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Professor, EditPersonProfileActivity.ProfessorsViewHolder>(studentsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final EditPersonProfileActivity.ProfessorsViewHolder professorsViewHolder, int position, @NonNull final Professor professor) {
                String personKey = getRef(position).getKey();

                mUsersReference.child(personKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fullName = dataSnapshot.child("fullName").getValue().toString();
                        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String temp = snapshot.child("fullName").getValue().toString();
                                if (fullName.equals(temp))
                                    professorsViewHolder.fullName.setText("You");

                                else {
                                    professorsViewHolder.fullName.setText(fullName);

                                    professorsViewHolder.itemView.setOnClickListener(view -> {
                                        Intent personProfileIntent = new Intent(PersonProfileActivity.this, PersonProfileActivity.class);
                                        personProfileIntent.putExtra("personKey", personKey);
                                        startActivity(personProfileIntent);
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                        if (! profileImage.equals("-1"))
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_75).into(professorsViewHolder.profileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public EditPersonProfileActivity.ProfessorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_professors_display_layout, parent, false);
                return new EditPersonProfileActivity.ProfessorsViewHolder(view);
            }
        };

        listOfUsers.setAdapter(professorsFirebaseRecyclerAdapter);
        professorsFirebaseRecyclerAdapter.startListening();
    }

    private void displayAllOfMyProfessors() {
        FirebaseRecyclerOptions<Professor> professorsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Professor>()
                .setQuery(mMyProfessorsReference.child(receiverUserID), Professor.class)
                .build();

        FirebaseRecyclerAdapter<Professor, EditPersonProfileActivity.ProfessorsViewHolder> professorsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Professor, EditPersonProfileActivity.ProfessorsViewHolder>(professorsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final EditPersonProfileActivity.ProfessorsViewHolder professorsViewHolder, int position, @NonNull final Professor professor) {
                String personKey = getRef(position).getKey();

                mUsersReference.child(personKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fullName = dataSnapshot.child("fullName").getValue().toString();
                        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String temp = snapshot.child("fullName").getValue().toString();
                                if (fullName.equals(temp))
                                    professorsViewHolder.fullName.setText("You");

                                else {
                                    professorsViewHolder.fullName.setText(fullName);

                                    professorsViewHolder.itemView.setOnClickListener(view -> {
                                        Intent personProfileIntent = new Intent(PersonProfileActivity.this, PersonProfileActivity.class);
                                        personProfileIntent.putExtra("personKey", personKey);
                                        startActivity(personProfileIntent);
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                        if (! profileImage.equals("-1"))
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_75).into(professorsViewHolder.profileImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                professorsViewHolder.itemView.setOnClickListener(view -> {
                    Intent personProfileIntent = new Intent(PersonProfileActivity.this, PersonProfileActivity.class);
                    personProfileIntent.putExtra("personKey", personKey);
                    startActivity(personProfileIntent);
                });
            }

            @NonNull
            @Override
            public EditPersonProfileActivity.ProfessorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_professors_display_layout, parent, false);
                return new EditPersonProfileActivity.ProfessorsViewHolder(view);
            }
        };

        listOfUsers.setAdapter(professorsFirebaseRecyclerAdapter);
        professorsFirebaseRecyclerAdapter.startListening();
    }
}