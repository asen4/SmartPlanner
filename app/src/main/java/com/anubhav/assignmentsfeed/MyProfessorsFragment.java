package com.anubhav.assignmentsfeed;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfessorsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference mUsersReference, mProfessorsReference;

    private CardView notificationMessageForProfessors;
    private TextInputEditText searchProfessorsBar;
    private RecyclerView listOfProfessors;
    private TextView noResultsFound;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_professors, container, false);

        initializeVariables(view);

        searchForProfessors();

        return view;
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mProfessorsReference = FirebaseDatabase.getInstance().getReference().child("Professors");

        notificationMessageForProfessors = view.findViewById(R.id.myProfessorsCardViewInstructions);
        listOfProfessors = view.findViewById(R.id.listOfProfessors);
        listOfProfessors.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        listOfProfessors.addItemDecoration(itemDecoration);
        searchProfessorsBar = view.findViewById(R.id.editTextSearchContacts);
        noResultsFound = view.findViewById(R.id.professorsNoResultsFound);

        searchProfessorsBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchForProfessors();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String personType = snapshot.child("personType").getValue().toString();
                if (personType.equals("Professor")) {
                    notificationMessageForProfessors.setVisibility(View.VISIBLE);
                    listOfProfessors.setVisibility(View.GONE);
                    searchProfessorsBar.setVisibility(View.GONE);
                }

                else {
                    notificationMessageForProfessors.setVisibility(View.GONE);
                    listOfProfessors.setVisibility(View.VISIBLE);
                    searchProfessorsBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mProfessorsReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    noResultsFound.setVisibility(View.GONE);
                else
                    noResultsFound.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchForProfessors() {
        FirebaseRecyclerOptions<Professor> professorFirebaseRecyclerOptions;

        if (searchProfessorsBar.getText().toString().equals("")) {
            professorFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Professor>()
                    .setQuery(mProfessorsReference, Professor.class)
                    .build();
        }

        else {
            professorFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Professor>()
                    .setQuery(mProfessorsReference.orderByChild("fullName").startAt(searchProfessorsBar.getText().toString()).endAt(searchProfessorsBar.getText().toString() + "\uf8ff"), Professor.class)
                    .build();
        }

        Query searchProfessorsQuery = mProfessorsReference.orderByChild("fullName")
                .startAt(searchProfessorsBar.getText().toString()).endAt(searchProfessorsBar.getText().toString() + "\uf8ff");

        searchProfessorsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    noResultsFound.setVisibility(View.GONE);

                else
                    noResultsFound.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerAdapter<Professor, ProfessorsViewHolder> professorFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Professor, ProfessorsViewHolder>(professorFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ProfessorsViewHolder professorsViewHolder, final int position, @NonNull Professor professor) {
                final String receiverUserID = getRef(position).getKey();

                professorsViewHolder.materialCardView.setOnClickListener(view -> {
                    Intent personProfileIntent = new Intent(getActivity(), PersonProfileActivity.class);
                    personProfileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    personProfileIntent.putExtra("personKey", receiverUserID);
                    startActivity(personProfileIntent);
                });

                mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fullName = snapshot.child("fullName").getValue().toString();
                        professorsViewHolder.fullName.setText(fullName);

                        String profileStatus = snapshot.child("profileStatus").getValue().toString();
                        professorsViewHolder.status.setText(profileStatus);

                        String profileImage = snapshot.child("profileImage").getValue().toString();
                        if (! profileImage.equals("-1")) {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_75).into(professorsViewHolder.profileImage);

                            professorsViewHolder.profileImage.setOnClickListener(view -> {
                                Intent imageViewerIntent = new Intent(getActivity(), ImageViewerActivity.class);
                                imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                imageViewerIntent.putExtra("URL", profileImage);
                                startActivity(imageViewerIntent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ProfessorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
                return new ProfessorsViewHolder(view);
            }
        };

        listOfProfessors.setAdapter(professorFirebaseRecyclerAdapter);
        professorFirebaseRecyclerAdapter.startListening();
    }

    public static class ProfessorsViewHolder extends RecyclerView.ViewHolder {

        private MaterialCardView materialCardView;
        private CircleImageView profileImage;
        private TextView fullName, status;

        public ProfessorsViewHolder(View itemView) {
            super(itemView);

            materialCardView = itemView.findViewById(R.id.professorContainer);
            profileImage = itemView.findViewById(R.id.allProfessorsProfileImage);
            fullName = itemView.findViewById(R.id.allProfessorsFullName);
            status = itemView.findViewById(R.id.allPeopleStatus);
        }
    }
}