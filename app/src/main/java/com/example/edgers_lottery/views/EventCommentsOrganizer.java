package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.Comment;
import com.example.edgers_lottery.models.OrganizerCommentArrayAdapter;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.services.CommentService;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
/**
 * Activity for organizers to view and manage comments on their event.
 * Organizers can post new comments and delete any existing comment.
 * Comments are refreshed each time the activity resumes.
 */
public class EventCommentsOrganizer extends AppCompatActivity {

    private static final String TAG = "EventCommentsOrganizer";

    private String eventId;
    private FirebaseFirestore db;
    private ListView commentsList;
    private EditText etCommentInput;
    private ArrayList<Comment> commentsArray = new ArrayList<>();
    private OrganizerCommentArrayAdapter adapter;
    /**
     * Initializes the activity, retrieves the event ID from the intent,
     * and sets up views and listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_organizer);

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
        adapter = new OrganizerCommentArrayAdapter(this, commentsArray);
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
     * Sets up click listeners for navigation buttons, comment deletion via
     * long press, and the post comment button.
     */
    private void setupListeners() {
        findViewById(R.id.btnBackComments).setOnClickListener(v -> finish());

        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, EventDetailsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, EventWaitlistTab.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, EventEntrantOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, CreateEditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        // long press to delete comment as organizer
        commentsList.setOnItemLongClickListener((parent, view, position, id) -> {
            Comment comment = commentsArray.get(position);
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Comment")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        CommentService.organizerDeleteComment(comment.getId(), this);
                        commentsArray.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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
}