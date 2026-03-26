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

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<AppNotification> notifications;

    public NotificationAdapter(List<AppNotification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification n = notifications.get(position);

        // build message from type
        String message;
        switch (n.getType() != null ? n.getType() : "") {

            case "PRIVATE_EVENT_INVITE":
                message = "You have been selected for a private event: " + n.getEventName();
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

            default:
                message = n.getEventName();
        }

        holder.tvMessage.setText(message);

        // time ago
        if (n.getTimestamp() != null) {
            holder.tvTime.setText(getTimeAgo(n.getTimestamp().toDate()));
            holder.tvTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        // unread red dot
        holder.unreadDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        View unreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage  = itemView.findViewById(R.id.tvMessage);
            tvTime     = itemView.findViewById(R.id.tvTime);
            unreadDot  = itemView.findViewById(R.id.unreadDot);
        }
    }
}