package com.example.edgers_lottery.models;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventCarouselAdapter extends RecyclerView.Adapter<EventCarouselAdapter.CarouselViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final Context context;
    private final List<Event> events;
    private OnEventClickListener listener;

    public EventCarouselAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_card, parent, false);
        return new CarouselViewHolder(view);
    }

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
        int capacity = event.getCapacity();
        int entrantCount = event.getEntrants() != null ? event.getEntrants().size() : 0;
        if (capacity > 0 && entrantCount >= capacity) {
            holder.status.setText("FULL");
            holder.status.setBackgroundResource(R.drawable.badge_full);
        } else {
            holder.status.setText("OPEN");
            holder.status.setBackgroundResource(R.drawable.badge_open);
        }

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

        // Poster image — same Glide setup with blank_event fallback
        if (event.getPoster() == null) {
            holder.poster.setImageResource(R.drawable.blank_event);
        } else {
            Glide.with(context)
                    .load(event.getPoster())
                    .placeholder(R.drawable.blank_event)
                    .centerCrop()
                    .into(holder.poster);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView name, date, location, status, organizer, registrationEnd;

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