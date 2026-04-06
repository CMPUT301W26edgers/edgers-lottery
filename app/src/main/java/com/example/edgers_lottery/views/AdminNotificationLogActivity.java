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
import com.example.edgers_lottery.models.CurrentUser;
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
 * This activity displays a list of users who have received notifications. Tapping on a
 * user opens a custom dialog displaying a chronological list of their notification history.
 */
public class AdminNotificationLogActivity extends AppCompatActivity {

    /** Instance of FirebaseFirestore used to query the database. */
    private FirebaseFirestore db;

    /** ListView displaying the list of users who have notification logs. */
    private ListView userListView;

    /** Button to navigate back to the previous administrative screen. */
    private ImageButton backButton;

    /** Local list storing the retrieved User objects for the adapter. */
    private ArrayList<User> users;

    /** Custom adapter used to display the users in a read-only visual state. */
    private UserListAdapter adapter;

    protected  User user;

    /**
     * Initializes the activity, configures the UI components, and triggers the initial
     * data fetch. The {@link UserListAdapter} is intentionally initialized in 'read-only'
     * mode to prevent accidental user deletion from this logging screen.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_logs);

        db = FirebaseFirestore.getInstance();
        userListView = findViewById(R.id.adminNotificationsUserList);
        backButton = findViewById(R.id.backButton);
        users = new ArrayList<>();
        user = CurrentUser.get();

        // Reuse the existing UserListAdapter, but pass 'true' to enable read-only mode!
        adapter = new UserListAdapter(this, users, true);
        userListView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        // Load the cross-referenced list of users
        loadUsersWithNotifications();

        // Handle clicks on specific users to view their logs
        userListView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = users.get(position);
            // Pass both the ID (for querying) and the Name (for the dialog title)
            showUserNotificationsDialog(selectedUser.getId(), selectedUser.getName());
        });
    }

    /**
     * Fetches and populates the ListView with users who have an existing notification log.
     * This is a two-step relational query process:
     * <ol>
     * <li>Queries the "notifications" collection to gather a unique Set of User IDs
     * that have notification documents.</li>
     * <li>Queries the "users" collection and filters the results, adding only the users
     * whose IDs exist in the Set gathered from step 1.</li>
     * </ol>
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
                                            user.setId(doc.getId()); // Ensure the ID is mapped
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
     * Retrieves the notification history for a specific user and displays it within a custom dialog.
     * Reverses the notification array so the most recent events appear at the top, and uses
     * {@link NotificationLogAdapter} to render each log entry as a visually styled card.
     *
     * @param userId   The unique Firestore document ID of the selected user (used to query their logs).
     * @param userName The display name of the selected user (used to personalize the dialog title).
     */
    @SuppressWarnings("unchecked")
    private void showUserNotificationsDialog(String userId, String userName) {
        db.collection("notifications").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Extract the array of notification maps from the document
                        List<Map<String, Object>> notificationsArray =
                                (List<Map<String, Object>>) document.get("notifications");

                        String displayName = (userName != null && !userName.isEmpty()) ? userName : "Unknown User";

                        if (notificationsArray == null || notificationsArray.isEmpty()) {
                            // If empty, fallback to a standard simple text dialog
                            new AlertDialog.Builder(this)
                                    .setTitle("Logs for: " + displayName)
                                    .setMessage("This user's notification array is empty.")
                                    .setPositiveButton("Close", null)
                                    .show();
                            return;
                        }

                        // Reverse the list so the newest notifications are displayed at the top
                        java.util.Collections.reverse(notificationsArray);

                        // 1. Inflate the custom dialog layout containing a ListView
                        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_notification_list, null);

                        // 2. Bind and set the dialog Title
                        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
                        titleText.setText("Logs for: " + displayName);

                        // 3. Bind the ListView and apply our custom NotificationLogAdapter
                        ListView listView = dialogView.findViewById(R.id.dialogListView);
                        NotificationLogAdapter logAdapter = new NotificationLogAdapter(this, notificationsArray);
                        listView.setAdapter(logAdapter);

                        // 4. Build and display the AlertDialog using the custom view
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