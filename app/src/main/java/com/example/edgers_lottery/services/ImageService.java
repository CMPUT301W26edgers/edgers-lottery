package com.example.edgers_lottery.services;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageService {

    // things we'll need to handle
    // 1. setting the user's profile image in the profile activity and putting it in the firebase storage
    // 2. setting the event's image in the event creation activity and putting it in the firebase storage

    // upload a profile image to firebase storage
    public static void uploadProfileImage(Uri imageUri, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userId + ".jpg");
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                // Save the URL to the user's profile in the database
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("profileImage", url)
                        .addOnSuccessListener(aVoid -> {
                            // Handle success
                            Toast.makeText(context, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            // Handle failure for updating the database
                            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(e -> {
                // Handle failure for getting the download URL
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();

            });
        }).addOnFailureListener(e -> {
            // Handle failure for uploading the image
            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }
    // upload an event image to firebase storage
    public static void uploadEventImage(Uri imageUri, String eventId, Context context) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("event_images/" + eventId + ".jpg");
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                // Save the URL to the user's profile in the database
                FirebaseFirestore.getInstance()
                        .collection("events")
                        .document(eventId)
                        .update("poster", url)
                        .addOnSuccessListener(aVoid -> {
                            // Handle success
                            Toast.makeText(context, "Event poster image uploaded successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            // Handle failure for updating the database
                            Toast.makeText(context, "Failed to upload poster", Toast.LENGTH_SHORT).show();
                        });
            }).addOnFailureListener(e -> {
                // Handle failure for getting the download URL
                Toast.makeText(context, "Failed to upload poster", Toast.LENGTH_SHORT).show();

            });
        }).addOnFailureListener(e -> {
            // Handle failure for uploading the image
            Toast.makeText(context, "Failed to upload poster", Toast.LENGTH_SHORT).show();
        });
    }
}
