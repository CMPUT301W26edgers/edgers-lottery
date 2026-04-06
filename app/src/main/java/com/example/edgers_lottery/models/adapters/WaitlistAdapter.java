package com.example.edgers_lottery.models.adapters;

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
import com.example.edgers_lottery.models.core.WaitlistUser;

import java.util.List;

/**
 * RecyclerView adapter that displays a list of waitlisted users.
 * Each row shows the user's profile image, name, and a remove button.
 * Removal events are dispatched through {@link OnRemoveClickListener}.
 *
 * Optionally accepts an {@link OnLongClickListener} — when provided, long-pressing
 * a row triggers that callback (used for co-organizer assignment, US 02.09.01).
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

    /**
     * Listener interface for handling a long-press on a waitlisted user row.
     * Used to trigger co-organizer assignment (US 02.09.01).
     */
    public interface OnLongClickListener {
        /**
         * Called when a row is long-pressed.
         *
         * @param user the {@link WaitlistUser} that was long-pressed
         */
        void onLongClick(WaitlistUser user);
    }

    private final List<WaitlistUser> users;
    private final OnRemoveClickListener removeListener;
    private final OnLongClickListener longClickListener; // nullable

    /**
     * Constructs an adapter without a long-click listener.
     * Existing call sites remain unchanged.
     *
     * @param users          the list of {@link WaitlistUser} objects to display
     * @param removeListener the listener to notify when a user is removed
     */
    public WaitlistAdapter(List<WaitlistUser> users, OnRemoveClickListener removeListener) {
        this(users, removeListener, null);
    }

    /**
     * Constructs an adapter with an optional long-click listener.
     *
     * @param users             the list of {@link WaitlistUser} objects to display
     * @param removeListener    the listener to notify when a user is removed
     * @param longClickListener the listener to notify on row long-press, or null to disable
     */
    public WaitlistAdapter(List<WaitlistUser> users,
                           OnRemoveClickListener removeListener,
                           OnLongClickListener longClickListener) {
        this.users             = users;
        this.removeListener    = removeListener;
        this.longClickListener = longClickListener;
    }

    /**
     * Inflates the row layout for a waitlist item and returns a new {@link ViewHolder}.
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
     * remove button listener, optional long-press listener, and profile image.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitlistUser user = users.get(position);
        holder.tvName.setText(user.getName());

        // Remove button
        holder.btnRemove.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                removeListener.onRemove(user, currentPosition);
            }
        });

        // Long-press to assign co-organizer (only wired when listener is provided)
        if (longClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(user);
                return true; // consume the event
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }

        // Profile image
        String profileImageUrl = user.getProfileImage();
        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.default_avatar)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.default_avatar);
        }
    }

    /**
     * @return the total number of waitlisted users in the list
     */
    @Override
    public int getItemCount() { return users.size(); }

    /**
     * ViewHolder that holds references to the profile image, name, and remove button
     * for a single waitlist row.
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
            tvName    = itemView.findViewById(R.id.tvName);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}