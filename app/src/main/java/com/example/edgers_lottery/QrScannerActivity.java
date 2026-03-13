


package com.example.edgers_lottery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QrScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScanner;
    private LinearLayout actionPanel;
    private TextView tvScannedLabel;
    private Button btnGoToEvent;
    private Button btnScanAgain;
    private boolean isScanned = false;
    private String scannedEventId = null;

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeScanner  = findViewById(R.id.barcodeScanner);
        actionPanel     = findViewById(R.id.actionPanel);
        tvScannedLabel  = findViewById(R.id.tvScannedLabel);
        btnGoToEvent    = findViewById(R.id.btnGoToEvent);
        btnScanAgain    = findViewById(R.id.btnScanAgain);

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

    private void startScanner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
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

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        if (!isScanned) barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }
}