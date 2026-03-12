//package com.example.edgers_lottery;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.zxing.ResultPoint;
//import com.journeyapps.barcodescanner.BarcodeCallback;
//import com.journeyapps.barcodescanner.BarcodeResult;
//import com.journeyapps.barcodescanner.DecoratedBarcodeView;
//
//import java.util.List;
//
//public class QrScannerActivity extends AppCompatActivity {
//
//    private DecoratedBarcodeView barcodeScanner;
//    private TextView tvResultLabel;
//    private TextView tvRawResult;
//    private Button btnScanAgain;
//    private boolean isScanned = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_qr_scanner);
//
//        barcodeScanner = findViewById(R.id.barcodeScanner);
//        tvResultLabel  = findViewById(R.id.tvResultLabel);
//        tvRawResult    = findViewById(R.id.tvRawResult);
//        btnScanAgain   = findViewById(R.id.btnScanAgain);
//
//        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
//
//        btnScanAgain.setOnClickListener(v -> {
//            isScanned = false;
//            tvResultLabel.setText("Scan a QR Code");
//            tvRawResult.setText("Point your camera at a QR code to see its contents here.");
//            btnScanAgain.setVisibility(View.GONE);
//            barcodeScanner.resume();
//        });
//
//        barcodeScanner.decodeContinuous(new BarcodeCallback() {
//            @Override
//            public void barcodeResult(BarcodeResult result) {
//                if (result == null || result.getText() == null) return;
//                if (isScanned) return;
//                isScanned = true;
//
//                String scannedValue = result.getText();
//
//                runOnUiThread(() -> {
//                    tvResultLabel.setText("QR Code Contents:");
//                    tvRawResult.setText(scannedValue);
//                    btnScanAgain.setVisibility(View.VISIBLE);
//                    barcodeScanner.pause();
//                });
//            }
//
//            @Override
//            public void possibleResultPoints(List<ResultPoint> resultPoints) {
//                // required but not needed
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!isScanned) {
//            barcodeScanner.resume();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        barcodeScanner.pause();
//    }
//}


package com.example.edgers_lottery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private TextView tvResultLabel;
    private TextView tvRawResult;
    private Button btnScanAgain;
    private boolean isScanned = false;

    private static final int CAMERA_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeScanner = findViewById(R.id.barcodeScanner);
        tvResultLabel  = findViewById(R.id.tvResultLabel);
        tvRawResult    = findViewById(R.id.tvRawResult);
        btnScanAgain   = findViewById(R.id.btnScanAgain);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnScanAgain.setOnClickListener(v -> {
            isScanned = false;
            tvResultLabel.setText("Scan a QR Code");
            tvRawResult.setText("Point your camera at a QR code to see its contents here.");
            btnScanAgain.setVisibility(View.GONE);
            barcodeScanner.resume();
        });

        // Check permission first before starting camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    private void startScanner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getText() == null) return;
                if (isScanned) return;
                isScanned = true;

                String scannedValue = result.getText();

                runOnUiThread(() -> {
                    tvResultLabel.setText("QR Code Contents:");
                    tvRawResult.setText(scannedValue);
                    btnScanAgain.setVisibility(View.VISIBLE);
                    barcodeScanner.pause();
                });
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    // Called after user accepts or denies the permission popup
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner(); // permission granted, start camera
            } else {
                tvRawResult.setText("Camera permission denied. Please enable it in Settings.");
            }
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