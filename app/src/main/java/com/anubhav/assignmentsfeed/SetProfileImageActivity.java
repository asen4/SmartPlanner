package com.anubhav.assignmentsfeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;

public class SetProfileImageActivity extends AppCompatActivity {

    private ImageView profileImage;
    private ProgressDialog loadingBar;
    private MaterialButton submitButton;

    private FirebaseAuth mAuth;
    private String currentUserID, downloadUrl;
    private DatabaseReference mUsersReference;
    private StorageReference mUserProfileImageReference;

    private final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_image);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID =  mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mUserProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        downloadUrl = "-1";

        loadingBar = new ProgressDialog(SetProfileImageActivity.this);

        profileImage = findViewById(R.id.setupProfileImage);

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String image = dataSnapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_black_200).into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profileImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_PICK);
        });

        submitButton = findViewById(R.id.profileImageButton);
        submitButton.setOnClickListener(v -> saveAccountProfileImageInformation());
    }

    private void saveAccountProfileImageInformation() {
        loadingBar.setTitle("Saving image...");
        loadingBar.setMessage("Please wait while we are processing your request.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        HashMap userMap = new HashMap();
            userMap.put("profileImage", downloadUrl);

        mUsersReference.updateChildren(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendUserToSetupActivity();
                Toast.makeText(SetProfileImageActivity.this, "Your profile image was created successfully!", Toast.LENGTH_SHORT).show();
            }

            else {
                String errorMessage = task.getException().toString();
                Toast.makeText(SetProfileImageActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
                    .start(SetProfileImageActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Setting profile image...");
                loadingBar.setMessage("Please wait while we are processing your request.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

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

                                        Toast.makeText(SetProfileImageActivity.this, "Your profile image was saved successfully!", Toast.LENGTH_LONG).show();
                                        Picasso.get().load(downloadUrl).placeholder(R.drawable.ic_baseline_person_black_200).into(profileImage);
                                        submitButton.setText("Continue");
                                        loadingBar.dismiss();
                                    });
                                }
                            }
                        });

                    }
                });
            }

            else {
                Toast.makeText(SetProfileImageActivity.this, "Your image cannot be cropped! Please try again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(SetProfileImageActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}