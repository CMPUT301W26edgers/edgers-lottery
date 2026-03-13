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

/**
 * Dialog fragment that allows a user to edit their profile information.
 * Displays editable fields for description, email, location, phone, and username.
 * Requires the host activity to implement {@link EditProfileDialogListener}.
 */
public class EditProfileFragment extends DialogFragment {

    private static final String TAG = "EditProfileFragment";

    private EditText descriptionEditText;
    private EditText emailEditText;
    private EditText locationEditText;
    private User user;
    private User editingProfile;

    /**
     * Creates a new instance of {@link EditProfileFragment} with the given user
     * passed as a serializable argument.
     *
     * @param user the {@link User} whose profile is being edited
     * @return a new {@link EditProfileFragment} instance with the user bundled as an argument
     */
    @NonNull
    public static EditProfileFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable("profile", user);
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Listener interface that must be implemented by the host activity.
     * Provides a default implementation that updates the user object fields
     * and persists the changes to Firestore.
     */
    interface EditProfileDialogListener {

        /**
         * Called when the user confirms their profile edits.
         * Updates the {@link User} object with the new values and saves to Firestore.
         * Also updates the global {@link CurrentUser} instance on success.
         *
         * @param user        the {@link User} object to update
         * @param newDesc     the new description entered by the user
         * @param newEmail    the new email entered by the user
         * @param newLocation the new location entered by the user
         * @param newPhone    the new phone number entered by the user
         * @param newUsername the new username entered by the user
         */
        default void editUser(User user, String newDesc, String newEmail, String newLocation, String newPhone, String newUsername) {
            user.setEmail(newEmail);
            user.setDescription(newDesc);
            user.setLocation(newLocation);
            user.setPhone(newPhone);
            user.setUsername(newUsername);
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getId())
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        CurrentUser.set(user);
                    });
        }
    }

    private EditProfileDialogListener listener;

    /**
     * Attaches the fragment to the host context and verifies that it implements
     * {@link EditProfileDialogListener}.
     *
     * @param context the host activity context
     * @throws RuntimeException if the host activity does not implement {@link EditProfileDialogListener}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditProfileDialogListener) {
            listener = (EditProfileDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement EditProfileDialogListener");
        }
    }

    /**
     * Inflates the fragment layout, pre-populates fields with the current user's data,
     * and builds the alert dialog with Cancel and Edit buttons.
     * On confirmation, delegates to {@link EditProfileDialogListener#editUser}.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     * @return the fully constructed {@link Dialog}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_edit_profile, null);
        EditText editDescription = view.findViewById(R.id.edit_description_text);
        EditText editEmail = view.findViewById(R.id.edit_email_text);
        EditText editLocation = view.findViewById(R.id.edit_location_text);
        EditText editPhone = view.findViewById(R.id.edit_phone_text);
        EditText editUsername = view.findViewById(R.id.edit_username_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (getArguments() != null) {
            editingProfile = (User) getArguments().getSerializable("profile");
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
                    listener.editUser(editingProfile,
                            editDescription.getText().toString(),
                            editEmail.getText().toString(),
                            editLocation.getText().toString(),
                            editPhone.getText().toString(),
                            editUsername.getText().toString());
                })
                .create();
    }
}