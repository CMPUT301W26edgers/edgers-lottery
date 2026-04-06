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

public class AdminUserListActivity extends AppCompatActivity {
    private ListView userList;
    private ArrayList<User> users = new ArrayList<>();
    private UserListAdapter adapter;
    private FirebaseFirestore db;

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

    public void removeUser(String userId) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> loadUsers());
    }

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
