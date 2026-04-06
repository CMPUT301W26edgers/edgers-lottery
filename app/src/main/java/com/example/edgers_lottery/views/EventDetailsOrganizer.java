//package com.example.edgers_lottery.views;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.edgers_lottery.R;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.WriterException;
//import com.google.zxing.common.BitMatrix;
//import com.journeyapps.barcodescanner.BarcodeEncoder;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.concurrent.TimeUnit;
//
///**
// * Activity that displays event details from the organizer's perspective.
// * Shows event name, description, entrant capacity, and a countdown to the registration deadline.
// * Allows the organizer to view the QR code, manage the waitlist and entrants, or edit the event.
// * Requires an {@code event_id} intent extra to load the correct Firestore document.
// * Reloads event data from Firestore each time the activity resumes, so edits made in
// * {@link CreateEditEventActivity} are always reflected when returning here.
// */
//public class EventDetailsOrganizer extends AppCompatActivity {
//
//    private String eventId;
//    private TextView locationName, entrantLimit, description, countdown;
//    private ImageView ivQrCode;
//
//    /**
//     * Initializes the activity, reads the event ID from the intent,
//     * and sets up views and listeners. Data loading is deferred to {@link #onResume()}
//     * so the display always reflects the latest Firestore state.
//     *
//     * @param savedInstanceState saved state from a previous instance, or null if first creation
//     */
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_event_details_organizer);
//
//        eventId = getIntent().getStringExtra("event_id");
//
//        initViews();
//        setupListeners();
//
//        if (eventId == null) {
//            Toast.makeText(this, "No event ID provided", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    /**
//     * Reloads event data from Firestore every time this activity becomes visible.
//     * This ensures that any changes saved in {@link CreateEditEventActivity} are
//     * immediately reflected when the user navigates back here.
//     */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (eventId != null) {
//            loadEventFromFirestore();
//        }
//    }
//
//    /**
//     * Binds all TextView fields to their corresponding layout views.
//     */
//    private void initViews() {
//        locationName = findViewById(R.id.tvEventTitle);
//        entrantLimit = findViewById(R.id.tvEntrantLimit);
//        description  = findViewById(R.id.tvDescription);
//        countdown    = findViewById(R.id.tvRegistrationCountdown);
//        ivQrCode     = findViewById(R.id.ivQrCode);
//    }
//
//    /**
//     * Fetches event data from Firestore and populates the UI fields.
//     * Calculates and displays the number of days remaining until the registration deadline.
//     */
//    private void loadEventFromFirestore() {
//        FirebaseFirestore.getInstance()
//                .collection("events")
//                .document(eventId)
//                .get()
//                .addOnSuccessListener(doc -> {
//                    if (doc.exists()) {
//                        String eventName  = doc.getString("name");
//                        String dateString = doc.getString("date");
//                        String desc       = doc.getString("description");
//                        Long capacity     = doc.getLong("capacity");
//
//                        locationName.setText(eventName != null ? eventName : "Unnamed Event");
//                        entrantLimit.setText("Entrants: " + (capacity != null ? capacity : 0));
//                        description.setText("Description: " + (desc != null ? desc : ""));
//
//                        // Generate and show QR inline if public; hide it if private
//                        Boolean isPublic = doc.getBoolean("ispublic");
//                        if (Boolean.TRUE.equals(isPublic)) {
//                            try {
//                                Bitmap qr = generateQrCode(eventId);
//                                ivQrCode.setImageBitmap(qr);
//                                ivQrCode.setVisibility(View.VISIBLE);
//                            } catch (WriterException e) {
//                                ivQrCode.setVisibility(View.GONE);
//                            }
//                        } else {
//                            ivQrCode.setVisibility(View.GONE);
//                        }
//
//                        if (dateString != null) {
//                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                            try {
//                                Date eventDate = sdf.parse(dateString);
//                                long diffMillis = eventDate.getTime() - new Date().getTime();
//                                long daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis);
//                                countdown.setText("Registration ends in " + daysRemaining + " days");
//                            } catch (ParseException e) {
//                                countdown.setText("Invalid registration date");
//                            }
//                        } else {
//                            countdown.setText("No registration date set");
//                        }
//                    } else {
//                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//    }
//
//    /**
//     * Attaches click listeners to the back, QR code, waitlist, entrant, and edit event buttons.
//     * Note: the edit button does NOT call finish() so this activity remains on the back stack
//     * and automatically reloads data via onResume() when the user returns from editing.
//     */
//    private void setupListeners() {
//        findViewById(R.id.btnBackEventDetails).setOnClickListener(v -> finish());
//
//        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
//            Intent intent = new Intent(this, EventWaitlistTab.class);
//            intent.putExtra("event_id", eventId);
//            startActivity(intent);
//        });
//
//        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
//            Intent intent = new Intent(this, EventEntrantOrganizer.class);
//            intent.putExtra("event_id", eventId);
//            startActivity(intent);
//        });
//
//        // Do NOT call finish() here — keep this activity alive on the back stack
//        // so onResume() can reload the updated data when returning from the edit screen
//        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
//            Intent intent = new Intent(this, CreateEditEventActivity.class);
//            intent.putExtra("event_id", eventId);
//            startActivity(intent);
//        });
//    }
//
//    /**
//     * Generates a QR code bitmap encoding the given string content.
//     *
//     * @param content the string to encode in the QR code, typically the event ID
//     * @return a {@link Bitmap} of the generated QR code
//     * @throws WriterException if the QR code could not be generated
//     */
//    private Bitmap generateQrCode(String content) throws WriterException {
//        BitMatrix bitMatrix = new MultiFormatWriter().encode(
//                content, BarcodeFormat.QR_CODE, 600, 600
//        );
//        return new BarcodeEncoder().createBitmap(bitMatrix);
//    }
//}



