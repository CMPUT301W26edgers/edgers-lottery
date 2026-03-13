package com.example.edgers_lottery; // Make sure this matches your package name!

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventHistoryAdapter extends RecyclerView.Adapter<EventHistoryAdapter.EventViewHolder> {

    private List<Event> eventList;
    private String currentUserId;
    private Context context;

    // Constructor to pass in the data
    public EventHistoryAdapter(Context context, List<Event> eventList, String currentUserId) {
        this.context = context;
        this.eventList = eventList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the single row XML layout we just made
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_history, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentEvent = eventList.get(position);

        // 1. Set the Event Name
        holder.eventNameText.setText(currentEvent.getName());

        // 2. Determine the User's Status
        String status = determineUserStatus(currentEvent);
        holder.statusText.setText(status);

        // Optional: You can change the background color of the status tag based on the text here!
        switch (status) {
            case "Accepted":
                // A nice success Green
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                break;
            case "Selected":
                // A bold Orange/Gold because it requires their attention!
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#FF9800"));
                break;
            case "In waitlist":
                // A neutral Gray
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#9E9E9E"));
                break;
            case "Rejected":
                // A solid Red
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#F44336"));
                break;
            default:
                // Default to black just in case
                holder.statusText.setTextColor(android.graphics.Color.parseColor("#000000"));
                break;
        }

        // 3. Handle the Click Event
        holder.itemView.setOnClickListener(v -> {
            if (status.equals("Selected")) {
                // Redirect to the Accept/Decline Screen
                Intent intent = new Intent(context, EventUserChoice.class);
                // Pass the event ID so the next screen knows which event they are accepting
                intent.putExtra("eventId", currentEvent.getId());
                context.startActivity(intent);
            } else if (status.equals("In waitlist")){
                // 🟢 Redirect to your teammate's Event Details Screen
                Intent intent = new Intent(context, EventDetailsActivity.class);

                // Pass that Firebase ID that you successfully saved earlier!
                intent.putExtra("eventId", currentEvent.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Helper method to figure out the user's status for this specific event.
     */
    private String determineUserStatus(Event event) {
        if (isUserInList(currentUserId, event.getInvitedUsers())) {
            return "Selected";
        } else if (isUserInList(currentUserId, event.getWaitingList())) {
            return "In waitlist";
        } else if (isUserInList(currentUserId, event.getEntrants())) {
            return "Accepted"; }
        else {
            // If they aren't in either, we assume they were rejected or canceled.
            // You can adjust this logic if you have a specific 'rejectedUsers' list!
            return "Rejected";
        }
    }

    /**
     * Helper method to safely check if our user's ID matches any User in the provided list.
     */
    private boolean isUserInList(String targetUserId, ArrayList<User> userList) {
        if (userList == null) return false;

        for (User user : userList) {
            // Change ".getUserId()" to whatever getter your team used in the User class!
            if (user.getId() != null && user.getId().equals(targetUserId)) {
                return true;
            }
        }
        return false;
    }

    // ViewHolder class that grabs the views from our XML
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameText;
        TextView statusText;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameText = itemView.findViewById(R.id.textHistoryEventName);
            statusText = itemView.findViewById(R.id.textHistoryStatus);
        }
    }
}