package com.example.edgers_lottery.views;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
public class NavigationHelper {

    public static boolean guardEventId(Context context, String eventId) {
        if (eventId == null) {
            Toast.makeText(context, "Create the event first", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static void goToEventDetails(Context context, String eventId) {
        Intent intent = new Intent(context, EventDetailsOrganizer.class);
        intent.putExtra("event_id", eventId);
        context.startActivity(intent);
    }

    public static void goToWaitlist(Context context, String eventId) {
        Intent intent = new Intent(context, EventWaitlistTab.class);
        intent.putExtra("event_id", eventId);
        context.startActivity(intent);
    }

    public static void goToEntrants(Context context, String eventId) {
        Intent intent = new Intent(context, EventEntrantOrganizer.class);
        intent.putExtra("event_id", eventId);
        context.startActivity(intent);
    }

    public static void goToOrganizerHome(Context context) {
        context.startActivity(new Intent(context, OrganizerHomeActivity.class));
    }
}
