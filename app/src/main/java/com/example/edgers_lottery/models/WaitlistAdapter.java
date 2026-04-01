package com.example.edgers_lottery.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;

import java.util.List;

/**
 * RecyclerView adapter that displays a list of waitlisted users.
 * Each row shows the user's profile image, name, and a remove button.
 * Removal events are dispatched through {@link OnRemoveClickListener}.
 */
public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

    /**
     * Listener interface for handling removal of a waitlisted user.
     */
    public interface OnRemoveClickListener {

        /**
         * Called when the remove button is clicked for a waitlisted user.
         *
         * @param user     the {@link WaitlistUser} to be removed
         * @param position the adapter position of the item
         */
        void onRemove(WaitlistUser user, int position);
    }

    private List<WaitlistUser> users;
    private OnRemoveClickListener removeListener;

    /**
     * Constructs a new adapter for the given waitlist.
     *
     * @param users          the list of {@link WaitlistUser} objects to display
     * @param removeListener the listener to notify when a user is removed
     */
    public WaitlistAdapter(List<WaitlistUser> users, OnRemoveClickListener removeListener) {
        this.users = users;
        this.removeListener = removeListener;
    }

    /**
     * Inflates the row layout for a waitlist item and returns a new {@link ViewHolder}.
     *
     * @param parent   the parent ViewGroup into which the new view will be added
     * @param viewType the view type of the new view
     * @return a new {@link ViewHolder} holding the inflated row view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a {@link WaitlistUser} to the given {@link ViewHolder}, setting the name,
     * remove button listener, and profile image if available.
     *
     * @param holder   the ViewHolder to bind data into
     * @param position the index of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitlistUser user = users.get(position);
        holder.tvName.setText(user.getName());

        // Safety check for removal
        holder.btnRemove.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                removeListener.onRemove(user, currentPosition);
            }
        });

        String profileImageUrl = user.getProfileImage();

        // Use Glide to load the URL, just like the comments feature!
        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .circleCrop() // Makes the image circular
                    .placeholder(R.drawable.default_avatar) 
                    // .error(R.drawable.default_avatar)       
                    .into(holder.ivProfile);
        } else {
            // Fallback if the user has no profile picture saved
            holder.ivProfile.setImageResource(R.drawable.default_avatar);
        }
    }

    /**
     * @return the total number of waitlisted users in the list
     */
    @Override
    public int getItemCount() { return users.size(); }

    /**
     * ViewHolder that holds references to the profile image, name, and remove button for a single waitlist row.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;
        ImageButton btnRemove;

        /**
         * @param itemView the inflated row view for a single waitlist entry
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
