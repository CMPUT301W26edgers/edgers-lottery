package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.Comment;
import com.example.edgers_lottery.models.CommentArrayAdapter;
import com.example.edgers_lottery.models.OrganizerCommentArrayAdapter;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.services.CommentService;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
/**
 * Activity for users to view and post comments on an event.
 * Users can post new comments and long-press any comment to view the commenter's profile.
 * Comments are refreshed each time the activity resumes.
 */
public class EventCommentsActivity extends AppCompatActivity {

    private static final String TAG = "EventCommentsActivity";

    private String eventId;
    private FirebaseFirestore db;
    private ListView commentsList;
    private EditText etCommentInput;
    private ArrayList<Comment> commentsArray = new ArrayList<>();
    private CommentArrayAdapter adapter;
    /**
     * Initializes the activity, retrieves the event ID from the intent,
     * and sets up views and listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_user);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("event_id");

        initViews();
        setupListeners();

        if (eventId == null) {
            Toast.makeText(this, "No event ID provided", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Reloads comments from Firestore each time the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (eventId != null) {
            loadComments();
        }
    }
    /**
     * Binds UI elements and sets up the comment list adapter.
     */
    private void initViews() {
        commentsList = findViewById(R.id.commentsList);
        etCommentInput = findViewById(R.id.etCommentInput);
        adapter = new CommentArrayAdapter(this, commentsArray);
        commentsList.setAdapter(adapter);
    }
    /**
     * Fetches comments for the current event from Firestore and refreshes the list.
     */
    private void loadComments() {
        CommentService.getCommentsForEvent(eventId, comments -> {
            commentsArray.clear();
            commentsArray.addAll(comments);
            adapter.notifyDataSetChanged();
        });
    }
    /**
     * Sets up click listeners for the back button, post comment button,
     * and long-press on comments to view the commenter's profile.
     */
    private void setupListeners() {
        findViewById(R.id.btnBackComments).setOnClickListener(v -> finish());

        // long press to delete comment if admin, view profile if non-admin
        commentsList.setOnItemLongClickListener((parent, view, position, id) -> {
            Comment comment = commentsArray.get(position);
            if ("ADMIN".equals(CurrentUser.get().getRole())) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            CommentService.adminDeleteComment(comment.getId(), this);
                            commentsArray.remove(position);
                            adapter.notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                viewProfile(comment);
            }
            return true;
        });

        Button btnPostComment = findViewById(R.id.btnPostComment);
        btnPostComment.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = CurrentUser.get().getId();
            CommentService.addComment(eventId, userId, commentText);
            etCommentInput.setText("");
            loadComments(); // refresh after posting
        });
    }
    /**
     * Fetches the commenter's profile from Firestore and displays it in a dialog,
     * including their username, name, description, and profile image.
     *
     * @param comment the comment whose author's profile should be displayed
     */
    private void viewProfile(Comment comment) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_profile, null);
        ImageView profileImage = dialogView.findViewById(R.id.dialogProfileImage);
        TextView username = dialogView.findViewById(R.id.dialogUsername);
        TextView name = dialogView.findViewById(R.id.dialogName);
        TextView description = dialogView.findViewById(R.id.dialogDescription);

        FirebaseFirestore.getInstance().collection("users")
                .document(comment.getUserID())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        username.setText(document.getString("username") != null ? document.getString("username") : "No username");
                        name.setText(document.getString("name") != null ? document.getString("name") : "No name");
                        description.setText(document.getString("description") != null ? document.getString("description") : "No description");

                        String imageUrl = document.getString("profileImage");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).circleCrop().into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.default_avatar);
                        }

                        new AlertDialog.Builder(this)
                                .setView(dialogView)
                                .setPositiveButton("Close", null)
                                .show();
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}