package com.example.edgers_lottery.models;

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
import com.example.edgers_lottery.views.AdminUserListActivity;
import com.example.edgers_lottery.views.AdminUserProfileActivity;

import java.util.List;

/**
 * Adapter that populates the user list in {@link AdminUserListActivity}.
 * Each row displays the user's name and a delete button that prompts
 * for confirmation before removing the user.
 */
public class UserListAdapter extends ArrayAdapter<User> {
    private Context context;

    /**
     * Constructs a new adapter for the given list of users.
     *
     * @param context    the hosting context, expected to be an {@link AdminUserListActivity}
     * @param users the list of {@link User}
     */
    public UserListAdapter(Context context, List<User> users) {
        super(context, 0, users);
        this.context = context;
    }

    /**
     * Inflates or recycles a row view and binds the user's name and delete button.
     * The delete button shows a confirmation dialog before calling
     * {@link AdminUserListActivity#removeUser}.
     *
     * @param position    the index of the item in the list
     * @param convertView a recycled view to reuse, or null if a new view must be inflated
     * @param parent      the parent ViewGroup the view will be attached to
     * @return the populated row view for the user at the given position
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

        // Load and display the user's profile image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }

        // Open a user's full profile on click
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserProfileActivity.class);
            intent.putExtra("userId", user.getId());
            context.startActivity(intent);
        });

        // delete button removes one's profile
        delete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (context instanceof AdminUserListActivity) {
                            ((AdminUserListActivity) context)
                                    .removeUser(user.getId());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return convertView;
    }
}