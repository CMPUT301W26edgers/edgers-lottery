package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListenerRegistration listenerReg;
    private String eventId;
    private String currentUserId;
    private String currentUsername;

    private CommentAdapter adapter;
    private EditText etComment;

    private String replyToId = null;
    private String replyToUsername = null;

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_view);

        eventId = getIntent().getStringExtra("event_id");
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId   = user.getUid();
            currentUsername = user.getDisplayName() != null ? user.getDisplayName() : "Anonymous";
        } else {
            currentUserId   = "";
            currentUsername = "Anonymous";
        }

        initViews();
        setupNavigation();
        listenToComments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerReg != null) listenerReg.remove();
    }

    // ── Views ──────────────────────────────────────────────────────────────

    private void initViews() {
        RecyclerView rv = findViewById(R.id.rvComments);
        adapter = new CommentAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        etComment = findViewById(R.id.etComment);
        findViewById(R.id.btnSendComment).setOnClickListener(v -> postComment());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // ── Navigation (matches your existing pattern) ─────────────────────────

    private void setupNavigation() {
        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            finish();
            Intent i = new Intent(this, EventDetailsOrganizer.class);
            i.putExtra("event_id", eventId);
            startActivity(i);
        });
        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            finish();
            Intent i = new Intent(this, EventWaitlistTab.class);
            i.putExtra("event_id", eventId);
            startActivity(i);
        });
        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            finish();
            Intent i = new Intent(this, EventEntrantOrganizer.class);
            i.putExtra("event_id", eventId);
            startActivity(i);
        });
        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            finish();
            Intent i = new Intent(this, CreateEditEventActivity.class);
            i.putExtra("event_id", eventId);
            startActivity(i);
        });
    }

    // ── Firestore ──────────────────────────────────────────────────────────

    private void listenToComments() {
        listenerReg = db.collection("events")
                .document(eventId)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null) return;

                    List<Map<String, Object>> comments = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            data.put("commentId", doc.getId());
                            comments.add(data);
                        }
                    }
                    adapter.setComments(comments);
                });
    }

    private void postComment() {
        String text = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        Map<String, Object> comment = new HashMap<>();
        comment.put("userId",    currentUserId);
        comment.put("username",  currentUsername);
        comment.put("text",      text);
        comment.put("timestamp", new Date());
        comment.put("likedBy",   new ArrayList<>());
        comment.put("parentId",  replyToId);

        db.collection("events").document(eventId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(ref -> {
                    etComment.setText("");
                    replyToId       = null;
                    replyToUsername = null;
                    etComment.setHint("Add a comment...");
                })
                .addOnFailureListener(err ->
                        Toast.makeText(this, "Failed to post: " + err.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void toggleLike(String commentId, List<String> likedBy) {
        boolean liked = likedBy != null && likedBy.contains(currentUserId);
        db.collection("events").document(eventId)
                .collection("comments").document(commentId)
                .update("likedBy", liked
                        ? FieldValue.arrayRemove(currentUserId)
                        : FieldValue.arrayUnion(currentUserId))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to like.", Toast.LENGTH_SHORT).show());
    }

    private void deleteComment(String commentId) {
        // Delete replies first, then the comment
        db.collection("events").document(eventId)
                .collection("comments")
                .whereEqualTo("parentId", commentId)
                .get()
                .addOnSuccessListener(snap -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.delete(db.collection("events").document(eventId)
                            .collection("comments").document(commentId));
                    batch.commit()
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete.", Toast.LENGTH_SHORT).show());
                });
    }

    private void setReplyTarget(String commentId, String username) {
        replyToId       = commentId;
        replyToUsername = username;
        etComment.setHint("Replying to " + username + "…");
        etComment.requestFocus();
    }

    // ── Adapter ────────────────────────────────────────────────────────────

    class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {

        private final List<Map<String, Object>> items = new ArrayList<>();

        void setComments(List<Map<String, Object>> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int position) {
            h.bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvUsername, tvCommentText, tvLikeCount, btnReply, btnDelete;
            ImageButton btnLike;

            VH(View v) {
                super(v);
                tvUsername    = v.findViewById(R.id.tvUsername);
                tvCommentText = v.findViewById(R.id.tvCommentText);
                tvLikeCount   = v.findViewById(R.id.tvLikeCount);
                btnReply      = v.findViewById(R.id.btnReply);
                btnDelete     = v.findViewById(R.id.btnDelete);
                btnLike       = v.findViewById(R.id.btnLike);
            }

            void bind(Map<String, Object> comment) {
                String commentId = (String) comment.get("commentId");
                String username  = (String) comment.get("username");
                String text      = (String) comment.get("text");
                String userId    = (String) comment.get("userId");
                List<String> likedBy = (List<String>) comment.get("likedBy");

                tvUsername.setText(username);
                tvCommentText.setText(text);
                tvLikeCount.setText(String.valueOf(likedBy != null ? likedBy.size() : 0));

                // Only show delete for own comments
                btnDelete.setVisibility(
                        currentUserId.equals(userId) ? View.VISIBLE : View.GONE);

                btnLike.setOnClickListener(v -> toggleLike(commentId, likedBy));
                btnReply.setOnClickListener(v -> setReplyTarget(commentId, username));
                btnDelete.setOnClickListener(v -> deleteComment(commentId));
            }
        }
    }
}