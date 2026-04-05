package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.models.UserListAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Admin-only activity that displays a list of all non-admin users.
 * Allows administrators to view user profiles and delete user accounts.
 * Should only be accessed for users with admin privileges.
 */
public class AdminUserListActivity extends AppCompatActivity {
    private ListView userList;
    private ArrayList<User> users = new ArrayList<>();
    private UserListAdapter adapter;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes the UI, sets up the adapter, and loads non-admin users from Firestore.
     *
     * @param savedInstanceState previously saved instance state, or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        db = FirebaseFirestore.getInstance();

        // navigate back to admin home
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        userList = findViewById(R.id.adminUserList);
        adapter = new UserListAdapter(this, users);
        userList.setAdapter(adapter);
        loadUsers();
    }

    /**
     * Deletes a user from Firestore by their ID and refreshes the list.
     *
     * @param userId the Firestore document ID of the user to delete
     */
    public void removeUser(String userId) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> loadUsers());
    }

    /**
     * Fetches all non-admin users from Firestore and updates the list view.
     */
    private void loadUsers() {
        db.collection("users")
                .whereNotEqualTo("role", "ADMIN")
                .get()
                .addOnSuccessListener(query -> {
                    users.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            users.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AdminUserList", "Failed to fetch users: " + e.getMessage());
                });
    }
}
