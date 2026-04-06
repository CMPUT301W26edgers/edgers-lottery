package com.example.edgers_lottery.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.services.ImageService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity that displays and manages the current user's profile.
 * Supports editing profile fields, deleting the account, and signing out.
 * Implements {@link EditProfileFragment.EditProfileDialogListener} to receive profile edit results.
 */
public class ProfileActivity extends AppCompatActivity implements EditProfileFragment.EditProfileDialogListener {
    private static final String TAG = "ProfileActivity";
    protected static User user;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView descriptionTextView;
    private TextView locationTextView;
    private TextView usernameTextView;
    private TextView phoneTextView;
    private ImageView profileImageView;

    private  ImageButton uploadProfileImageButton;

    private static final int PICK_IMAGE_REQUEST = 1;
    /**
     * Displays an alert dialog showing the given user's name and email.
     *
     * @param user the {@link User} whose info is displayed
     */
    private void showUserInfoDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("User Info")
                .setMessage("Name: " + user.getName() + "\nEmail: " + user.getEmail())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Applies edited profile fields to the given user, persists the changes to Firestore,
     * and refreshes the displayed profile TextViews.
     *
     * @param user                 the {@link User} object to update
     * @param newDesc              the new description entered by the user
     * @param newEmail             the new email entered by the user
     * @param newLocation          the new location entered by the user
     * @param newPhone             the new phone number entered by the user
     * @param newUsername          the new username entered by the user
     * @param notificationsEnabled true if the user wants to receive notifications, false to opt out
     */
    public void editUser(User user, String newDesc, String newEmail, String newLocation,
                         String newPhone, String newUsername, boolean notificationsEnabled) {
        user.setEmail(newEmail);
        user.setDescription(newDesc);
        user.setLocation(newLocation);
        user.setPhone(newPhone);
        user.setUsername(newUsername);
        user.setNotificationsEnabled(notificationsEnabled);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    CurrentUser.set(user);
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

    /**
     * Initializes the activity, populates profile fields from the current user,
     * and sets up listeners for the home, edit, delete, and sign-out buttons.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        loadUser();

        uploadProfileImageButton = findViewById(R.id.uploadImageButton);

        ImageButton homeButton = findViewById(R.id.HomeButton);
        ImageButton qrButton = findViewById(R.id.qrButton);
        Button editProfileButton = findViewById(R.id.ProfileEditButton);
        Button deleteProfileButton = findViewById(R.id.deleteProfileButton);
        Button signoutButton = findViewById(R.id.signoutButton);

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        qrButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, QrScannerActivity.class);
            startActivity(intent);
            finish();
        });


        editProfileButton.setOnClickListener(v -> {
            EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(user);
            editProfileFragment.show(getSupportFragmentManager(), "edit_profile");
        });

        // click profile image to upload new image
        uploadProfileImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
                                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                        FirebaseAuth.getInstance().getCurrentUser().delete();
                                    }
                                    CurrentUser.set(null);
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

        signoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            CurrentUser.set(null);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    /**
     * Handles the result of the image picker. If a valid image was selected,
     * displays it in the profile image view, uploads it to Firebase Storage,
     * and updates the current user.
     *
     * @param requestCode the request code from startActivityForResult
     * @param resultCode  the result code returned by the image picker activity
     * @param data        the intent containing the selected image URI
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData(); // the image the user picked
            Glide.with(this).load(imageUri).circleCrop().into(profileImageView); // load the image into the ImageView
            ImageService.uploadProfileImage(imageUri, this);
            assert imageUri != null;
            // update current user
            user.setProfileImage(imageUri.toString());
            CurrentUser.set(user);
        }
    }

    /**
     * Loads the current user's profile data and populates the UI fields,
     * including name, username, description, email, phone, location,
     * and profile image.
     */
    private void loadUser(){
        user = CurrentUser.get();

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

        profileImageView = findViewById(R.id.profileImageView);
        if (user.getProfileImage() == null) {
            profileImageView.setImageResource(R.drawable.default_avatar);// set as default avatar for now as user has not profile picture
        }
        else Glide.with(this).load(user.getProfileImage()).circleCrop().into(profileImageView);
    }
}