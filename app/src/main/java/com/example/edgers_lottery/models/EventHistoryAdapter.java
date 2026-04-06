package com.example.edgers_lottery.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.views.EventUserChoice;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.views.EventDetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying a user's event history.
 * Each row shows the event name and the current user's status for that event
 * (Accepted, Selected, In waitlist, or Rejected).
 * Clicking a row navigates to the appropriate screen based on the user's status.
 */
public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.EventViewHolder> {

    private List<Event> eventList;
    private String currentUserId;
    private Context context;

    /**
     * @param context       the current context
     * @param eventList     the list of events to display
     * @param currentUserId the ID of the currently logged-in user
     */
    public EventHistoryAdapter(Context context, List<Event> eventList, String currentUserId) {
        this.context = context;
        this.eventList = eventList;
        this.currentUserId = currentUserId;
    }

    /**
     * Inflates the row layout for a single event history item.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new view
     * @return a new {@link EventViewHolder} holding the inflated row view
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_history, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the row view at the given position.
     * Sets the event name, determines and colors the user's status label,
     * and attaches a click listener that navigates based on status.
     *
     * @param holder   the {@link EventViewHolder} to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = eventList.get(position);

        holder.eventNameText.setText(currentEvent.getName());

        String status = determineUserStatus(currentEvent);
        holder.statusText.setText(status);

        switch (status) {
            case "Accepted":
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                break;
            case "Selected":
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#FF9800"));
                break;
            case "In waitlist":
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
                break;
            case "Rejected":
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#F44336"));
                break;
            default:
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#000000"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (status.equals("Selected")) {
                Intent intent = new Intent(context, EventUserChoice.class);
                intent.putExtra("eventId", currentEvent.getId());
                context.startActivity(intent);
            } else if (status.equals("In waitlist")) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("eventId", currentEvent.getId());
                context.startActivity(intent);
            } else if (status.equals("Pending Invite")) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("eventId", currentEvent.getId());
                intent.putExtra("isPendingInvite", true);
                context.startActivity(intent);
            }
        });
    }

    /**
     * @return the total number of events in the list
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Determines the current user's participation status for the given event
     * by checking which list they appear in.
     *
     * @param event the {@link Event} to check
     * @return a status string: "Selected", "In waitlist", "Accepted", or "Rejected"
     */
    private String determineUserStatus(Event event) {
        if (isUserInList(currentUserId, event.getInvitedUsers())) {
            return "Selected";
        } else if (isUserInList(currentUserId, event.getWaitingList())) {
            return "In waitlist";
        } else if (isUserInList(currentUserId, event.getEntrants())) {
            return "Accepted";
        } else if (isUserInList(currentUserId, event.getDeclinedUsers())) {
            return "Rejected";
        } else if (isUserInList(currentUserId, event.getAllInvitedUsers())) {
            return "Pending Invite";
        } else {
            return "Rejected";
        }
    }

    /**
     * Checks whether a user with the given ID exists in the provided list.
     *
     * @param targetUserId the ID to search for
     * @param userList     the list of {@link User} objects to search
     * @return true if the user is found, false otherwise
     */
    private boolean isUserInList(String targetUserId, ArrayList<User> userList) {
        if (userList == null) return false;
        for (User user : userList) {
            if (user.getId() != null && user.getId().equals(targetUserId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * ViewHolder that holds references to the event name and status TextViews for a single row.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventNameText;
        TextView statusText;

        /**
         * @param itemView the inflated row view for a single event history item
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameText = itemView.findViewById(R.id.textHistoryEventName);
            statusText = itemView.findViewById(R.id.textHistoryStatus);
        }
    }
}