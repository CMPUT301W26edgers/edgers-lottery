package com.example.edgers_lottery.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.services.CommentService;

import java.util.ArrayList;
/**
 * ArrayAdapter for displaying a list of {@link Comment} objects in a ListView.
 * Loads user profile images and usernames from Firestore, and shows a delete
 * button if the current user owns the comment.
 */
public class CommentArrayAdapter extends ArrayAdapter<Comment> {
    /**
     * @param context  the current context
     * @param comments the list of comments to display
     */
    public CommentArrayAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
    }
    /**
     * Returns the view for a single comment item. Inflates the layout if needed,
     * populates comment text, timestamp, username, and profile image. Shows a
     * delete button only if the current user owns the comment.
     *
     * @param position    the position of the item in the list
     * @param convertView a recycled view to reuse, or null if unavailable
     * @param parent      the parent ViewGroup
     * @return the populated view for this comment
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content_comment, parent, false);
        } else {
            view = convertView;
        }

        Comment comment = getItem(position);

        if (comment != null) {
            ImageView profileImage = view.findViewById(R.id.comment_profile_image);
            TextView usernameText = view.findViewById(R.id.comment_username);
            TextView commentText = view.findViewById(R.id.comment_text);
            TextView timestampText = view.findViewById(R.id.comment_timestamp);
            ImageButton deleteButton = view.findViewById(R.id.comment_delete_button);

            // set comment text
            commentText.setText(comment.getCommentText() != null ? comment.getCommentText() : "");

            // set timestamp
            timestampText.setText(comment.getTimestamp() != null ? comment.getTimestamp() : "");

            // load username and profile image from Firestore via userId
            if (comment.getUserID() != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(comment.getUserID())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String username = document.getString("username");
                                String profileImageUrl = document.getString("profileImage");

                                usernameText.setText(username != null ? username : document.getString("name"));

                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(getContext())
                                            .load(profileImageUrl)
                                            .circleCrop()
                                            .placeholder(R.drawable.default_avatar)
                                            .into(profileImage);
                                } else {
                                    profileImage.setImageResource(R.drawable.default_avatar);
                                }
                            }
                        });
            } else {
                usernameText.setText("Unknown User");
                profileImage.setImageResource(R.drawable.default_avatar);
            }

            // show delete button only if current user owns the comment
            if (CurrentUser.get() != null && comment.getUserID() != null
                    && comment.getUserID().equals(CurrentUser.get().getId())) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(getContext())
                            .setTitle("Delete Comment")
                            .setMessage("Are you sure you want to delete this comment?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                CommentService.userDeleteComment(comment.getId(), getContext());
                                remove(comment);
                                notifyDataSetChanged();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }
        }

        return view;
    }
}