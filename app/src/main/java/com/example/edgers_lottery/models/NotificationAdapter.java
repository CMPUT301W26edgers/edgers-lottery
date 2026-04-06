package com.example.edgers_lottery.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.R;

import java.util.Date;
import java.util.List;

/**
 * Adapter class for binding a list of {@link AppNotification} objects to a {@link RecyclerView}.
 * This adapter handles the layout inflation and data binding for individual notification items,
 * including generating contextual messages based on the notification type, calculating relative
 * timestamps (e.g., "Just now", "2 hours ago"), and toggling unread status indicators.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    /**
     * The list of notifications to be displayed in the RecyclerView.
     */
    private final List<AppNotification> notifications;

    /**
     * Constructs a new NotificationAdapter with the provided list of notifications.
     *
     * @param notifications The list of {@link AppNotification} objects to display.
     */
    public NotificationAdapter(List<AppNotification> notifications) {
        this.notifications = notifications;
    }

    /**
     * Called when the RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
     * Inflates the custom notification item layout.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link ViewHolder} that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display the data at the specified position.
     * Updates the contents of the {@link ViewHolder#itemView} to reflect the notification at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification n = notifications.get(position);

        // Build contextual message from notification type
        String message;
        switch (n.getType() != null ? n.getType() : "") {

            case "PRIVATE_EVENT_INVITE":
                message = "You have been invited to a private event: " + n.getEventName();
                break;

            case "SELECTED":
                message = "You have been selected for " + n.getEventName();
                break;

            case "NOT_SELECTED":
                message = "You have unfortunately not been selected for " + n.getEventName();
                break;

            case "JOINED_WAITLIST":
                message = "You joined the waitlist for " + n.getEventName();
                break;

            case "CANCELLED":
                message = "You have declined your invitation to " + n.getEventName();
                break;
            case "WAITLIST_UPDATE":
                message = "The organizer has sent you an update for " + n.getEventName();
                break;
            default:
                message = n.getEventName();
        }

        holder.tvMessage.setText(message);

        // Process and display relative time ago
        if (n.getTimestamp() != null) {
            holder.tvTime.setText(getTimeAgo(n.getTimestamp().toDate()));
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        // Toggle visibility of the unread indicator dot
        holder.unreadDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of notifications.
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Helper method to calculate the relative time difference between a given date and the current time.
     *
     * @param date The past date to compare against the current system time.
     * @return A formatted string representing the time elapsed (e.g., "Just now", "5 minutes ago", "1 day ago").
     */
    private String getTimeAgo(Date date) {
        long diffMs   = System.currentTimeMillis() - date.getTime();
        long diffSec  = diffMs / 1000;
        long diffMin  = diffSec / 60;
        long diffHour = diffMin / 60;
        long diffDay  = diffHour / 24;
        long diffWeek = diffDay / 7;

        if (diffSec  < 60)  return "Just now";
        if (diffMin  < 60)  return diffMin  + (diffMin  == 1 ? " minute ago" : " minutes ago");
        if (diffHour < 24)  return diffHour + (diffHour == 1 ? " hour ago"   : " hours ago");
        if (diffDay  < 7)   return diffDay  + (diffDay  == 1 ? " day ago"    : " days ago");
        return diffWeek + (diffWeek == 1 ? " week ago" : " weeks ago");
    }

    /**
     * ViewHolder class that describes an item view and metadata about its place within the RecyclerView.
     * Holds references to the UI components for a single notification item to avoid repeated view lookups.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /** TextView displaying the primary notification message. */
        TextView tvMessage;

        /** TextView displaying the relative time elapsed since the notification was sent. */
        TextView tvTime;

        /** View acting as a visual indicator (red dot) for unread notifications. */
        View unreadDot;

        /**
         * Constructs a new ViewHolder, binding the UI components to their corresponding view IDs.
         *
         * @param itemView The top-level view of the custom notification item layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage  = itemView.findViewById(R.id.tvMessage);
            tvTime     = itemView.findViewById(R.id.tvTime);
            unreadDot  = itemView.findViewById(R.id.unreadDot);
        }
    }
}