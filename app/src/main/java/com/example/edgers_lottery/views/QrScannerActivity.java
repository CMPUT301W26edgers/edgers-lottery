package com.example.edgers_lottery.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.edgers_lottery.R;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**
 * Activity that provides a QR code scanner for joining lottery events.
 *
 * <p>Uses the ZXing barcode scanning library to continuously scan for QR codes.
 * When a valid QR code is detected, its text is treated as a Firestore event ID
 * and the user is offered the option to navigate to that event's details page
 * or scan a different code.</p>
 *
 * <p>Camera permission is requested at runtime if not already granted.</p>
 */
public class QrScannerActivity extends AppCompatActivity {

    /** The ZXing barcode scanner view that handles camera preview and decoding. */
    private DecoratedBarcodeView barcodeScanner;

    /** Panel shown after a successful scan, containing the action buttons. */
    private LinearLayout actionPanel;

    /** Label displayed to the user after a QR code has been scanned. */
    private TextView tvScannedLabel;

    /** Button that navigates to the scanned event's details page. */
    private Button btnGoToEvent;

    /** Button that dismisses the action panel and resumes scanning. */
    private Button btnScanAgain;
    /** Button the send us back to the main actvity */
    private ImageButton btnBack;


    /**
     * Flag indicating whether a QR code has already been scanned in the
     * current session. Prevents duplicate decode callbacks from firing.
     */
    private boolean isScanned = false;

    /** The event ID extracted from the most recently scanned QR code, or {@code null} if none. */
    private String scannedEventId = null;

    /** Request code used to identify the camera permission request result. */
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    /**
     * Initializes the activity, binds UI components, sets up button listeners,
     * and requests camera permission if it has not already been granted.
     *
     * <p>If camera permission is already available, scanning begins immediately.
     * Otherwise, a runtime permission request is made and scanning starts upon
     * the user granting access.</p>
     *
     * @param savedInstanceState the previously saved instance state, or {@code null}
     *                           if this is a fresh start
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeScanner  = findViewById(R.id.barcodeScanner);
        actionPanel     = findViewById(R.id.actionPanel);
        tvScannedLabel  = findViewById(R.id.tvScannedLabel);
        btnGoToEvent    = findViewById(R.id.btnGoToEvent);
        btnScanAgain    = findViewById(R.id.btnScanAgain);
        btnBack         = findViewById(R.id.backButton);

        btnGoToEvent.setOnClickListener(v -> {
            if (scannedEventId != null) {
                Intent intent = new Intent(this, EventDetailsActivity.class);
                intent.putExtra("eventId", scannedEventId);
                startActivity(intent);
                finish();
            }
        });

        btnScanAgain.setOnClickListener(v -> {
            isScanned = false;
            scannedEventId = null;
            actionPanel.setVisibility(View.GONE);
            barcodeScanner.resume();
        });
        btnBack.setOnClickListener(v -> {
            finish();
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }

        barcodeScanner.setStatusText("");
    }

    /**
     * Begins continuous barcode decoding using the camera.
     *
     * <p>Once a non-null result is decoded and no prior scan has been recorded,
     * the scanner pauses and the action panel is shown on the UI thread.
     * Subsequent decode callbacks are ignored until the user taps "Scan Again".</p>
     */
    private void startScanner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            /**
             * Called when a barcode is successfully decoded.
             *
             * <p>Stores the scanned text as the event ID, pauses the scanner,
             * and reveals the action panel. Has no effect if a scan has already
             * been recorded or if the result is {@code null}.</p>
             *
             * @param result the decoded barcode result, or {@code null} if decoding failed
             */
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getText() == null) return;
                if (isScanned) return;
                isScanned = true;

                scannedEventId = result.getText();
                android.util.Log.d("QrScannerActivity", "Scanned event ID: " + scannedEventId);

                runOnUiThread(() -> {
                    barcodeScanner.pause();
                    actionPanel.setVisibility(View.VISIBLE);
                });
            }

            /**
             * Called when the scanner detects possible barcode candidate points
             * in the camera preview. No action is taken here.
             *
             * @param resultPoints the list of candidate result points detected
             */
            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    /**
     * Handles the result of the camera permission request.
     *
     * <p>If the user grants camera access, scanning is started immediately.
     * If permission is denied, the scanner remains inactive.</p>
     *
     * @param requestCode  the request code passed to {@link ActivityCompat#requestPermissions}
     * @param permissions  the requested permissions
     * @param grantResults the grant results for each requested permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        }
    }

    /**
     * Resumes the barcode scanner when the activity returns to the foreground,
     * but only if no QR code has been scanned yet in the current session.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!isScanned) barcodeScanner.resume();
    }

    /**
     * Pauses the barcode scanner when the activity moves to the background
     * to conserve resources and release the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }
}