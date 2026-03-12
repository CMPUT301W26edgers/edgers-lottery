package com.example.edgers_lottery;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.MultiFormatWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.widget.TextView;


public class EventDetailsOrganizer extends AppCompatActivity{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_organizer);

        Button btnQrCode = findViewById(R.id.btnQrCode);
        btnQrCode.setOnClickListener(v -> {
            try {
                Bitmap qr = generateQrCode("your-event-id-or-url-here");
                showQrCodeDialog(qr);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        });

        TextView countdown = findViewById(R.id.tvRegistrationCountdown);
        String dateString = getIntent().getStringExtra("registration_date");
        String eventName = getIntent().getStringExtra("eventName");
        int entrant = getIntent().getIntExtra("entrants", 0);
        String descrptionText = getIntent().getStringExtra("description");



        TextView LocationName = findViewById(R.id.tvEventTitle);
        //TextView Date = findViewById(R.id.tvDate);
        TextView entrantLimit = findViewById(R.id.tvEntrantLimit);
        TextView description = findViewById(R.id.tvDescription);

        LocationName.setText(eventName);
        //Date.setText(eventName);
        entrantLimit.setText("Entrant: " + entrant);
        description.setText("Description: " + descrptionText);

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


        ImageButton btnBackEventDetails = findViewById(R.id.btnBackEventDetails);
        btnBackEventDetails.setOnClickListener(v -> finish());

    }
}
