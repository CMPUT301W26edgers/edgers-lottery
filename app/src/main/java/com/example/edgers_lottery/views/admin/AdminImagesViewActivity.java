package com.example.edgers_lottery.views.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.adapters.AdminImagesViewAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Images view screen for admin users, lists all images in firebase storage.
 * Should only be accessed for users with admin privileges.
 */
public class AdminImagesViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> imageUrls = new ArrayList<>();
    private List<StorageReference> imageRefs = new ArrayList<>();
    private AdminImagesViewAdapter adapter;

    /**
     * Called when the activity is created.
     * Sets up the RecyclerView and loads all images from Firebase Storage.
     *
     * @param savedInstanceState previously saved instance state, or null
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_images_view);

        recyclerView = findViewById(R.id.imagesView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AdminImagesViewAdapter(this, imageUrls, imageRefs);
        recyclerView.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        loadAllImages();
    }

    /**
     * Loads all profile and event images from Firebase Storage
     * and displays them in the grid view.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadAllImages() {
        // Load profile images
        StorageReference profileRef = FirebaseStorage.getInstance().getReference("profile_images");
        profileRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageRefs.add(fileRef);
                    imageUrls.add(uri.toString());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch profile image", Toast.LENGTH_SHORT).show();
                });
            }
        })
        .addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch profile images", Toast.LENGTH_SHORT).show();
        });

        // Load event images
        StorageReference eventRef = FirebaseStorage.getInstance().getReference("event_images");
        eventRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageRefs.add(fileRef);
                    imageUrls.add(uri.toString());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch event image", Toast.LENGTH_SHORT).show();
                });
            }
        })
        .addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch event images", Toast.LENGTH_SHORT).show();
        });
    }
}
