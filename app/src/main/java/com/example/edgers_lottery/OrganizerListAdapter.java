package com.example.edgers_lottery;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class OrganizerListAdapter extends ArrayAdapter<User> {
    private Context context;

    public OrganizerListAdapter(Context context, List<User> organizers) {
        super(context, 0, organizers);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_organizer, parent, false);
        }

        // get organizer name and delete button from layout
        User organizer = getItem(position);
        TextView name = convertView.findViewById(R.id.organizerName);
        ImageButton delete = convertView.findViewById(R.id.deleteButton);
        name.setText(organizer.getName());

        // click listener for delete button
        delete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Organizer")
                    .setMessage("Delete this organizer and all their events?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (context instanceof OrganizerListActivity) {
                            ((OrganizerListActivity) context)
                                    .removeOrganizer(organizer.getId());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return convertView;
    }
}
