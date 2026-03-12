package com.example.edgers_lottery;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.slider.Slider;
import java.util.Calendar;
import androidx.appcompat.app.AlertDialog;
import android.view.View;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


public class CreateEditEventActivity extends AppCompatActivity {

    private ImageView ivImage;
    private EditText registrationDeadlineInput, priceInput, descriptionInput;
    private TextInputLayout priceLayout;
    private SwitchMaterial swGeo, swWaitlist;
    private Slider sliderEntrants;
    private EditText eventNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_end_event);

        initViews();
        setupListeners();
        setupEdgeToEdge();
    }

    private void initViews() {
        registrationDeadlineInput = findViewById(R.id.registration_deadline);
        priceLayout = findViewById(R.id.priceLayout);
        priceInput = findViewById(R.id.price);
        descriptionInput = findViewById(R.id.editTilDescription);
        swGeo = findViewById(R.id.swGeo);
        swWaitlist = findViewById(R.id.swWaitlist);
        sliderEntrants = findViewById(R.id.sliderEntrants);
        ivImage = findViewById(R.id.ivImage);
        eventNameInput = findViewById(R.id.event_name);

        sliderEntrants.setValueFrom(1);
        sliderEntrants.setValueTo(100);
        sliderEntrants.setStepSize(1);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> navigateBack());
        findViewById(R.id.btnAddImage).setOnClickListener(v -> pickImage());
        findViewById(R.id.btnSave).setOnClickListener(v -> onSaveClicked());
        findViewById(R.id.btnRemove).setOnClickListener(v -> onRemoveClicked());
        findViewById(R.id.btnCreateEvent).setOnClickListener(v -> navigateToEventDetails());

        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventDetailsOrganizer.class));
        });
        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventWaitlistTab.class));
        });
        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventEntrantOrganizer.class));
        });

        registrationDeadlineInput.setOnClickListener(v -> showDatePicker());

        swGeo.setOnCheckedChangeListener((btn, isChecked) -> onGeoToggled(isChecked));
        swWaitlist.setOnCheckedChangeListener((btn, isChecked) -> onWaitlistToggled(isChecked));
    }

    private void navigateBack() {
        startActivity(new Intent(this, OrganizerHomeActivity.class));
    }

    private void navigateToEventDetails() {
        String deadline = registrationDeadlineInput.getText().toString().trim();
        String price = priceInput.getText().toString().replace("$", "").trim();
        String eventName = eventNameInput.getText().toString().trim();
        String descriptionText = descriptionInput.getText().toString().trim();
        int entrant = (int) sliderEntrants.getValue();

        if (deadline.isEmpty() || price.isEmpty() || eventName.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields before continuing", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId= db.collection("events").document().getId();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", eventId);
        eventData.put("name", eventName);
        eventData.put("date", deadline);
        eventData.put("price", price);
        eventData.put("description", descriptionText);
        eventData.put("capacity", entrant);

        // Save image to Firestore if one has been selected
        Drawable drawable = ivImage.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            eventData.put("image", encodedImage);
        } else {
            eventData.put("image", null);
        }

        db.collection("events")
                .add(eventData)
                .addOnSuccessListener(documentReference -> {
                    Intent intent = new Intent(this, EventDetailsOrganizer.class);
                    intent.putExtra("eventName", eventName);
                    intent.putExtra("registration_date", deadline);
                    intent.putExtra("event_id", eventId);
                    intent.putExtra("description", descriptionText);
                    intent.putExtra("entrants", entrant);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    if (selected.before(calendar)) {
                        Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show();
                    } else {
                        String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                        registrationDeadlineInput.setText(date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void onSaveClicked() {
        String priceText = priceInput.getText().toString().replace("$", "").trim();

        if (priceText.isEmpty()) {
            priceLayout.setError("Enter a valid price");
            return;
        }
        try {
            Double.parseDouble(priceText);
            priceLayout.setError(null);
            new AlertDialog.Builder(this)
                    .setTitle("Save changes?")
                    .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                    .setPositiveButton("Confirm", (d, w) -> saveChanges())
                    .show();
        } catch (NumberFormatException e) {
            priceLayout.setError("Enter a valid number");
        }
    }

    private void onRemoveClicked() {
        new AlertDialog.Builder(this)
                .setTitle("Delete event?")
                .setMessage("This action cannot be undone.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Confirm", (d, w) -> deleteEvent())
                .show();
    }

    private void onGeoToggled(boolean isChecked) {
        // handle geolocation toggle
    }

    private void onWaitlistToggled(boolean isChecked) {

    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveChanges() { }

    private void deleteEvent() { }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ivImage.setImageURI(data.getData());
        }
    }
}