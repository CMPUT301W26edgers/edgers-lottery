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

/**
 * Dialog fragment that allows a user to filter events by interests and availability dates.
 * Requires the host activity to implement {@link EditFilterDialogListener}.
 */
public class FilterEventsFragment extends DialogFragment {

    private static final String TAG = "FilterEventsFragment";
    private EditText interestsEditText;
    private EditText registrationStartEditText;
    private EditText registrationEndEditText;

    /**
     * Listener interface that must be implemented by the host activity.
     * Called when the user confirms or applies a filter.
     */
    interface EditFilterDialogListener {

        /**
         * Called when filter values are edited.
         *
         * @param interests         the interests keyword to filter by
         * @param registrationStart the start of the availability window
         * @param registrationEnd   the end of the availability window
         */
        void editFilter(String interests, String registrationStart, String registrationEnd);

        /**
         * Called when the user confirms the filter by tapping the Filter button.
         *
         * @param interests         the interests keyword to filter by
         * @param availabilityStart the start of the availability window
         * @param availabilityEnd   the end of the availability window
         */
        void onFilterApplied(String interests, String availabilityStart, String availabilityEnd);
    }

    private EditFilterDialogListener listener;

    /**
     * Attaches the fragment to the host context and verifies it implements {@link EditFilterDialogListener}.
     *
     * @param context the host activity context
     * @throws RuntimeException if the host activity does not implement {@link EditFilterDialogListener}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EditFilterDialogListener) {
            listener = (EditFilterDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement EditFilterDialogListener");
        }
    }

    /**
     * Inflates the filter dialog layout and builds the alert dialog with Cancel and Filter buttons.
     * On confirmation, delegates the filter values to {@link EditFilterDialogListener#onFilterApplied}.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     * @return the fully constructed {@link Dialog}
     */
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