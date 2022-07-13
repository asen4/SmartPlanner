package com.anubhav.assignmentsfeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterFragment extends Fragment {

    private boolean mCheckpointEmailAddress, mCheckpointPassword, mCheckpointConfirmPassword;
    private AppCompatButton mGoogleSignInButton;
    private MaterialButton mButtonRegister;
    private EditText mEditTextEmailAddress, mEditTextPassword, mEditTextConfirmPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private TextInputLayout mTextInputLayoutEmailAddress, mTextInputLayoutPassword, mTextInputLayoutConfirmPassword;

    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        initializeVariables(view);

        mButtonRegister.setOnClickListener(v -> {
                    String emailAddress = mEditTextEmailAddress.getText().toString().trim();
                    String password = mEditTextPassword.getText().toString().trim();

                    if (mCheckpointEmailAddress && mCheckpointPassword && mCheckpointConfirmPassword) {
                        loadingBar.setTitle("Registering...");
                        loadingBar.setMessage("Please wait while we are processing your request.");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        mAuth.createUserWithEmailAndPassword(emailAddress, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        sendEmailVerificationMessage();
                                        loadingBar.dismiss();
                                    }

                                    else {
                                        String errorMessage = task.getException().getMessage();
                                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                                .setTitle("Registration Error!")
                                                .setMessage(errorMessage)
                                                .setPositiveButton("Ok", null)
                                                .setNegativeButton("Cancel", null)
                                                .show();
                                        loadingBar.dismiss();
                                    }
                                });
                    }
                }
        );

        mEditTextEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    mTextInputLayoutEmailAddress.setErrorEnabled(false);
                    mCheckpointEmailAddress = false;
                }

                else if (! validateEmail(s.toString())) {
                    mTextInputLayoutEmailAddress.setErrorEnabled(true);
                    mTextInputLayoutEmailAddress.setError("Please enter a valid email address!");
                    mCheckpointEmailAddress = false;
                }

                else {
                    mTextInputLayoutEmailAddress.setErrorEnabled(false);
                    mCheckpointEmailAddress = true;
                }
            }
        });

        mEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    mTextInputLayoutPassword.setErrorEnabled(false);
                    mCheckpointPassword = false;
                }

                else if (s.toString().length() < 8) {
                    mTextInputLayoutPassword.setErrorEnabled(true);
                    mTextInputLayoutPassword.setError("Your password is too short! The minimum length is 8 characters.");
                    mCheckpointPassword = false;
                }

                else if (s.toString().length() > 10) {
                    mTextInputLayoutPassword.setErrorEnabled(true);
                    mTextInputLayoutPassword.setError("Your password is too long! The maximum length is 10 characters.");
                    mCheckpointPassword = false;
                }

                else {
                    mTextInputLayoutPassword.setErrorEnabled(false);
                    mCheckpointPassword = true;
                }
            }
        });

        mEditTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    mTextInputLayoutConfirmPassword.setErrorEnabled(false);
                    mCheckpointConfirmPassword = false;
                }

                else if (! mEditTextPassword.getText().toString().equals(s.toString())) {
                    mTextInputLayoutConfirmPassword.setErrorEnabled(true);
                    mTextInputLayoutConfirmPassword.setError("Your passwords are not matching each other!");
                    mCheckpointConfirmPassword = false;
                }

                else {
                    mTextInputLayoutConfirmPassword.setErrorEnabled(false);
                    mCheckpointConfirmPassword = true;
                }
            }
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String mETEmailAddress = mEditTextEmailAddress.getText().toString().trim();
                        final String mETPassword = mEditTextPassword.getText().toString().trim();
                        final String mETConfirmPassword = mEditTextConfirmPassword.getText().toString().trim();

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {

                                if (validateEmail(mETEmailAddress) && mETPassword.length() >= 8 && mETConfirmPassword.length() >= 8 && mETPassword.equals(mETConfirmPassword)) {
                                    mButtonRegister.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                                    mButtonRegister.setEnabled(true);
                                }

                                else {
                                    mButtonRegister.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));
                                    mButtonRegister.setEnabled(false);
                                }
                            });
                        }
                    }

                    catch (InterruptedException e) {
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
                .enableAutoManage(getActivity(), 1, connectionResult -> Toast.makeText(getActivity(), "Your connection to Google Sign In failed!", Toast.LENGTH_SHORT).show())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSignInButton.setOnClickListener(v -> signIn());

        return view;
    }

    private void initializeVariables(View view) {
        mButtonRegister = view.findViewById(R.id.cirRegisterButton);
        mButtonRegister.setEnabled(false);

        mCheckpointEmailAddress = false;
        mCheckpointPassword = false;
        mCheckpointConfirmPassword = false;

        mAuth = FirebaseAuth.getInstance();

        mGoogleSignInButton = view.findViewById(R.id.GoogleBtnRegister);

        mEditTextEmailAddress = view.findViewById(R.id.editTextEmailAddress);
        mEditTextEmailAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_email_24, 0, 0, 0);

        mEditTextPassword = view.findViewById(R.id.editTextPassword);
        mEditTextPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);

        mEditTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        mEditTextConfirmPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_lock_24, 0, 0, 0);

        loadingBar = new ProgressDialog(getActivity());

        mTextInputLayoutEmailAddress = view.findViewById(R.id.textInputEmailAddress);
        mTextInputLayoutPassword = view.findViewById(R.id.textInputPassword);
        mTextInputLayoutConfirmPassword = view.findViewById(R.id.textInputConfirmPassword);
    }

    private void sendEmailVerificationMessage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "The verification message has been sent to the given email address.", Toast.LENGTH_LONG).show();
                    sendUserToLoginFragment();
                    mAuth.signOut();
                }

                else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            });
        }
    }

    private void sendUserToLoginFragment() {
        Intent loginIntent = new Intent(getActivity(), LRContainerActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
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

            if  (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(getActivity(), "Please wait while we are signing you in...", Toast.LENGTH_SHORT).show();
            }

            else {
                Toast.makeText(getActivity(), "Error! Please try again later!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful())
                        startActivity(new Intent(getActivity(), MainActivity.class));

                    else {
                        String errorMessage = task.getException().toString();
                        startActivity(new Intent(getActivity(), LRContainerActivity.class));
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    loadingBar.dismiss();
                });
    }

    private boolean validateEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}