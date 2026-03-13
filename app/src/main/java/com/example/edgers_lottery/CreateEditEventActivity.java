package com.example.edgers_lottery;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.DocumentReference;

/**
 * Activity for creating a new event or editing an existing one.
 * Handles event name, registration deadline, price, description,
 * entrant capacity, geolocation toggle, waitlist toggle, and an optional image.
 * If launched with an {@code event_id} intent extra, the activity operates in edit mode.
 * Otherwise it creates a new Firestore document for the event.
 */
public class CreateEditEventActivity extends AppCompatActivity {

    private ImageView ivImage;
    private EditText registrationDeadlineInput, priceInput, descriptionInput;
    private TextInputLayout priceLayout;
    private SwitchMaterial swGeo, swWaitlist;
    private Slider sliderEntrants;
    private EditText eventNameInput;
    private String currentEventId;
    private String eventId;

    /**
     * Initializes the activity, enables edge-to-edge display, inflates the layout,
     * and delegates to {@link #initViews()}, {@link #setupListeners()},
     * and {@link #setupEdgeToEdge()}.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_end_event);

        initViews();
        setupListeners();
        setupEdgeToEdge();
    }

    /**
     * Binds all UI views and reads the optional {@code event_id} intent extra
     * to determine whether the activity is in create or edit mode.
     */
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
        currentEventId = getIntent().getStringExtra("event_id");
    }

    /**
     * Attaches click and toggle listeners to all interactive UI elements including
     * navigation buttons, save, remove, image picker, date picker, and switches.
     */
    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> navigateBack());
        findViewById(R.id.btnAddImage).setOnClickListener(v -> pickImage());
        findViewById(R.id.btnSave).setOnClickListener(v -> onSaveClicked());
        findViewById(R.id.btnRemove).setOnClickListener(v -> onRemoveClicked());
        findViewById(R.id.btnCreateEvent).setOnClickListener(v -> navigateToEventDetails());

        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, EventDetailsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
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

        registrationDeadlineInput.setOnClickListener(v -> showDatePicker());
        swGeo.setOnCheckedChangeListener((btn, isChecked) -> onGeoToggled(isChecked));
        swWaitlist.setOnCheckedChangeListener((btn, isChecked) -> onWaitlistToggled(isChecked));
    }

    /**
     * Navigates back to {@link OrganizerHomeActivity}.
     */
    private void navigateBack() {
        startActivity(new Intent(this, OrganizerHomeActivity.class));
    }

    /**
     * Validates all input fields, creates a new Firestore event document with a
     * generated ID, optionally encodes and stores an event image as Base64,
     * and navigates to {@link EventDetailsOrganizer} on success.
     */
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
        DocumentReference docRef = db.collection("events").document();
        String eventId = docRef.getId();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId", eventId);
        eventData.put("id", eventId);
        eventData.put("name", eventName);
        eventData.put("date", deadline);
        eventData.put("price", price);
        eventData.put("description", descriptionText);
        eventData.put("capacity", entrant);

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

        docRef.set(eventData)
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(this, EventDetailsOrganizer.class);
                    intent.putExtra("eventName", eventName);
                    intent.putExtra("registration_date", deadline);
                    intent.putExtra("event_id", eventId);
                    intent.putExtra("description", descriptionText);
                    intent.putExtra("entrants", entrant);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Launches the system image picker to allow the user to select an event image.
     */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    /**
     * Displays a {@link DatePickerDialog} pre-set to today's date.
     * Prevents selection of past dates and formats the chosen date as {@code yyyy-MM-dd}.
     */
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

    /**
     * Validates the price field and shows a confirmation dialog before saving changes.
     * Displays an error on the price input if the value is missing or not a valid number.
     */
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

    /**
     * Shows a confirmation dialog before permanently deleting the current event.
     */
    private void onRemoveClicked() {
        new AlertDialog.Builder(this)
                .setTitle("Delete event?")
                .setMessage("This action cannot be undone.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Confirm", (d, w) -> deleteEvent())
                .show();
    }

    /**
     * Called when the geolocation toggle switch is changed.
     *
     * @param isChecked true if geolocation is now enabled, false otherwise
     */
    private void onGeoToggled(boolean isChecked) {
        // handle geolocation toggle
    }

    /**
     * Called when the waitlist toggle switch is changed.
     *
     * @param isChecked true if the waitlist is now enabled, false otherwise
     */
    private void onWaitlistToggled(boolean isChecked) {
    }

    /**
     * Applies system window insets as padding to the root view to support
     * edge-to-edge display on modern Android versions.
     */
    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Reads all current field values and updates the existing Firestore event document
     * identified by {@code currentEventId}. Also encodes and updates the event image
     * if one has been selected. Shows a toast on success or failure.
     */
    private void saveChanges() {
        String eventName = eventNameInput.getText().toString().trim();
        String deadline = registrationDeadlineInput.getText().toString().trim();
        String price = priceInput.getText().toString().replace("$", "").trim();
        String description = descriptionInput.getText().toString().trim();
        int entrants = (int) sliderEntrants.getValue();

        if (currentEventId == null) {
            Toast.makeText(this, "No event to update", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", eventName);
        updates.put("date", deadline);
        updates.put("price", price);
        updates.put("description", description);
        updates.put("capacity", entrants);
        updates.put("geoRequired", swGeo.isChecked());
        updates.put("waitlistEnabled", swWaitlist.isChecked());

        Drawable drawable = ivImage.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            updates.put("image", Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
        }

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(currentEventId)
                .update(updates)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Deletes the current event document from Firestore and navigates back
     * to {@link OrganizerHomeActivity} on success.
     */
    private void deleteEvent() {
        if (currentEventId == null) return;

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(currentEventId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, OrganizerHomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Receives the result from the image picker and sets the selected image
     * on the event image view.
     *
     * @param requestCode the request code passed to {@code startActivityForResult}
     * @param resultCode  the result code returned by the image picker activity
     * @param data        the intent containing the selected image URI
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ivImage.setImageURI(data.getData());
        }
    }
}