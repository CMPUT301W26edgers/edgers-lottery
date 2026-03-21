package com.example.edgers_lottery.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.edgers_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Array adapter for displaying {@link Event} objects in a ListView.
 * Each row shows the event name, organizer name (fetched from Firestore), date, and registration end date.
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {

    /**
     * @param context the current context
     * @param events  the list of events to display
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
    }

    /**
     * Inflates or recycles a row view and populates it with event data.
     * Organizer name is fetched asynchronously from Firestore using the event's organizer ID.
     *
     * @param position    the position of the item in the list
     * @param convertView a recycled view to reuse, or null if none available
     * @param parent      the parent view group
     * @return the populated row view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView != null ? convertView :
                LayoutInflater.from(getContext()).inflate(R.layout.content_events, parent, false);

        Event event = getItem(position);
        if (event != null) {
            TextView titleTextView = view.findViewById(R.id.event_text);
            TextView organizerTextView = view.findViewById(R.id.organizer_text);
            TextView dateTextView = view.findViewById(R.id.date_text);
            TextView registrationEndTextView = view.findViewById(R.id.registration_end_text);

            titleTextView.setText(event.getName() != null ? event.getName() : "Unknown Event");
            dateTextView.setText(event.getDate() != null ? event.getDate() : "Unknown Date");
            registrationEndTextView.setText(event.getRegistrationEnd() != null ? event.getRegistrationEnd() : "Unknown Registration End");

            if (event.getOrganizerId() != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(event.getOrganizerId())
                        .get()
                        .addOnSuccessListener(doc -> {
                            User organizer = doc.toObject(User.class);
                            assert organizer != null;
                            organizerTextView.setText(organizer.getName());
                        });
            } else {
                organizerTextView.setText("Unknown Organizer");
            }
        }
        return view;
    }
}