package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;

/**
 * Home screen for admin users providing navigation to all administrative sections.
 * Accessible sections include organizer list, events list, images view,
 * user list, export notifications, and profile.
 * Should only be launched for users with admin privileges.
 */
public class AdminHomeActivity extends AppCompatActivity {

    /**
     * Initializes the admin home screen, sets menu item labels,
     * and attaches navigation click listeners to each menu item and the profile button.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminhome);

        TextView organizerList = findViewById(R.id.organizerListMenu).findViewById(R.id.menuTitle);
        TextView events = findViewById(R.id.eventListMenu).findViewById(R.id.menuTitle);
        TextView images = findViewById(R.id.imagesViewMenu).findViewById(R.id.menuTitle);
        TextView users = findViewById(R.id.userListMenu).findViewById(R.id.menuTitle);
        TextView export = findViewById(R.id.exportNotificationsMenu).findViewById(R.id.menuTitle);
        ImageButton homeButton = findViewById(R.id.HomeButton);
        ImageButton qrButton = findViewById(R.id.qrButton);
        ImageButton checkoutButton = findViewById(R.id.checkoutButton);
        ImageButton profileButton = findViewById(R.id.ProfileButton);
        ImageButton backButton = findViewById(R.id.backButton);

        organizerList.setText("Organizer List");
        events.setText("Events List");
        images.setText("Images View");
        users.setText("User List");
        export.setText("Export Notifications");

        organizerList.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerListActivity.class);
            startActivity(intent);
        });

        events.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEventListActivity.class);
            startActivity(intent);
        });

        images.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminImagesViewActivity.class);
            startActivity(intent);
        });

        users.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserListActivity.class);
            startActivity(intent);
        });

        export.setOnClickListener(v -> {
            // placeholder
            Intent intent = new Intent(this, AdminEventListActivity.class);
            startActivity(intent);
        });

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

        checkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
            finish();
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}