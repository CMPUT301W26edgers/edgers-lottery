package com.example.edgers_lottery;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
;


public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    protected static User user;
    private void showUserInfoDialog(DocumentSnapshot documentSnapshot) {
        String name = documentSnapshot.getString("name");
        String email = documentSnapshot.getString("email");
        String phone = documentSnapshot.getString("phone");

        new AlertDialog.Builder(this)
                .setTitle("User Info")
                .setMessage("Name: " + name + "\nEmail: " + email)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        user.setId(uid);
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // GRAB DATA
                showUserInfoDialog(documentSnapshot);
            }
        });
    }

}
