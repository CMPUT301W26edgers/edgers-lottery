package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.NotificationLogAdapter;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.models.UserListAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity for Administrators to view logs of all notifications sent to users.
 */
public class AdminNotificationLogActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView userListView;
    private ImageButton backButton;

    // Using your User model and custom adapter
    private ArrayList<User> users;
    private UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_logs);

        db = FirebaseFirestore.getInstance();
        userListView = findViewById(R.id.adminNotificationsUserList);
        backButton = findViewById(R.id.backButton);
        users = new ArrayList<>();

        // Reuse your existing UserListAdapter!
        adapter = new UserListAdapter(this, users, true);
        userListView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        // Load the cross-referenced list of users
        loadUsersWithNotifications();

        // Handle clicks on specific users
        userListView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = users.get(position);
            // Pass both the ID (for querying) and the Name (for the dialog title)
            showUserNotificationsDialog(selectedUser.getId(), selectedUser.getName());
        });
    }

    /**
     * Step 1: Fetches all User IDs from the "notifications" collection.
     * Step 2: Fetches User objects and filters them based on the IDs from Step 1.
     */
    private void loadUsersWithNotifications() {
        // Step 1: Get the IDs of users who have notifications
        db.collection("notifications").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> usersWithNotificationsIds = new HashSet<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        usersWithNotificationsIds.add(document.getId());
                    }

                    if (usersWithNotificationsIds.isEmpty()) {
                        Toast.makeText(this, "No notification logs found.", Toast.LENGTH_SHORT).show();
                        return; // Exit early if no logs exist
                    }

                    // Step 2: Fetch actual User documents and cross-reference
                    db.collection("users").get()
                            .addOnSuccessListener(userQuery -> {
                                users.clear();
                                for (DocumentSnapshot doc : userQuery.getDocuments()) {
                                    // Check if this user's ID exists in our notification list
                                    if (usersWithNotificationsIds.contains(doc.getId())) {
                                        User user = doc.toObject(User.class);
                                        if (user != null) {
                                            user.setId(doc.getId());
                                            users.add(user);
                                        }
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e ->
                                    android.util.Log.e("AdminNotificationLog", "Failed to fetch users: " + e.getMessage())
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notification IDs.", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Fetches the notification array for a specific user and displays it in a custom Dialog.
     * @param userId   The ID of the user clicked (to query the database)
     * @param userName The actual name of the user (to display in the UI)
     */
    @SuppressWarnings("unchecked")
    private void showUserNotificationsDialog(String userId, String userName) {
        db.collection("notifications").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<Map<String, Object>> notificationsArray =
                                (List<Map<String, Object>>) document.get("notifications");

                        String displayName = (userName != null && !userName.isEmpty()) ? userName : "Unknown User";

                        if (notificationsArray == null || notificationsArray.isEmpty()) {
                            // If empty, just show a standard simple dialog
                            new AlertDialog.Builder(this)
                                    .setTitle("Logs for: " + displayName)
                                    .setMessage("This user's notification array is empty.")
                                    .setPositiveButton("Close", null)
                                    .show();
                            return;
                        }

                        // Reverse the list so newest are on top
                        java.util.Collections.reverse(notificationsArray);

                        // 1. Inflate the custom layout
                        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_notification_list, null);

                        // 2. Bind the Title
                        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
                        titleText.setText("Logs for: " + displayName);

                        // 3. Bind the ListView and set our new custom adapter
                        ListView listView = dialogView.findViewById(R.id.dialogListView);
                        NotificationLogAdapter logAdapter = new NotificationLogAdapter(this, notificationsArray);
                        listView.setAdapter(logAdapter);

                        // 4. Build and show the dialog using our custom view
                        new AlertDialog.Builder(this)
                                .setView(dialogView)
                                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        Toast.makeText(this, "No notification document found for this user.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notifications for user.", Toast.LENGTH_SHORT).show()
                );
    }
}