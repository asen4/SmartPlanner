package com.anubhav.assignmentsfeed;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Intent;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private FloatingActionButton editPersonProfileButton, logoutButton;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeVariables(view);

        return view;
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();

        editPersonProfileButton = view.findViewById(R.id.editPersonProfileButton);
        editPersonProfileButton.setOnClickListener(buttonView -> sendUserToEditPersonProfileActivity());

        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            sendUserToLoginFragment();
        });
    }

    private void sendUserToEditPersonProfileActivity() {
        Intent editPersonProfileIntent = new Intent(getActivity(), EditPersonProfileActivity.class);
        editPersonProfileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(editPersonProfileIntent);
    }

    private void sendUserToLoginFragment() {
        Intent loginIntent = new Intent(getActivity(), LRContainerActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }
}