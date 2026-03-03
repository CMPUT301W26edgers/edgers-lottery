package com.example.edgers_lottery;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

public class ProfileTestEnv {
    // firestore db
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference profiles = db.collection("profiles");
    // try to add a profile
    @Test
    public void addProfileTest() { // does not work
        String name = "John Doe";
        String email = "john.doe@email.com";
        String phone = "123-456-7890";
        // try to check if profile already exists
        // here

        // update database
        db.collection("profiles").document(name).set(new Entrant(name, email, phone));


    }



}
