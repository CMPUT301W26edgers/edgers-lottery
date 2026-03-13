package com.example.edgers_lottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Dialog;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EventDetailsOrganizer extends AppCompatActivity {

    private String eventId;
    private TextView locationName, entrantLimit, description, countdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_organizer);

        eventId = getIntent().getStringExtra("event_id");

        initViews();
        setupListeners();

        if (eventId != null) {
            loadEventFromFirestore();
        } else {
            Toast.makeText(this, "No event ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        locationName = findViewById(R.id.tvEventTitle);
        entrantLimit = findViewById(R.id.tvEntrantLimit);
        description = findViewById(R.id.tvDescription);
        countdown = findViewById(R.id.tvRegistrationCountdown);
    }

    private void loadEventFromFirestore() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String eventName  = doc.getString("name");
                        String dateString = doc.getString("date");
                        String desc       = doc.getString("description");
                        Long capacity     = doc.getLong("capacity");

                        locationName.setText(eventName != null ? eventName : "Unnamed Event");
                        entrantLimit.setText("Entrants: " + (capacity != null ? capacity : 0));
                        description.setText("Description: " + (desc != null ? desc : ""));

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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void setupListeners() {
        findViewById(R.id.btnBackEventDetails).setOnClickListener(v -> finish());

        findViewById(R.id.btnQrCode).setOnClickListener(v -> {
            try {
                Bitmap qr = generateQrCode(eventId != null ? eventId : "no-id");
                showQrCodeDialog(qr);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventWaitlistTab.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventEntrantOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
    }

    private void showQrCodeDialog(Bitmap qrBitmap) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_qr_card_view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        ImageView ivQrCode = dialog.findViewById(R.id.ivQrCode);
        ivQrCode.setImageBitmap(qrBitmap);

        dialog.findViewById(R.id.btnCloseWindow).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private Bitmap generateQrCode(String content) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                content, BarcodeFormat.QR_CODE, 600, 600
        );
        return new BarcodeEncoder().createBitmap(bitMatrix);
    }
}