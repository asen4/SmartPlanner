package com.anubhav.assignmentsfeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment {

    private AppCompatButton mGoogleSignInButton;
    private MaterialButton mButtonLogin;
    private EditText mEditTextEmailAddress, mEditTextPassword;

    private FirebaseAuth mAuth;
    private boolean isEmailAddressVerified;

    private ProgressDialog loadingBar;

    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initializeVariables(view);

        mButtonLogin.setOnClickListener(v -> {
            String emailAddress = mEditTextEmailAddress.getText().toString().trim();
            String password = mEditTextPassword.getText().toString().trim();

            loadingBar.setTitle("Logging in...");
            loadingBar.setMessage("Please wait while we are processing your request.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(emailAddress, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            verifyEmailAddress();
                            loadingBar.dismiss();
                        }

                        else {
                            String errorMessage = task.getException().getMessage();
                            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                    .setTitle("Login Error!")
                                    .setMessage(errorMessage)
                                    .setPositiveButton("Ok", null)
                                    .setNegativeButton("Cancel", null)
                                    .show();
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

                        final String mETUsername = mEditTextEmailAddress.getText().toString().trim();
                        final String mETPassword = mEditTextPassword.getText().toString().trim();

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (! mETUsername.isEmpty() && ! mETPassword.isEmpty()) {
                                    mButtonLogin.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                                    mButtonLogin.setEnabled(true);
                                }

                                else {
                                    mButtonLogin.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));
                                    mButtonLogin.setEnabled(false);
                                }

                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("901919795087-bnj79ur81l88f2plsh7mba0tiqpqhpdl.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), 0, connectionResult -> Toast.makeText(getActivity(), "Your connection to Google Sign In failed!", Toast.LENGTH_SHORT).show())

                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSignInButton.setOnClickListener(v -> signIn());

        return view;
    }

    private void initializeVariables(View view) {
        mButtonLogin = view.findViewById(R.id.cirLoginButton);
        mButtonLogin.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();

        mGoogleSignInButton = view.findViewById(R.id.GoogleBtnLogin);

        mEditTextEmailAddress = view.findViewById(R.id.editTextEmailAddress);
        mEditTextEmailAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_email_24, 0, 0, 0);

        mEditTextPassword = view.findViewById(R.id.editTextPassword);
        mEditTextPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);

        loadingBar = new ProgressDialog(getActivity());
    }

    private void verifyEmailAddress() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        isEmailAddressVerified = currentUser.isEmailVerified();

        if (isEmailAddressVerified) {
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
        }

        else {
            Toast.makeText(getActivity(), "Please verify your account first by checking your email!", Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        mGoogleSignInClient.clearDefaultAccountAndReconnect();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            loadingBar.setTitle("Signing in with Google...");
            loadingBar.setMessage("Please wait while we are processing your request.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }

            else {
                Toast.makeText(getActivity(), "Error! Please try again later!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }

                    else {
                        String errorMessage = task.getException().toString();
                        startActivity(new Intent(getActivity(), LRContainerActivity.class));
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    loadingBar.dismiss();
                });
    }
}