package com.example.edgers_lottery.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.services.LotteryService;
import com.example.edgers_lottery.services.NotificationService;
import com.google.firebase.firestore.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EventDetailsOrganizer extends AppCompatActivity {

    private String eventId;
    private TextView locationName, entrantLimit, description, countdown;
    private ImageView ivQrCode, ivEventImage;

    private FirebaseFirestore db;

    // ✅ NEW
    private boolean isPublicEvent = false;
    private View inviteContainer;
    private Button btnInviteUser;

    private List<Map<String, Object>> invitedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_organizer);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("event_id");

        initViews();
        setupListeners();

        if (eventId == null) {
            Toast.makeText(this, "No event ID provided", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventId != null) {
            loadEventFromFirestore();
        }
    }

    private void initViews() {
        locationName = findViewById(R.id.tvEventTitle);
        entrantLimit = findViewById(R.id.tvEntrantLimit);
        description  = findViewById(R.id.tvDescription);
        countdown    = findViewById(R.id.tvRegistrationCountdown);
        ivQrCode     = findViewById(R.id.ivQrCode);
        ivEventImage = findViewById(R.id.eventImage);
        // ✅ NEW
        inviteContainer = findViewById(R.id.inviteContainer);
        btnInviteUser = findViewById(R.id.btnInviteUser);


    }
    private void loadEventFromFirestore() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        String eventName  = doc.getString("name");
                        String dateString = doc.getString("date");
                        String desc       = doc.getString("description");
                        Long capacity     = doc.getLong("capacity");
                        String imageURL = doc.getString("poster");
                        List<Map<String, Object>> rawinvitedUsers =
                                (List<Map<String, Object>>) doc.get("invitedUsers");
                        if (rawinvitedUsers != null) {
                            invitedUsers
                                    = rawinvitedUsers;
                        } else {
                            invitedUsers = new ArrayList<>();
                        }


                        locationName.setText(eventName != null ? eventName : "Unnamed Event");
                        entrantLimit.setText("Entrants: " + (capacity != null ? capacity : 0));
                        description.setText("Description: " + (desc != null ? desc : ""));

                        if (imageURL != null) {
                            Glide.with(this).load(imageURL).placeholder(R.drawable.blank_event).into(ivEventImage);
                        } else {
                            ivEventImage.setImageResource(R.drawable.blank_event);
                        }
                        // ✅ Store isPublic
                        Boolean isPublic = doc.getBoolean("ispublic");
                        isPublicEvent = Boolean.TRUE.equals(isPublic);

                        // ✅ QR logic
                        if (isPublicEvent) {
                            try {
                                Bitmap qr = generateQrCode(eventId);
                                ivQrCode.setImageBitmap(qr);
                                ivQrCode.setVisibility(View.VISIBLE);
                            } catch (WriterException e) {
                                ivQrCode.setVisibility(View.GONE);
                            }
                            inviteContainer.setVisibility(View.GONE);
                            // ✅ SHOW invite UI


                        } else {
                            ivQrCode.setVisibility(View.GONE);
                            inviteContainer.setVisibility(View.VISIBLE);
                        }

                        // Countdown
                        if (dateString != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            try {
                                Date eventDate = sdf.parse(dateString);
                                long diffMillis = eventDate.getTime() - new Date().getTime();
                                long daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis);
                                countdown.setText("Registration ends in " + daysRemaining + " days");
                            } catch (ParseException e) {
                                countdown.setText("Invalid registration date");
                            }
                        } else {
                            countdown.setText("No registration date set");
                        }

                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupListeners() {

        findViewById(R.id.btnBackEventDetails).setOnClickListener(v -> finish());

        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, EventWaitlistTab.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, EventEntrantOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
        Button commentsBtn = findViewById(R.id.commentsBtn);
        commentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventCommentsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        // Add this to EventDetailsOrganizer, EventWaitlistTab, and EventEntrantOrganizer
        Button mapBtn = findViewById(R.id.mapBtn);
        if (mapBtn != null) {
            mapBtn.setOnClickListener(v -> {
                finish();
                Intent intent = new Intent(this, OrganizerWaitlistMapActivity.class);
                intent.putExtra("event_id", eventId); // Make sure the variable name matches their intent key
                startActivity(intent);
            });
        }
        findViewById(R.id.btnRunLottery).setOnClickListener(v -> {
            LotteryService.sampleWaitlist(eventId, result -> {
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

            });
        });
        findViewById(R.id.btnChosenEntrants).setOnClickListener(v -> {
            if (invitedUsers == null || invitedUsers.isEmpty()) {
                Toast.makeText(this, "No invited users", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder list = new StringBuilder();

            for (Map<String, Object> user : invitedUsers) {
                String name = (String) user.get("name");
                String email = (String) user.get("email");

                list.append(name != null ? name : "Unknown")
                        .append(" (")
                        .append(email != null ? email : "No email")
                        .append(")\n");
            }

            new AlertDialog.Builder(EventDetailsOrganizer.this)
                    .setTitle("Invited Users (" + invitedUsers.size() + ")")
                    .setMessage(list.toString())
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        findViewById(R.id.btnCancelledEntrants).setOnClickListener(v-> {

        });

        // ✅ INVITE BUTTON
        btnInviteUser.setOnClickListener(v -> showInviteDialog());
    }

    // ✅ Invite popup
    private void showInviteDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter user email");

        new AlertDialog.Builder(this)
                .setTitle("Invite User")
                .setView(input)
                .setPositiveButton("Send Invite", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (!email.isEmpty()) {
                        inviteUserByEmail(email);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ✅ Firestore logic
    private void inviteUserByEmail(String email) {

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot userDoc = query.getDocuments().get(0);
                    Map<String, Object> userData = userDoc.getData();

                    if (userData == null) {
                        Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get event invitedUsers instead of waitingList
                    db.collection("events")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(eventDoc -> {

                                List<Map<String, Object>> AllInvitedList =
                                        (List<Map<String, Object>>) eventDoc.get("AllInvitedUsers");

                                if (AllInvitedList == null) {
                                    AllInvitedList = new ArrayList<>();
                                }

                                // Prevent duplicates by id
                                String newUserId = (String) userData.get("id");

                                for (Map<String, Object> user : AllInvitedList) {
                                    if (newUserId.equals(user.get("id"))) {
                                        Toast.makeText(this, "User already invited", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                AllInvitedList.add(userData);

                                db.collection("events")
                                        .document(eventId)
                                        .update("AllInvitedUsers", AllInvitedList)
                                        .addOnSuccessListener(unused -> {
                                            String invitedUserId = (String) userData.get("id");
                                            android.util.Log.d("InviteDebug", "invitedUserId = " + invitedUserId);

                                            NotificationService.sendPrivateEventInvite(
                                                    invitedUserId,
                                                    eventId,
                                                    locationName.getText().toString()
                                            );

                                            Toast.makeText(this, "User invited!", Toast.LENGTH_SHORT).show();
                                        });
                            });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private Bitmap generateQrCode(String content) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                content, BarcodeFormat.QR_CODE, 600, 600
        );
        return new BarcodeEncoder().createBitmap(bitMatrix);
    }
}