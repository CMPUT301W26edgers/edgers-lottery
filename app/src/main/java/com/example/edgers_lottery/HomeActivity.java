package com.example.edgers_lottery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
;


public class HomeActivity extends AppCompatActivity {
    private User user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // GRAB DATA
                user.setName(documentSnapshot.getString("name"));
                user.setEmail(documentSnapshot.getString("email"));
                user.setPhone(documentSnapshot.getString("phone"));

            }
        });
    }
}
