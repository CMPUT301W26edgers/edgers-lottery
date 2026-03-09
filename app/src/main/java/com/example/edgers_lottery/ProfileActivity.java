package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    protected static User user;
    private void showUserInfoDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("User Info")
                .setMessage("Name: " + user.getName() + "\nEmail: " + user.getEmail())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = CurrentUser.get(); // already loaded in StartActivity

        ImageButton homeButton = findViewById(R.id.HomeButton);
        Button deleteProfileButton = findViewById(R.id.deleteProfileButton);
        Button signoutButton = findViewById(R.id.signoutButton);
        // ImageButton checkoutButton = findViewById(R.id.checkoutButton);


        homeButton.setOnClickListener(v -> {
            String role = user.getRole();
            if (role.equals("ORGANIZER")){
                Intent intent = new Intent(this, OrgHomeActivity.class);
                startActivity(intent);
                finish();
            }
            else if (role.equals("ADMIN")) {
                Intent intent = new Intent(this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        deleteProfileButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .delete()
                                .addOnSuccessListener(aVoid -> {

                                    // remove the firebase auth user as well, so email can be remade
                                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                        FirebaseAuth.getInstance().getCurrentUser().delete();
                                    }

                                    // prevents the crash the happens when profile deleted, creating another then delete again
                                    CurrentUser.set(null);

                                    // navigate to new user screen after deletion
                                    Intent intent = new Intent(this, NewUserActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    new AlertDialog.Builder(ProfileActivity.this)
                                            .setTitle("Error")
                                            .setMessage("You will stay and enjoy the lottery")
                                            .setPositiveButton("OK", null)
                                            .show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

}