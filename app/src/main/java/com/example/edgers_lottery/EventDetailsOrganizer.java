package com.example.edgers_lottery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Dialog;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_organizer);

        eventId = getIntent().getStringExtra("event_id");

        initViews();
        setupListeners();
    }

    private void initViews() {
        String dateString = getIntent().getStringExtra("registration_date");
        String eventName = getIntent().getStringExtra("eventName");
        int entrant = getIntent().getIntExtra("entrants", 0);
        String descriptionText = getIntent().getStringExtra("description");

        TextView locationName = findViewById(R.id.tvEventTitle);
        TextView entrantLimit = findViewById(R.id.tvEntrantLimit);
        TextView description = findViewById(R.id.tvDescription);
        TextView countdown = findViewById(R.id.tvRegistrationCountdown);

        locationName.setText(eventName);
        entrantLimit.setText("Entrant: " + entrant);
        description.setText("Description: " + descriptionText);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date eventDate = sdf.parse(dateString);
            Date today = new Date();
            long diffMillis = eventDate.getTime() - today.getTime();
            long daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis);
            countdown.setText("Registration ends in " + daysRemaining + " days");
        } catch (ParseException e) {
            e.printStackTrace();
            countdown.setText("Invalid registration date");
        }
    }

    private void setupListeners() {
        findViewById(R.id.btnBackEventDetails).setOnClickListener(v -> finish());

        findViewById(R.id.btnQrCode).setOnClickListener(v -> {
            try {
                Bitmap qr = generateQrCode("your-event-id-or-url-here");
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

        TextView btnClose = dialog.findViewById(R.id.btnCloseWindow);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private Bitmap generateQrCode(String content) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                content, BarcodeFormat.QR_CODE, 600, 600
        );
        return new BarcodeEncoder().createBitmap(bitMatrix);
    }
}