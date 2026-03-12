package com.example.edgers_lottery;

import static com.example.edgers_lottery.User.Role.ENTRANT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class StartActivity extends AppCompatActivity{
    private static final String TAG = "StartActivity";
    protected User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the user is already logged in
        FirebaseUser deviceUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean hasSignedInBefore = prefs.getBoolean("has_signed_in_before", false);

        if (deviceUser != null) {
            // User is logged in, navigate to the appropriate activity
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Firebase remembers the user
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String role = document.getString("role");
                            // based on the user's role, navigate to the appropriate activity
                            // this is done here with a switch case statemment
                            if (role == null) {
                                navigateTo(NewUserActivity.class);
                                return;
                            }
                            switch (role) {
                                case "ORGANIZER":
                                    user = document.toObject(User.class);
                                    CurrentUser.set(user);
                                    prefs.edit().putBoolean("has_signed_in_before", true).apply();
                                    navigateTo(OrgHomeActivity.class);
                                    break;
                                case "ADMIN":
                                    user = document.toObject(User.class);
                                    CurrentUser.set(user);
                                    prefs.edit().putBoolean("has_signed_in_before", true).apply();
                                    navigateTo(AdminHomeActivity.class);
                                    break;
                                default: // "entrant"
                                    user = document.toObject(User.class);
                                    CurrentUser.set(user);
                                    prefs.edit().putBoolean("has_signed_in_before", true).apply();
                                    navigateTo(HomeActivity.class);
                                    break;
                            }
                        } else { // safety call
//                            navigateTo(NewUserActivity.class); // did not find the user in the database

                            if (hasSignedInBefore) {
                                String email = document.getString("email");
                                if (email == null) { // has signed in before but could not find user in database
//                                    navigateTo(NewUserActivity.class);
                                    navigateTo(LoginActivity.class); // returning user who signed out
                                }
                            } else {
                                navigateTo(NewUserActivity.class); // brand new user
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        navigateTo(NewUserActivity.class);
                    });
        } else {
//            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//            boolean hasSignedInBefore = prefs.getBoolean("has_signed_in_before", false);
            if (hasSignedInBefore) {
                navigateTo(LoginActivity.class); // returning user who signed out
            } else {
                navigateTo(NewUserActivity.class); // brand new user
            }
        }
    }
    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

