package com.example.edgers_lottery;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileFragment extends DialogFragment {
    private static final String TAG = "EditProfileFragment";

    private EditText descriptionEditText;
    private EditText emailEditText;
    private EditText locationEditText;
    private User user;
    private User editingProfile;


    @NonNull
    public static EditProfileFragment newInstance(User user) { // from lab instructions
        Bundle args = new Bundle();
        args.putSerializable("profile", user);
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    interface EditProfileDialogListener {
        default void editUser(User user, String newDesc, String newEmail, String newLocation, String newPhone, String newUsername){
            user.setEmail(newEmail);
            user.setDescription(newDesc);
            user.setLocation(newLocation);
            user.setPhone(newPhone);
            user.setUsername(newUsername);
            // update the user in the database
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getId())
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        // update the user in the CurrentUser class
                        CurrentUser.set(user);
                    });
        };
    }
    private EditProfileDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditProfileDialogListener) {
            listener = (EditProfileDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement EditProfileDialogListener");
        }
    }
    @NonNull
    @Override // check over this function!
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_edit_profile, null);
        EditText editDescription = view.findViewById(R.id.edit_description_text);
        EditText editEmail = view.findViewById(R.id.edit_email_text);
        EditText editLocation = view.findViewById(R.id.edit_location_text);
        EditText editPhone = view.findViewById(R.id.edit_phone_text);
        EditText editUsername = view.findViewById(R.id.edit_username_text);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // set the text before returning anything!
        if (getArguments()!=null){
            editingProfile = (User) getArguments().getSerializable("profile"); // get that city that was clicked
            assert editingProfile != null;

            editDescription.setText(editingProfile.getDescription());
            editEmail.setText(editingProfile.getEmail());
            editLocation.setText(editingProfile.getLocation());
            editPhone.setText(editingProfile.getPhone());
            editUsername.setText(editingProfile.getUsername());
        }
        return builder
                .setView(view)
                .setTitle("Editing Your Profile")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Edit", (dialog, which) -> {
                    assert getArguments() != null;
                    editingProfile = (User) getArguments().getSerializable("profile");
                    assert editingProfile != null;
                    listener.editUser(editingProfile, editDescription.getText().toString(), editEmail.getText().toString(), editLocation.getText().toString(), editPhone.getText().toString(), editUsername.getText().toString());
                })
                .create();
    }
}
