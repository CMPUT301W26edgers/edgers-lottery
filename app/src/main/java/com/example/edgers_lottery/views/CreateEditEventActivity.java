package com.example.edgers_lottery.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.User;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.Intent;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import com.google.android.gms.tasks.OnSuccessListener;

public class CreateEditEventActivity extends AppCompatActivity {

    private ImageView ivImage;
    private EditText registrationDeadlineInput, eventDateInput, priceInput, descriptionInput;
    private TextInputLayout priceLayout;
    private SwitchMaterial swGeo, swWaitlist, swPublic;
    private Slider sliderEntrants;
    private Slider sliderWaitlist;
    private EditText eventNameInput;

    private String currentEventId;

    /**
     * Persists state across the activity lifecycle — specifically the poster {@link Uri}
     * selected by the user (not survives Firestore) and the Base64 image string loaded
     * from Firestore (so it is not lost when saving without picking a new image).
     */
    private final Event currentEvent = new Event();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_end_event);

        initViews();
        setupListeners();
        setupEdgeToEdge();
    }

    // --- View Initialization -------------------------------------------------

    private void initViews() {
        registrationDeadlineInput = findViewById(R.id.registration_deadline);
        eventDateInput            = findViewById(R.id.event_date);
        priceLayout               = findViewById(R.id.priceLayout);
        priceInput                = findViewById(R.id.price);
        descriptionInput          = findViewById(R.id.editTilDescription);
        swGeo                     = findViewById(R.id.swGeo);
        swWaitlist                = findViewById(R.id.swWaitlist);
        swPublic                  = findViewById(R.id.swPublic);
        sliderEntrants            = findViewById(R.id.sliderEntrants);
        sliderWaitlist            = findViewById(R.id.sliderWaitlist);
        ivImage                   = findViewById(R.id.ivImage);
        eventNameInput            = findViewById(R.id.event_name);

        sliderEntrants.setValueFrom(1);
        sliderEntrants.setValueTo(100);
        sliderEntrants.setStepSize(1);

        sliderWaitlist.setValueFrom(1);
        sliderWaitlist.setValueTo(100);
        sliderWaitlist.setStepSize(1);

        currentEventId = getIntent().getStringExtra("event_id");
        if (currentEventId != null) {
            loadEventData();
        }
    }

    // --- Load Existing Event -------------------------------------------------

    /**
     * Fetches the event document from Firestore and populates {@link #currentEvent} via its
     * setters. The Base64 image is stored on currentEvent so it is preserved on subsequent
     * saves if the user does not pick a new image. The poster URI is not set here because
     * a Uri cannot be retrieved from Firestore — the Base64 is decoded for display instead.
     */
    private void loadEventData() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(currentEventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentEvent.setId(doc.getString("id"));
                    currentEvent.setName(doc.getString("name"));
                    currentEvent.setDate(doc.getString("date"));
                    currentEvent.setRegistrationEnd(doc.getString("registrationEnd"));
                    //currentEvent.setPrice(doc.getString("price"));
                    currentEvent.setDescription(doc.getString("description"));
                    currentEvent.setPoster(doc.getString("poster")); // retained for saves

                    Long capacity = doc.getLong("capacity");
                    if (capacity != null) currentEvent.setCapacity(capacity.intValue());

                    Long waitlistCapacity = doc.getLong("waitlistCapacity");
                    if (waitlistCapacity != null) currentEvent.setWaitlistCapacity(waitlistCapacity.intValue());

                    Boolean enforceLocation = doc.getBoolean("enforceLocation");
                    if (enforceLocation != null) currentEvent.setEnforceLocation(enforceLocation);

                    Boolean ispublic = doc.getBoolean("ispublic");
                    if (ispublic != null) currentEvent.setIspublic(ispublic);

                    // poster URI is local-only and cannot be restored from Firestore;
                    // populateUiFromEvent will render the image from the Base64 string instead.
                    populateUiFromEvent(currentEvent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Reads fields from the given {@link Event} via its getters and applies them to the UI.
     * The image is rendered by decoding {@link Event#getImage()} because the poster
     * {@link Uri} is a local-only handle that is not persisted to Firestore.
     *
     * @param event the event whose data should be displayed
     */
    private void populateUiFromEvent(Event event) {
        if (event.getName()            != null) eventNameInput.setText(event.getName());
        if (event.getDate()            != null) eventDateInput.setText(event.getDate());
        if (event.getRegistrationEnd() != null) registrationDeadlineInput.setText(event.getRegistrationEnd());
        if (event.getPrice()           != null) priceInput.setText(event.getPrice());
        if (event.getDescription()     != null) descriptionInput.setText(event.getDescription());

        int capacity = event.getCapacity();
        if (capacity > 0) sliderEntrants.setValue(Math.min(Math.max((float) capacity, 1f), 100f));

        int waitlistCapacity = event.getWaitlistCapacity();
        if (waitlistCapacity > 0) sliderWaitlist.setValue(Math.min(Math.max((float) waitlistCapacity, 1f), 100f));

        swGeo.setChecked(event.isEnforceLocation());
        swPublic.setChecked(event.isIspublic());

        // Poster URI does not survive a Firestore round-trip, so decode the Base64 image.
        String encodedImage = event.getImage();
        if (encodedImage != null && !encodedImage.isEmpty()) {
            try {
                byte[] bytes  = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) ivImage.setImageBitmap(bitmap);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    // --- Build Event From UI -------------------------------------------------

    /**
     * Reads the current UI state and constructs an {@link Event} object using its setters.
     *
     * <p>Image handling:
     * <ul>
     *   <li>If the user picked a new image this session, {@link #currentEvent}'s poster
     *       {@link Uri} is non-null. That URI is resolved to a {@link Bitmap}, compressed,
     *       and stored as Base64 via {@link Event#setImage}.</li>
     *   <li>Otherwise the existing Base64 from {@link #currentEvent} (loaded from Firestore)
     *       is carried forward so it is not lost on save.</li>
     * </ul>
     *
     * @param eventId the Firestore document ID to assign, or {@code null} for a new event
     * @return a fully populated {@link Event} ready for {@link Event#toFirestoreMap()}
     */
    private Event buildEventFromUi(String eventId) {
        Event event = new Event();

        event.setId(eventId);
        event.setName(eventNameInput.getText().toString().trim());
        event.setDate(eventDateInput.getText().toString().trim());
        event.setRegistrationEnd(registrationDeadlineInput.getText().toString().trim());
        event.setPrice(priceInput.getText().toString().replace("$", "").trim());
        event.setDescription(descriptionInput.getText().toString().trim());
        event.setCapacity((int) sliderEntrants.getValue());
        event.setWaitlistCapacity((int) sliderWaitlist.getValue());
        event.setEnforceLocation(swGeo.isChecked());
        event.setIspublic(swPublic.isChecked());

        // Read the poster URI stored on currentEvent when the user picked an image.
        Uri poster = currentEvent.getPoster();
        if (poster != null) {
            // New image selected this session — encode from poster URI to Base64 for Firestore
            event.setPoster(poster);
            try {
                Bitmap bitmap = android.provider.MediaStore.Images.Media
                        .getBitmap(getContentResolver(), poster);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                event.setImage(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
            } catch (IOException e) {
                Toast.makeText(this, "Failed to encode image", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No new image picked — carry forward the Base64 already loaded from Firestore
            event.setImage(currentEvent.getImage());
        }

        return event;
    }

    // --- Create Event --------------------------------------------------------

    /**
     * Validates inputs, builds an {@link Event} via {@link #buildEventFromUi},
     * writes it to Firestore using {@link Event#toFirestoreMap()}, then navigates
     * to {@link EventDetailsOrganizer}.
     */
    private void createEventAndNavigate() {

        if (eventNameInput.getText().toString().trim().isEmpty()
                || eventDateInput.getText().toString().trim().isEmpty()
                || registrationDeadlineInput.getText().toString().trim().isEmpty()
                || priceInput.getText().toString().replace("$", "").trim().isEmpty()) {

            Toast.makeText(this, "Please fill in all fields before continuing", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("events")
                .document();

        Event event = buildEventFromUi(docRef.getId());

        docRef.set(event.toFirestoreMap())
                .addOnSuccessListener(unused -> {

                    // NOW fetch the document so you can use toObject()
                    docRef.get().addOnSuccessListener(doc -> {

                        if (doc.exists()) {
                            Event savedEvent = doc.toObject(Event.class);

                            if (savedEvent != null) {
                                currentEventId = doc.getId();

                                Intent intent = new Intent(this, EventDetailsOrganizer.class);
                                intent.putExtra("event_id", currentEventId);
                                startActivity(intent);
                            }
                        }

                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    // --- Save Changes --------------------------------------------------------

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
     * Builds an updated {@link Event} from the current UI state and writes it to
     * Firestore using {@link Event#toFirestoreMap()}.
     */
    private void saveChanges() {
        if (currentEventId == null) {
            Toast.makeText(this, "No event to update", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = buildEventFromUi(currentEventId);

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(currentEventId)
                .update(event.toFirestoreMap())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- Delete Event --------------------------------------------------------

    private void onRemoveClicked() {
        new AlertDialog.Builder(this)
                .setTitle("Delete event?")
                .setMessage("This action cannot be undone.")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Confirm", (d, w) -> deleteEvent())
                .show();
    }

    private void deleteEvent() {
        if (currentEventId == null) {
            Toast.makeText(this, "No event to delete", Toast.LENGTH_SHORT).show();
            return;
        }
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

    // --- Listeners & Helpers -------------------------------------------------

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddImage).setOnClickListener(v -> pickImage());
        findViewById(R.id.btnSave).setOnClickListener(v -> onSaveClicked());
        findViewById(R.id.btnRemove).setOnClickListener(v -> onRemoveClicked());
        findViewById(R.id.btnCreateEvent).setOnClickListener(v -> createEventAndNavigate());

        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            if (currentEventId == null) {
                Toast.makeText(this, "Create the event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, EventDetailsOrganizer.class);
            intent.putExtra("event_id", currentEventId);
            startActivity(intent);
        });

        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            if (currentEventId == null) {
                Toast.makeText(this, "Create the event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, EventWaitlistTab.class);
            intent.putExtra("event_id", currentEventId);
            startActivity(intent);
        });

        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            if (currentEventId == null) {
                Toast.makeText(this, "Create the event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, EventEntrantOrganizer.class);
            intent.putExtra("event_id", currentEventId);
            startActivity(intent);
        });

        registrationDeadlineInput.setOnClickListener(v -> showDatePicker(registrationDeadlineInput));
        eventDateInput.setOnClickListener(v -> showDatePicker(eventDateInput));

        swGeo.setOnCheckedChangeListener((btn, isChecked) -> onGeoToggled(isChecked));
        swWaitlist.setOnCheckedChangeListener((btn, isChecked) -> onWaitlistToggled(isChecked));
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri posterUri = data.getData();
            currentEvent.setPoster(posterUri); // store via Event setter so buildEventFromUi can read it
            ivImage.setImageURI(posterUri);    // display immediately in the UI
        }
    }

    private Calendar parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            String[] parts = dateStr.split("-");
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    private void showDatePicker(EditText targetInput) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    selected.set(Calendar.HOUR_OF_DAY, 0);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    if (selected.before(today)) {
                        Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (targetInput == eventDateInput) {
                        Calendar regEnd = parseDate(registrationDeadlineInput.getText().toString().trim());
                        if (regEnd != null && selected.before(regEnd)) {
                            Toast.makeText(this, "Event date cannot be before the registration deadline", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (targetInput == registrationDeadlineInput) {
                        Calendar eventDate = parseDate(eventDateInput.getText().toString().trim());
                        if (eventDate != null && selected.after(eventDate)) {
                            Toast.makeText(this, "Registration deadline cannot be after the event date", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    targetInput.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void onGeoToggled(boolean isChecked) { }

    private void onWaitlistToggled(boolean isChecked) { }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}