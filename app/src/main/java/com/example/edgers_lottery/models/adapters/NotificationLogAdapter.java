package com.example.edgers_lottery.models.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.edgers_lottery.R;

import java.util.List;
import java.util.Map;

/**
 * Adapter class for binding a list of notification log entries to a ListView.
 * Unlike standard adapters that use domain models, this adapter directly consumes a list
 * of {@code Map<String, Object>} representing raw data fetched from a Firestore array.
 * * It is primarily used in the admin interface to display a read-only history of notifications
 * sent to a specific user, formatting them into styled cards.
 */
public class NotificationLogAdapter extends ArrayAdapter<Map<String, Object>> {

    /**
     * The context in which the adapter is running, used for layout inflation and resource access.
     */
    private Context context;

    /**
     * Constructs a new NotificationLogAdapter.
     *
     * @param context       The current context (e.g., the hosting Activity).
     * @param notifications The list of notification logs, where each log is represented
     * as a key-value map directly from Firestore.
     */
    public NotificationLogAdapter(Context context, List<Map<String, Object>> notifications) {
        super(context, 0, notifications);
        this.context = context;
    }

    /**
     * Provides a view for an AdapterView (ListView).
     * Inflates the custom log item layout, extracts data from the map, populates the
     * TextViews, and applies dynamic styling (such as color-coding the read status).
     *
     * @param position    The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible. If null, a new view is inflated.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the layout if a recycled view is not available
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_admin_notification_log, parent, false);
        }

        // Retrieve the notification data map for this position
        Map<String, Object> notif = getItem(position);

        // Bind UI components
        TextView eventName = convertView.findViewById(R.id.logEventName);
        TextView type = convertView.findViewById(R.id.logType);
        TextView status = convertView.findViewById(R.id.logStatus);
        TextView time = convertView.findViewById(R.id.logTime);

        // Extract data from the Firestore map
        String eventStr = (String) notif.get("eventName");
        String typeStr = (String) notif.get("type");
        Boolean isRead = (Boolean) notif.get("isRead");
        com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) notif.get("timestamp");

        // Set UI values with fallbacks for null or missing data
        eventName.setText(eventStr != null ? eventStr : "Unknown Event");
        type.setText(typeStr != null ? typeStr : "Unknown Type");
        time.setText(ts != null ? ts.toDate().toString() : "Unknown Date");

        // Style the read status text and color
        if (isRead != null && isRead) {
            status.setText("Read");
            status.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            status.setText("Unread");
            status.setTextColor(Color.parseColor("#F44336")); // Red
        }

        // Make the tile unclickable so it behaves strictly as a visual display card
        // and does not intercept touch events from the parent layout or dialog.
        convertView.setClickable(false);

        return convertView;
    }
}