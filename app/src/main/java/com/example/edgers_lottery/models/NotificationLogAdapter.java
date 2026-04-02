package com.example.edgers_lottery.models;

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

public class NotificationLogAdapter extends ArrayAdapter<Map<String, Object>> {

    private Context context;

    public NotificationLogAdapter(Context context, List<Map<String, Object>> notifications) {
        super(context, 0, notifications);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_admin_notification_log, parent, false);
        }

        Map<String, Object> notif = getItem(position);

        TextView eventName = convertView.findViewById(R.id.logEventName);
        TextView type = convertView.findViewById(R.id.logType);
        TextView status = convertView.findViewById(R.id.logStatus);
        TextView time = convertView.findViewById(R.id.logTime);

        // Extract data
        String eventStr = (String) notif.get("eventName");
        String typeStr = (String) notif.get("type");
        Boolean isRead = (Boolean) notif.get("isRead");
        com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) notif.get("timestamp");

        // Set UI values
        eventName.setText(eventStr != null ? eventStr : "Unknown Event");
        type.setText(typeStr != null ? typeStr : "Unknown Type");
        time.setText(ts != null ? ts.toDate().toString() : "Unknown Date");

        // Style the read status
        if (isRead != null && isRead) {
            status.setText("Read");
            status.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            status.setText("Unread");
            status.setTextColor(Color.parseColor("#F44336")); // Red
        }

        // Make the tile unclickable so it behaves just as a visual display
        convertView.setClickable(false);

        return convertView;
    }
}