package com.example.edgers_lottery.models;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.views.OrganizerListActivity;

import java.util.List;

/**
 * Adapter that populates the organizer list in {@link OrganizerListActivity}.
 * Each row displays the organizer's name and a delete button that prompts
 * for confirmation before removing the organizer and their events.
 */
public class OrganizerListAdapter extends ArrayAdapter<User> {
    private Context context;

    /**
     * Constructs a new adapter for the given list of organizers.
     *
     * @param context    the hosting context, expected to be an {@link OrganizerListActivity}
     * @param organizers the list of {@link User} objects with the ORGANIZER role
     */
    public OrganizerListAdapter(Context context, List<User> organizers) {
        super(context, 0, organizers);
        this.context = context;
    }

    /**
     * Inflates or recycles a row view and binds the organizer's name and delete button.
     * The delete button shows a confirmation dialog before calling
     * {@link OrganizerListActivity#removeOrganizer}.
     *
     * @param position    the index of the item in the list
     * @param convertView a recycled view to reuse, or null if a new view must be inflated
     * @param parent      the parent ViewGroup the view will be attached to
     * @return the populated row view for the organizer at the given position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_organizer, parent, false);
        }

        User organizer = getItem(position);
        TextView name = convertView.findViewById(R.id.organizerName);
        ImageButton delete = convertView.findViewById(R.id.deleteButton);
        name.setText(organizer.getName());

        delete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Organizer")
                    .setMessage("Delete this organizer for violating app policies?")
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