package com.example.edgers_lottery.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Entry point activity that determines where to route the user on app launch.
 * Checks Firebase Authentication state and Firestore role to navigate to
 * {@link AdminHomeActivity}, {@link HomeActivity}, {@link LoginActivity},
 * or {@link NewUserActivity} as appropriate.
 */
public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";
    protected User user;

    /**
     * Checks the current Firebase Authentication state and user role,
     * then navigates to the appropriate activity based on login history and account type.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser deviceUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean hasSignedInBefore = prefs.getBoolean("has_signed_in_before", false);
//        SeedEvents.seed();
        if (deviceUser != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String role = document.getString("role");
                            if (role == null) {
                                navigateTo(NewUserActivity.class);
                                return;
                            }
                            switch (role) {
                                case "ADMIN":
                                    user = document.toObject(User.class);
                                    CurrentUser.set(user);
                                    prefs.edit().putBoolean("has_signed_in_before", true).apply();
                                    navigateTo(AdminHomeActivity.class);
                                    break;
                                default:
                                    user = document.toObject(User.class);
                                    CurrentUser.set(user);
                                    prefs.edit().putBoolean("has_signed_in_before", true).apply();
                                    navigateTo(HomeActivity.class);
                                    break;
                            }
                        } else {
                            if (hasSignedInBefore) {
                                String email = document.getString("email");
                                if (email == null) {
                                    navigateTo(LoginActivity.class);
                                }
                            } else {
                                navigateTo(NewUserActivity.class);
                            }
                        }
                    })
                    .addOnFailureListener(e -> navigateTo(NewUserActivity.class));
        } else {
            if (hasSignedInBefore) {
                navigateTo(LoginActivity.class);
            } else {
                navigateTo(NewUserActivity.class);
            }
        }
    }

    /**
     * Navigates to the specified activity and clears the back stack.
     *
     * @param destination the activity class to navigate to
     */
    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}