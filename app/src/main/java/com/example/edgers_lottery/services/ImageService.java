package com.example.edgers_lottery.services;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;

/**
 * Service class for uploading images to Firebase Storage.
 */
public class ImageService {

    // things we'll need to handle
    // 1. setting the user's profile image in the profile activity and putting it in the firebase storage
    // 2. setting the event's image in the event creation activity and putting it in the firebase storage


    /**
     * Uploads a profile image to Firebase Storage and saves the download URL
     * to the current user's Firestore document. Shows a toast on success or failure.
     *
     * @param imageUri the URI of the image to upload
     * @param context  the current context for displaying toasts
     */
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
    /**
     * Uploads an event poster image to Firebase Storage and saves the download URL
     * to the corresponding event's Firestore document. Shows a toast on success or failure.
     *
     * @param imageUri the URI of the image to upload
     * @param eventId  the ID of the event to associate the image with
     * @param context  the current context for displaying toasts
     */
    public static void uploadEventImage(Uri imageUri, String eventId, Context context) {
        if (imageUri == null) {
            android.util.Log.e("ImageService", "imageUri is null");
            return;
        }
        if (eventId == null) {
            android.util.Log.e("ImageService", "eventId is null");
            return;
        }

        android.util.Log.d("ImageService", "Starting upload for eventId: " + eventId + " uri: " + imageUri);

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("event_images/" + eventId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    android.util.Log.d("ImageService", "Storage upload success");
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        android.util.Log.d("ImageService", "Got download URL: " + uri);
                        FirebaseFirestore.getInstance()
                                .collection("events")
                                .document(eventId)
                                .set(Collections.singletonMap("poster", uri.toString()), SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    android.util.Log.d("ImageService", "Firestore updated successfully");
                                    Toast.makeText(context, "Event poster uploaded!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("ImageService", "Firestore update failed: " + e.getMessage());
                                });
                    }).addOnFailureListener(e -> {
                        android.util.Log.e("ImageService", "getDownloadUrl failed: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ImageService", "Storage upload failed: " + e.getMessage());
                });
    }
    // might need to implement deletes
}
