package com.example.edgers_lottery.views;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.AppNotification;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.NotificationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.Source;

/**
 * Displays a real-time feed of notifications for the currently logged-in user.
 * Listens for changes in the Firestore {@code notifications} collection and
 * updates the list automatically when new notifications arrive.
 */
public class ExportNotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final List<AppNotification> notificationList = new ArrayList<>();

    /**
     * Snapshot listener reference — kept so it can be detached in {@link #onStop()}
     * to avoid memory leaks when the activity is not visible.
     */
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_notifications);

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // attach the listener when the activity becomes visible
        attachNotificationListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // detach the listener when the activity is no longer visible to prevent memory leaks
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    /**
     * Attaches a real-time Firestore snapshot listener filtered to the current user.
     * Any new notifications written to Firestore will appear instantly without a manual refresh.
     */
//    private void attachNotificationListener() {
//        String currentUserId = CurrentUser.get().getId();
//        android.util.Log.d("ExportNotifications", "Current user ID: " + currentUserId); // ← add this
//
//        listenerRegistration = FirebaseFirestore.getInstance()
//                .collection("notifications")
//                .whereEqualTo("userId", currentUserId)
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .addSnapshotListener((snapshots, error) -> {
//                    if (error != null) {
//                        android.util.Log.e("ExportNotifications", "Listener error", error);
//                        return;
//                    }
//                    if (snapshots != null) {
//                        android.util.Log.d("ExportNotifications", "Documents found: " + snapshots.size()); // ← add this
//
//                        // refresh the list with latest data from Firestore
//                        notificationList.clear();
//                        notificationList.addAll(snapshots.toObjects(AppNotification.class));
//                        adapter.notifyDataSetChanged();
//                    }
//                });
//    }
    private void attachNotificationListener() {
        String currentUserId = CurrentUser.get().getId();
        android.util.Log.d("ExportNotifications", "Querying for userId: " + currentUserId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        android.util.Log.d("ExportNotifications", "Firestore app name: " + db.getApp().getName());
        android.util.Log.d("ExportNotifications", "Firestore project ID: " + db.getApp().getOptions().getProjectId());

        db.collection("notifications")
                .get(Source.SERVER)
                .addOnSuccessListener(snapshots -> {
                    android.util.Log.d("ExportNotifications", "Total docs: " + snapshots.size());
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        android.util.Log.d("ExportNotifications", "Found doc ID: " + doc.getId());
                        android.util.Log.d("ExportNotifications", "  userId field: '" + doc.getString("userId") + "'");
                    }
                })
                .addOnFailureListener(e ->
                        android.util.Log.e("ExportNotifications", "FETCH FAILED: " + e.getMessage(), e));
    }
}