package com.example.edgers_lottery;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FilterEventsFragment extends DialogFragment {
    private static final String TAG = "FilterEventsFragment";
    private EditText interestsEditText;
    private EditText registrationStartEditText;
    private EditText registrationEndEditText;
    interface EditFilterDialogListener {
        void editFilter(String interests, String registrationStart, String registrationEnd);

        void onFilterApplied(String interests, String availabilityStart, String availabilityEnd);
    }
    private EditFilterDialogListener listener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditFilterDialogListener) {
            listener = (EditFilterDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement EditFilterDialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_filter_events, null);
        EditText editInterests = view.findViewById(R.id.edit_interests_text);
        EditText editRegistrationStart = view.findViewById(R.id.edit_registration_start_text);
        EditText editRegistrationEnd = view.findViewById(R.id.edit_registration_end_text);
        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Filter Events")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Filter", (dialog, which) -> {
                    String interests = editInterests.getText().toString().trim();
                    String availabilityStart = editRegistrationStart.getText().toString().trim();
                    String availabilityEnd = editRegistrationEnd.getText().toString().trim();

                    listener.onFilterApplied(interests, availabilityStart, availabilityEnd);

                })
                .create();
    }

}
