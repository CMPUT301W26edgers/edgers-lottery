package com.example.edgers_lottery.views;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
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

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.services.ImageService;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.slider.Slider;
import java.util.Calendar;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.DocumentReference;

/**
 * Activity for creating a new event or editing an existing one.
 *
 * Uses a single {@code currentEventId} field throughout:
 * - null = create mode: btnCreateEvent writes a new document and sets currentEventId.
 * - non-null = edit mode: fields pre-populated from Firestore; btnSave updates the
 *   document then calls finish() so EventDetailsOrganizer reloads via onResume().
 */
public class CreateEditEventActivity extends AppCompatActivity {

    private ImageView ivImage;
    private Uri imageUri;
    private String url;
    private EditText registrationDeadlineInput, eventDateInput, priceInput, descriptionInput;
    private TextInputLayout priceLayout;
    private SwitchMaterial swGeo, swWaitlist, swPublic;
    private Slider sliderEntrants;
    private EditText eventNameInput;
    private User user;


    /**
     * Single source of truth for the event ID.
     * Null when creating; set from intent extra (or after first save) when editing.
     */
    private String currentEventId;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_end_event);
        // get the current user
        user = CurrentUser.get();
        if (user == null) {
            Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        setupListeners();
        setupEdgeToEdge();
    }

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
        ivImage                   = findViewById(R.id.ivImage);
        eventNameInput            = findViewById(R.id.event_name);

        sliderEntrants.setValueFrom(1);
        sliderEntrants.setValueTo(100);
        sliderEntrants.setStepSize(1);
        sliderEntrants.setEnabled(false);

        currentEventId = getIntent().getStringExtra("event_id");

        if (currentEventId != null) {
            loadEventData();
        }
    }

    /**
     * Fetches the existing Firestore document and populates every UI field.
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

                    String name        = doc.getString("name");
                    String deadline    = doc.getString("date");
                    String eventDate   = doc.getString("eventDate");
                    String price       = doc.getString("price");
                    String description = doc.getString("description");

                    if (name        != null) eventNameInput.setText(name);
                    if (deadline    != null) registrationDeadlineInput.setText(deadline);
                    if (eventDate   != null) eventDateInput.setText(eventDate);
                    if (price       != null) priceInput.setText(price);
                    if (description != null) descriptionInput.setText(description);

                    Long capacity = doc.getLong("capacity");
                    if (capacity != null) {
                        sliderEntrants.setValue(Math.min(Math.max(capacity.floatValue(), 1f), 100f));
                    }

                    Boolean geoRequired     = doc.getBoolean("geoRequired");
                    Boolean waitlistEnabled = doc.getBoolean("waitlistEnabled");
                    Boolean isPublic        = doc.getBoolean("ispublic");
                    if (geoRequired     != null) swGeo.setChecked(geoRequired);
                    if (waitlistEnabled != null) swWaitlist.setChecked(waitlistEnabled);
                    if (isPublic        != null) swPublic.setChecked(isPublic);

                    String encodedImage = doc.getString("poster");
                    if (encodedImage != null && !encodedImage.isEmpty()) {
                        try {
                            byte[] bytes  = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            if (bitmap != null) ivImage.setImageBitmap(bitmap);
                        } catch (IllegalArgumentException ignored) { }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        // Back always just pops this screen
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Leaving Create Page?")
                    .setMessage("Are you sure you are ready to leave this page?")
                    .setPositiveButton("Leave", (dialog, which) -> {
                        Intent intent;
                        if (user.isOrganizer()){
                            intent = new Intent(this, OrganizerHomeActivity.class);
                        }else{
                            intent = new Intent(this, HomeActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });

        findViewById(R.id.btnAddImage).setOnClickListener(v -> pickImage());
        findViewById(R.id.btnSave).setOnClickListener(v -> onSaveClicked());
        findViewById(R.id.btnRemove).setOnClickListener(v -> onRemoveClicked());
        findViewById(R.id.btnCreateEvent).setOnClickListener(v -> createEventAndNavigate());

        // Tab-bar buttons all use currentEventId — guard against null (create mode, pre-save)
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
//      Button commentsBtn = findViewById(R.id.commentsBtn);

        findViewById(R.id.commentsBtn).setOnClickListener(v -> {
            if (currentEventId == null) {
                Toast.makeText(this, "Create the event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, EventCommentsOrganizer.class);
            intent.putExtra("event_id", currentEventId);
            startActivity(intent);
        });

        findViewById(R.id.mapBtn).setOnClickListener(v -> {
            if (currentEventId == null) {
                Toast.makeText(this, "Create the event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, OrganizerWaitlistMapActivity.class);
            intent.putExtra("event_id", currentEventId); // Using "event_id" to match your teammate's intent keys
            startActivity(intent);
        });

        registrationDeadlineInput.setOnClickListener(v -> showDatePicker(registrationDeadlineInput));
        eventDateInput.setOnClickListener(v -> showDatePicker(eventDateInput));

        swGeo.setOnCheckedChangeListener((btn, isChecked) -> onGeoToggled(isChecked));
        swWaitlist.setOnCheckedChangeListener((btn, isChecked) -> onWaitlistToggled(isChecked));
    }

    /**
     * Validates fields, writes a new Firestore document, stores the returned ID in
     * currentEventId, then navigates to EventDetailsOrganizer.
     */
    private void createEventAndNavigate() {
        String deadline        = registrationDeadlineInput.getText().toString().trim();
        String eventDate       = eventDateInput.getText().toString().trim();
        String price           = priceInput.getText().toString().replace("$", "").trim();
        String eventName       = eventNameInput.getText().toString().trim();
        String descriptionText = descriptionInput.getText().toString().trim();

        int    entrant         = (int) sliderEntrants.getValue();

        if (deadline.isEmpty() || eventDate.isEmpty() || price.isEmpty() || eventName.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields before continuing", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser deviceUser = FirebaseAuth.getInstance().getCurrentUser();
        if (deviceUser == null){
            Toast.makeText(this, "No logged in user found", Toast.LENGTH_SHORT).show();
            return;
        }
        String organizerId = deviceUser.getUid();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("events").document();
        String newId = docRef.getId();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventId",         newId);
        eventData.put("id",              newId);
        eventData.put("name",            eventName);
        eventData.put("eventDate",       eventDate);
        eventData.put("date",            deadline);
        eventData.put("price",           price);
        eventData.put("description",     descriptionText);
        eventData.put("capacity",        entrant);
        eventData.put("geoRequired",      swGeo.isChecked());
        eventData.put("enforceLocation",  swGeo.isChecked());
        eventData.put("waitlistEnabled",  swWaitlist.isChecked());
        eventData.put("ispublic",         swPublic.isChecked());
        eventData.put("organizerId",     organizerId);
        eventData.put("poster", null);


//        Drawable drawable = ivImage.getDrawable();
//        if (drawable instanceof BitmapDrawable) {
//            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
//            eventData.put("image", Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
//        } else {
//            eventData.put("image", null);
//        }

        docRef.set(eventData)
                .addOnSuccessListener(unused -> {
                    //currentEventId = newId; // now the activity knows its own ID
                    //Intent intent = new Intent(this, EventDetailsOrganizer.class);
                    //intent.putExtra("event_id", currentEventId);
                    //startActivity(intent);
                    if (imageUri != null) {
                        ImageService.uploadEventImage(imageUri, newId, this);
                    }
                    if (user.isOrganizer()){
                        Intent intent = new Intent(this, OrganizerEventsListActivity.class);
                        startActivity(intent);

                    }else{
                        Toast.makeText(this, "Event created! You are now an organizer!", Toast.LENGTH_SHORT).show();
                        updateUserOrganizerPermission(); // make sure user is organizer now and send them to home screen for organizers now
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
                        Calendar deadline = parseDate(registrationDeadlineInput.getText().toString().trim());
                        if (deadline != null && selected.before(deadline)) {
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

    private void onGeoToggled(boolean isChecked) { }

    private void onWaitlistToggled(boolean isChecked) {
        sliderEntrants.setEnabled(isChecked);
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Updates the Firestore document then calls finish() so EventDetailsOrganizer
     * resurfaces and reloads the fresh data via its onResume().
     */
    private void saveChanges() {
        if (currentEventId == null) {
            Toast.makeText(this, "No event to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventName   = eventNameInput.getText().toString().trim();
        String eventDate   = eventDateInput.getText().toString().trim();
        String deadline    = registrationDeadlineInput.getText().toString().trim();
        String price       = priceInput.getText().toString().replace("$", "").trim();
        String description = descriptionInput.getText().toString().trim();
        int    entrants    = (int) sliderEntrants.getValue();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name",            eventName);
        updates.put("eventDate",       eventDate);
        updates.put("date",            deadline);
        updates.put("price",           price);
        updates.put("description",     description);
        updates.put("capacity",        entrants);
        updates.put("geoRequired",      swGeo.isChecked());
        updates.put("enforceLocation",  swGeo.isChecked());
        updates.put("waitlistEnabled",  swWaitlist.isChecked());
        updates.put("ispublic",         swPublic.isChecked());

        // update the event with the object

//        Drawable drawable = ivImage.getDrawable();
//        if (drawable instanceof BitmapDrawable) {
//            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
//            updates.put("image", Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
//        }

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(currentEventId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                    // confirm user roles as an organizer
                    updateUserOrganizerPermission();

                    finish(); // pops back to EventDetailsOrganizer → onResume() reloads data
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
    private void updateUserOrganizerPermission(){
        if (!user.isOrganizer()) {
            user.setOrganizer(true);
            CurrentUser.set(user);// update current user
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getId())
                    .set(user);
            startActivity(new Intent(this, OrganizerHomeActivity.class));
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData(); // the image the user picked
            Glide.with(this).load(imageUri).circleCrop().into(ivImage); // load the image into the ImageView

            url = imageUri.toString();
            if (currentEventId != null) {
                ImageService.uploadEventImage(imageUri, currentEventId, this);
            }
            // update the event here with the new image
            // do it just in case, its already set in the db
            // currentEvent.setPoster(imageUri.toString());
        }
    }
}