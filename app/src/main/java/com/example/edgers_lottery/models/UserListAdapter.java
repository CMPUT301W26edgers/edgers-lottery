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

public class UserListAdapter extends ArrayAdapter<User> {
    private Context context;
    private boolean isReadOnly; // Add a flag to control the behavior

    /**
     * ORIGINAL CONSTRUCTOR (Your teammate's code uses this).
     * By defaulting isReadOnly to false, their code continues to work perfectly!
     */
    public UserListAdapter(Context context, List<User> users) {
        super(context, 0, users);
        this.context = context;
        this.isReadOnly = false;
    }

    /**
     * NEW CONSTRUCTOR (You will use this in AdminNotificationLogActivity).
     * Pass 'true' to disable the delete button and profile clicks.
     */
    public UserListAdapter(Context context, List<User> users, boolean isReadOnly) {
        super(context, 0, users);
        this.context = context;
        this.isReadOnly = isReadOnly;
    }

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

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .circleCrop()
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }

        // --- CHECK THE FLAG INSTEAD OF THE ACTIVITY ---

        if (!isReadOnly) {
            // ORIGINAL BEHAVIOR: Show delete button and allow profile clicks
            delete.setVisibility(View.VISIBLE);

            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdminUserProfileActivity.class);
                intent.putExtra("userId", user.getId());
                context.startActivity(intent);
            });

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
            convertView.setOnClickListener(null);

            convertView.setClickable(false);
            convertView.setFocusable(false);

            delete.setFocusable(false);
            delete.setClickable(false);
        }

        return convertView;
    }
}