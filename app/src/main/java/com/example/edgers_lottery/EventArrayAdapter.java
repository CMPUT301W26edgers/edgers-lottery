package com.example.edgers_lottery;

import android.widget.ArrayAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.content_events,
                    parent, false);
        } else {
            view = convertView;
        }
        Event event = getItem(position);
        if (event != null) {
            TextView titleTextView = view.findViewById(R.id.event_text);
            TextView organizerTextView = view.findViewById(R.id.organizer_text);
            TextView dateTextView = view.findViewById(R.id.date_text);
            TextView registrationEndTextView = view.findViewById(R.id.registration_end_text);

            titleTextView.setText(event.getName());
            organizerTextView.setText(event.getOrganizer().getName());
            dateTextView.setText(event.getDate());
            registrationEndTextView.setText(event.getRegistrationEnd());
            }
        return view;
        }


}
