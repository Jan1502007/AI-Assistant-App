package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "GoogleAuth";
    private CredentialManager credentialManager;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Credential Manager
        credentialManager = CredentialManager.create(this);

        Button signInButton = findViewById(R.id.btn_google_signin);
        signInButton.setOnClickListener(v -> triggerGoogleSignIn());
    }

    private void triggerGoogleSignIn() {
        // Configure the Google OAuth Request
        // Replace with your actual Web Client ID from Google Cloud Console
        String webClientId = "15655029445-ba9i76m3eksfvmqg1snhoqprfi67rsl2.apps.googleusercontent.com";

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build();

        // Package option into a credential request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Execute request asynchronously
        credentialManager.getCredentialAsync(this, request, null, executor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignInResult(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Sign-in error: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(SigninActivity.this, "Sign-in failed", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void handleSignInResult(Credential credential) {
        if (credential instanceof CustomCredential &&
                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

            try {
                // Extract Google OAuth profile details from token
                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.getData());

                String idToken = googleIdTokenCredential.getIdToken();
                String userEmail = googleIdTokenCredential.getId();
                String displayName = googleIdTokenCredential.getDisplayName();

                // Token validation logic (Send idToken to your backend here)
                runOnUiThread(() -> {
                    Toast.makeText(SigninActivity.this, "Welcome " + displayName, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "OAuth Token: " + idToken);
                });

            } catch (Exception e) {
                Log.e(TAG, "Data parsing error", e);
            }
        }
    }

    private void signOut() {
        // 1. Initialize the correct request object
        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();

        // 2. Clear user credential session across providers
        credentialManager.clearCredentialStateAsync(
                clearRequest,
                new CancellationSignal(), // Required argument
                executor,
                new CredentialManagerCallback<Void, ClearCredentialException>() { // Specify explicit exception type
                    @Override
                    public void onResult(Void result) {
                        Log.d(TAG, "Signed out successfully");
                        runOnUiThread(() -> {
                            // Update your UI here (e.g., return to login screen)
                            Toast.makeText(SigninActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e(TAG, "Sign out error: " + e.getLocalizedMessage());
                    }
                }
        );
    }
}
