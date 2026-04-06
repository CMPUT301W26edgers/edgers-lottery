package com.example.edgers_lottery.views.user;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.core.AppNotification;
import com.example.edgers_lottery.models.core.CurrentUser;
import com.example.edgers_lottery.models.adapters.NotificationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExportNotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final List<AppNotification> notificationList = new ArrayList<>();
    private View emptyState;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_notifications);

        emptyState = findViewById(R.id.emptyState);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupSwipeToDelete();
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachNotificationListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    private void updateEmptyState() {
        if (notificationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void attachNotificationListener() {
        String currentUserId = CurrentUser.get().getId();
        android.util.Log.d("ExportNotifications", "Attaching listener for userId: " + currentUserId);

        listenerRegistration = FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(currentUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e("ExportNotifications", "Listener error", error);
                        return;
                    }

                    notificationList.clear();

                    if (snapshot != null && snapshot.exists()) {
                        List<Map<String, Object>> raw =
                                (List<Map<String, Object>>) snapshot.get("notifications");

                        if (raw != null) {
                            for (Map<String, Object> entry : raw) {
                                AppNotification n = new AppNotification();
                                n.setEventId((String) entry.get("eventId"));
                                n.setEventName((String) entry.get("eventName"));
                                n.setType((String) entry.get("type"));
                                n.setRead(Boolean.TRUE.equals(entry.get("isRead")));
                                n.setTimestamp((com.google.firebase.Timestamp) entry.get("timestamp"));
                                notificationList.add(n);
                            }
                        }
                        android.util.Log.d("ExportNotifications", "Notifications loaded: " + notificationList.size());
                    } else {
                        android.util.Log.d("ExportNotifications", "No notification document found for user");
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    markAllAsRead(currentUserId);
                });
    }

    private void markAllAsRead(String currentUserId) {
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists()) return;

                    List<Map<String, Object>> raw =
                            (List<Map<String, Object>>) snapshot.get("notifications");
                    if (raw == null) return;

                    List<Map<String, Object>> updated = new ArrayList<>(raw);
                    for (Map<String, Object> entry : updated) {
                        entry.put("isRead", true);
                    }

                    FirebaseFirestore.getInstance()
                            .collection("notifications")
                            .document(currentUserId)
                            .update("notifications", updated);
                });
    }

    private void setupSwipeToDelete() {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#E53935"));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                notificationList.remove(position);
                adapter.notifyItemRemoved(position);
                updateEmptyState();

                String currentUserId = CurrentUser.get().getId();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("notifications").document(currentUserId)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot == null || !snapshot.exists()) return;

                            List<Map<String, Object>> raw =
                                    (List<Map<String, Object>>) snapshot.get("notifications");
                            if (raw == null) return;

                            List<Map<String, Object>> updated = new ArrayList<>(raw);
                            if (position < updated.size()) {
                                updated.remove(position);
                            }

                            db.collection("notifications").document(currentUserId)
                                    .update("notifications", updated)
                                    .addOnSuccessListener(unused ->
                                            android.util.Log.d("ExportNotifications", "Deleted at index " + position))
                                    .addOnFailureListener(e ->
                                            android.util.Log.e("ExportNotifications", "Delete failed: " + e.getMessage()));
                        })
                        .addOnFailureListener(e ->
                                android.util.Log.e("ExportNotifications", "Fetch for delete failed: " + e.getMessage()));
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                RectF background = new RectF(
                        itemView.getRight() + dX,
                        itemView.getTop() + 4f,
                        itemView.getRight(),
                        itemView.getBottom() - 4f
                );
                c.drawRoundRect(background, 12f, 12f, paint);

                Paint iconPaint = new Paint();
                iconPaint.setColor(Color.WHITE);
                iconPaint.setTextSize(52f);
                iconPaint.setTextAlign(Paint.Align.CENTER);
                float iconX = itemView.getRight() - 80f;
                float iconY = itemView.getTop() + (itemView.getHeight() / 2f) + 18f;
                c.drawText("🗑", iconX, iconY, iconPaint);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }
}