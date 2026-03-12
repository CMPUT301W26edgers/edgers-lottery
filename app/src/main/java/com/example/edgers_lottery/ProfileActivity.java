package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity implements EditProfileFragment.EditProfileDialogListener {
    private static final String TAG = "ProfileActivity";
    protected static User user;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView descriptionTextView;
    private TextView locationTextView;
    private TextView usernameTextView;
    private TextView phoneTextView;


    private void showUserInfoDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("User Info")
                .setMessage("Name: " + user.getName() + "\nEmail: " + user.getEmail())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    public void editUser(User user, String newDesc, String newEmail, String newLocation, String newPhone, String newUsername){
        user.setEmail(newEmail);
        user.setDescription(newDesc);
        user.setLocation(newLocation);
        user.setPhone(newPhone);
        user.setUsername(newUsername);
        // update the user in the database
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // update the user in the CurrentUser class
                    CurrentUser.set(user);
                    // show a success
                    new AlertDialog.Builder(this)
                            .setTitle("Success")
                            .setMessage("Profile updated successfully")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                });
        emailTextView.setText(user.getEmail());
        descriptionTextView.setText(user.getDescription());
        locationTextView.setText(user.getLocation());
        usernameTextView.setText(user.getUsername());
        phoneTextView.setText(user.getPhone());

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = CurrentUser.get(); // already loaded in StartActivity
        // set Profile names to user name
        TextView profileNames = findViewById(R.id.ProfileNames);
        profileNames.setText(user.getName());

        usernameTextView = findViewById(R.id.Username);
        usernameTextView.setText(user.getUsername());

        descriptionTextView = findViewById(R.id.descriptionText);
        descriptionTextView.setText(user.getDescription());

        emailTextView = findViewById(R.id.ProfileEmail);
        emailTextView.setText("Email: " + user.getEmail());

        phoneTextView = findViewById(R.id.ProfilePhone);
        phoneTextView.setText("Phone: " + user.getPhone());

        locationTextView = findViewById(R.id.ProfileLocation);
        locationTextView.setText("Location: " + user.getLocation());


        // set up buttons here
        ImageButton homeButton = findViewById(R.id.HomeButton);
        Button editProfileButton = findViewById(R.id.ProfileEditButton);
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
        editProfileButton.setOnClickListener(v -> {
            // create the fragment so the user can edit their profile
            EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(user);
            editProfileFragment.show(getSupportFragmentManager(), "edit_profile");
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