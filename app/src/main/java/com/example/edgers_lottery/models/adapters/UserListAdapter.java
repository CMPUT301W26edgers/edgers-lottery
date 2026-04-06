package com.example.edgers_lottery.models.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.core.User;
import com.example.edgers_lottery.views.admin.AdminUserListActivity;
import com.example.edgers_lottery.views.admin.AdminUserProfileActivity;

import java.util.List;

/**
 * Adapter class for binding a list of {@link User} objects to a ListView.
 * This adapter supports two distinct visual and interactive modes:
 * <ul>
 * <li><b>Interactive Mode (Default):</b> Displays a delete button with a confirmation dialog
 * and allows clicking the row to view the user's detailed profile.</li>
 * <li><b>Read-Only Mode:</b> Hides the delete button and disables row clicks, preventing
 * accidental navigation or deletion when used in contexts like notification logs.</li>
 * </ul>
 * Profile images are loaded dynamically using the Glide library.
 */
public class UserListAdapter extends ArrayAdapter<User> {

    /** The context used for view inflation, intent creation, and Glide image loading. */
    private Context context;

    /** Flag determining whether the adapter should restrict interactive elements (delete/profile). */
    private boolean isReadOnly;

    /**
     * Constructs a standard, interactive UserListAdapter.
     * By defaulting {@code isReadOnly} to false, it enables the delete button and profile
     * navigation clicks. This constructor maintains backward compatibility with existing usages.
     *
     * @param context The current context.
     * @param users   The list of {@link User} objects to display.
     */
    public UserListAdapter(Context context, List<User> users) {
        super(context, 0, users);
        this.context = context;
        this.isReadOnly = false;
    }

    /**
     * Constructs a UserListAdapter with a customizable read-only state.
     * This is utilized in screens like the Admin Notification Logs where the list is purely
     * for display and selection, and administrative actions should be disabled.
     *
     * @param context    The current context.
     * @param users      The list of {@link User} objects to display.
     * @param isReadOnly Pass {@code true} to hide the delete button and disable profile navigation.
     */
    public UserListAdapter(Context context, List<User> users, boolean isReadOnly) {
        super(context, 0, users);
        this.context = context;
        this.isReadOnly = isReadOnly;
    }

    /**
     * Inflates the layout for a user item and populates it with the corresponding data.
     * Handles dynamic image loading via Glide and configures the UI based on the {@code isReadOnly} flag.
     *
     * @param position    The index of the user within the adapter's data set.
     * @param convertView The recycled view to populate, or null if a new view needs to be inflated.
     * @param parent      The parent ViewGroup that this view will eventually be attached to.
     * @return The fully populated View representing the user at the given position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_admin_user_list, parent, false);
        }

        User user = getItem(position);
        TextView name = convertView.findViewById(R.id.userName);
        ImageButton delete = convertView.findViewById(R.id.deleteButton);
        ImageView profileImage = convertView.findViewById(R.id.profileImage);

        name.setText(user.getName());
        String imageUrl = user.getProfileImage();

        // Load the user's profile image using Glide, falling back to a default avatar if empty
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }

        // --- CONFIGURE BEHAVIOR BASED ON READ-ONLY FLAG ---

        if (!isReadOnly) {
            // ORIGINAL BEHAVIOR: Show delete button and allow profile clicks
            delete.setVisibility(View.VISIBLE);

            // Navigate to the user's full profile on row click
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminUserProfileActivity.class);
                intent.putExtra("userId", user.getId());
                context.startActivity(intent);
            });

            // Prompt for confirmation before deleting a user
            delete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete User")
                        .setMessage("Are you sure you want to delete this user?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (context instanceof AdminUserListActivity) {
                                ((AdminUserListActivity) context).removeUser(user.getId());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        } else {
            // READ-ONLY BEHAVIOR: Hide delete button, disable profile clicks
            delete.setVisibility(View.GONE);

            // Remove any existing click listeners to prevent accidental navigation
            convertView.setOnClickListener(null);

            // Explicitly disable clickability so touch events pass through to the parent ListView
            convertView.setClickable(false);
            convertView.setFocusable(false);

            delete.setFocusable(false);
            delete.setClickable(false);
        }

        return convertView;
    }
}