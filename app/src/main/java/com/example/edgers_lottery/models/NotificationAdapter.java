package com.example.edgers_lottery.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of {@link AppNotification} items.
 * Constructs the display message from the notification type and event name
 * rather than storing a pre-built message string.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<AppNotification> notifications;

    /** @param notifications list of notifications to display */
    public NotificationAdapter(List<AppNotification> notifications) {
        this.notifications = notifications;
    }

    /**
     * Inflates the row layout for a single notification item.
     * Uses a built-in two-line list layout — swap for a custom layout when styling.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a notification to a row. Message is constructed from {@code type} + {@code eventName}.
     * Timestamp is displayed if present.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification notif = notifications.get(position);

        // build the display message based on the lottery outcome type
        switch (notif.getType()) {
            case "SELECTED":
                holder.message.setText("You have been selected for " + notif.getEventName());
                break;
            case "NOT_SELECTED":
                holder.message.setText("You have unfortunately not been selected for " + notif.getEventName());
                break;
            case "JOINED_WAITLIST":
                // deferred — not yet triggered anywhere but handled for completeness
                holder.message.setText("You joined the waitlist for " + notif.getEventName());
                break;
            default:
                holder.message.setText(notif.getEventName());
        }

        // format and display timestamp if the notification has one
        if (notif.getTimestamp() != null) {
            holder.timestamp.setText(notif.getTimestamp().toDate().toString());
        }
    }

    /** @return total number of notifications in the list */
    @Override
    public int getItemCount() { return notifications.size(); }

    /**
     * Holds view references for a single notification row.
     * {@code message} displays the lottery outcome, {@code timestamp} shows when it was created.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(android.R.id.text1);
            timestamp = itemView.findViewById(android.R.id.text2);
        }
    }
}