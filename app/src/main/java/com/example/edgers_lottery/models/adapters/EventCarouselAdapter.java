package com.example.edgers_lottery.models.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.core.Event;
import com.example.edgers_lottery.models.core.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * RecyclerView adapter for displaying {@link Event} objects as swipeable cards
 * in the home screen carousel (CardStackView).
 * Each card shows the event poster, name, date, location, registration countdown,
 * organizer name (fetched asynchronously from Firestore), and an open/full status badge.
 * Navigation is handled by the CardStackView's swipe listener in HomeActivity,
 * not by individual card click listeners.
 */
public class EventCarouselAdapter extends RecyclerView.Adapter<EventCarouselAdapter.CarouselViewHolder> {

    private final Context context;
    private final List<Event> events;

    /**
     * Constructs a new EventCarouselAdapter.
     *
     * @param context the current context
     * @param events  the list of events to display as cards
     */
    public EventCarouselAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    /**
     * Inflates the card layout and returns a new {@link CarouselViewHolder}.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type (not used; only one card type exists)
     * @return a new {@link CarouselViewHolder} wrapping the inflated card view
     */
    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_card, parent, false);
        return new CarouselViewHolder(view);
    }

    /**
     * Binds event data to the card at the given position.
     * Populates the poster image via Glide, sets the event name, date, location,
     * registration countdown, and open/full status badge.
     * Organizer name is fetched asynchronously from Firestore using the event's organizer ID.
     *
     * @param holder   the {@link CarouselViewHolder} to bind data into
     * @param position the position of the event in the list
     */
    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Event event = events.get(position);

        holder.name.setText(event.getName() != null ? event.getName() : "Unknown Event");

        holder.date.setText(event.getDate() != null
                ? "📅 " + EventArrayAdapter.formatDate(event.getDate())
                : "📅 Unknown Date");

        holder.location.setText(event.getLocation() != null
                ? "📍 " + event.getLocation()
                : "📍 Unknown Location");

        holder.registrationEnd.setText(event.getRegistrationEnd() != null
                ? EventArrayAdapter.timeUntilRegistration(event.getRegistrationEnd())
                : "Unknown Registration End");

        // Set open/full status badge based on capacity vs confirmed entrants
        int capacity = event.getCapacity();
        int entrantCount = event.getEntrants() != null ? event.getEntrants().size() : 0;
        if (capacity > 0 && entrantCount >= capacity) {
            holder.status.setText("FULL");
            holder.status.setBackgroundResource(R.drawable.badge_full);
        } else {
            holder.status.setText("OPEN");
            holder.status.setBackgroundResource(R.drawable.badge_open);
        }

        // Fetch organizer name asynchronously from Firestore
        if (event.getOrganizerId() != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(event.getOrganizerId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        User organizer = doc.toObject(User.class);
                        if (organizer != null) {
                            holder.organizer.setText("☆ " + organizer.getName());
                        } else {
                            holder.organizer.setText("☆ Unknown Organizer");
                        }
                    })
                    .addOnFailureListener(e -> holder.organizer.setText("☆ Unknown Organizer"));
        } else {
            holder.organizer.setText("☆ Unknown Organizer");
        }

        // Load poster image with Glide, falling back to blank_event placeholder
        if (event.getPoster() == null) {
            holder.poster.setImageResource(R.drawable.blank_event);
        } else {
            Glide.with(context)
                    .load(event.getPoster())
                    .placeholder(R.drawable.blank_event)
                    .centerCrop()
                    .into(holder.poster);
        }
    }

    /**
     * Returns the total number of event cards in the adapter.
     *
     * @return the size of the events list
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Replaces the current event list with a new one and refreshes the adapter.
     *
     * @param newEvents the new list of events to display
     */
    public void setEvents(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for a single event card in the carousel.
     * Holds references to all views within the card layout.
     */
    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView name;
        TextView date;
        TextView location;
        TextView status;
        TextView organizer;
        TextView registrationEnd;
        /**
         * Constructs a CarouselViewHolder and binds all child views.
         *
         * @param itemView the inflated card view
         */
        CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.ivCarouselPoster);
            name = itemView.findViewById(R.id.tvCarouselName);
            date = itemView.findViewById(R.id.tvCarouselDate);
            location = itemView.findViewById(R.id.tvCarouselLocation);
            status = itemView.findViewById(R.id.tvCarouselStatus);
            organizer = itemView.findViewById(R.id.tvCarouselOrganizer);
            registrationEnd = itemView.findViewById(R.id.tvCarouselRegistration);
        }
    }
}