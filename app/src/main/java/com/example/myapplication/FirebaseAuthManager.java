package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {
    private FirebaseAuth mAuth;

    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void logout() {
        mAuth.signOut();
    }
}