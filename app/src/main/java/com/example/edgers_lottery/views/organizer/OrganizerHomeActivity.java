package com.example.edgers_lottery.views.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.models.core.CurrentUser;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.core.User;
import com.example.edgers_lottery.views.user.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Home screen activity for organizers.
 * Displays the organizer's username and user ID, and provides navigation
 * to event creation via {@link CreateEditEventActivity} and the organizer's events list.
 */
public class OrganizerHomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    /**
     * Initializes the activity, populates the username and user ID fields,
     * and sets up navigation listeners for the create event and events list buttons.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_organizer_home);
        User currentUser = CurrentUser.get();

        ImageView profileImage = findViewById(R.id.profileImage);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvUsername = findViewById(R.id.tvUsername);
        ImageButton backButton = findViewById(R.id.backButton);


        tvName.setText("@Name");
        tvUsername.setText("@Username");

        TextView btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            startActivity(intent);
            finish();
        });

        TextView btnEventsList = findViewById(R.id.btnEventsList);
        btnEventsList.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerEventsListActivity.class);
            startActivity(intent);
        });
        backButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Go Back")
                    .setMessage("Are you sure you want to go back to User Home?")
                    .setPositiveButton("Yes", (dialog, which) -> {
//                        currentUser.setRole("ENTRANT");
                        Intent intent = new Intent(this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadOrganizerProfile(profileImage, tvName, tvUsername);
    }

    private void loadOrganizerProfile(ImageView profileImage, TextView tvName, TextView tvUsername) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);

                    if (user != null) {
                        tvName.setText(user.getName());
                        tvUsername.setText(user.getUsername());
                        String imageUrl = user.getProfileImage();

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_avatar)
                                    .circleCrop()
                                    .into(profileImage);
                        } else {
                            Glide.with(this)
                                    .load(R.drawable.default_avatar)
                                    .circleCrop()
                                    .into(profileImage);
                        }
                    }
                });
    }
}