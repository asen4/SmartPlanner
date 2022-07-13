package com.anubhav.assignmentsfeed;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private FirebaseAuth mAuth;
    private String currentUserID;

    private List<Comments> commentsList;
    private DatabaseReference mUsersReference;

    public CommentsAdapter(List<Comments> commentsList) {
        this.commentsList = commentsList;

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private ImageView image;
        private TextView name, comment, dateAndTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.all_comments_profile_image);
            image = itemView.findViewById(R.id.all_images);
            name = itemView.findViewById(R.id.all_comments_full_name);
            comment = itemView.findViewById(R.id.all_comments);
            dateAndTime = itemView.findViewById(R.id.commentsDateAndTime);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder commentsViewHolder, int position) {
        Comments comment = commentsList.get(position);
        String type = comment.getType();
        String fullName = comment.getFullName();

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String temp = dataSnapshot.child("fullName").getValue().toString();

                    if (fullName.equals(temp))
                        commentsViewHolder.name.setText("You");
                    else
                        commentsViewHolder.name.setText(fullName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (type.equals("text")) {
            commentsViewHolder.comment.setVisibility(View.VISIBLE);
            commentsViewHolder.image.setVisibility(View.GONE);
            commentsViewHolder.comment.setText(comment.getComment());
        }

        else {
            commentsViewHolder.comment.setVisibility(View.GONE);
            commentsViewHolder.image.setVisibility(View.VISIBLE);
            Picasso.get().load(comment.getComment()).into(commentsViewHolder.image);

            commentsViewHolder.image.setOnClickListener(v -> {
                Intent imageIntent = new Intent(commentsViewHolder.itemView.getContext(), ImageViewerActivity.class);
                imageIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                imageIntent.putExtra("URL", comment.getComment());
                commentsViewHolder.itemView.getContext().startActivity(imageIntent);
            });
        }

        if (! comment.getProfileImage().equals("-1")) {
            Picasso.get().load(comment.getProfileImage()).into(commentsViewHolder.profileImage);

            commentsViewHolder.profileImage.setOnClickListener(v -> {
                Intent imageViewerIntent = new Intent(commentsViewHolder.itemView.getContext(), ImageViewerActivity.class);
                imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                imageViewerIntent.putExtra("URL", comment.getProfileImage());
                commentsViewHolder.itemView.getContext().startActivity(imageViewerIntent);
            });
        }

        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterdayDate = dateFormat.format(calendar.getTime());

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        if (comment.getDate().equals(currentDate))
            commentsViewHolder.dateAndTime.setText(comment.getTime());

        else if (comment.getDate().equals(yesterdayDate))
            commentsViewHolder.dateAndTime.setText("yesterday\n" + comment.getTime());

        else {
            try {
                if (isDateInCurrentWeek(dateFormat.parse(comment.getDate()))) {
                    Date date = dateFormat.parse(comment.getDate());
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE");
                    commentsViewHolder.dateAndTime.setText(dateFormat2.format(date));
                }

                else
                    commentsViewHolder.dateAndTime.setText(comment.getDate());
            }

            catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    private boolean isDateInCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return week == targetWeek && year == targetYear;
    }
}
