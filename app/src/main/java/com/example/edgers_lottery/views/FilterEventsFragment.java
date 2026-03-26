package com.example.edgers_lottery.views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog fragment that allows a user to filter events by interests and availability dates.
 * Requires the host activity to implement {@link EditFilterDialogListener}.
 */
import com.example.edgers_lottery.R;

import java.util.Calendar;

public class FilterEventsFragment extends DialogFragment {

    private static final String TAG = "FilterEventsFragment";
    CheckBox capacity_bool;
    EditText editAvailabilityStart;
    EditText editAvailabilityEnd;
     /**
     * Listener interface that must be implemented by the host activity.
     * Called when the user confirms or applies a filter.
     */
    interface EditFilterDialogListener {
//        void editFilter(String interests, String registrationStart, String registrationEnd);

        /**
         * Called when the user confirms the filter by tapping the Filter button.
         *
         * @param isChecked         the interests keyword to filter by
         * @param availabilityStart the start of the availability window
         * @param availabilityEnd   the end of the availability window
         */
        void onFilterApplied(boolean isChecked, String availabilityStart, String availabilityEnd);
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
        capacity_bool = view.findViewById(R.id.capacity_checkbox);
        editAvailabilityStart = view.findViewById(R.id.edit_availability_start_text);
        editAvailabilityEnd = view.findViewById(R.id.edit_availability_end_text);

        // Prevent keyboard from popping up on the date fields
        editAvailabilityStart.setFocusable(false);
        editAvailabilityEnd.setFocusable(false);

        editAvailabilityStart.setOnClickListener(v -> showDatePicker(editAvailabilityStart));
        editAvailabilityEnd.setOnClickListener(v -> showDatePicker(editAvailabilityEnd));

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Filter Events")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Filter", (dialog, which) -> {
                    boolean isChecked = capacity_bool.isChecked();
                    String availabilityStart = editAvailabilityStart.getText().toString().trim();
                    String availabilityEnd = editAvailabilityEnd.getText().toString().trim();

                    listener.onFilterApplied(isChecked, availabilityStart, availabilityEnd);
                })
                .create();
    }
    private void showDatePicker(EditText targetField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getContext(), (datePicker, y, m, d) -> { // date format is yyyy-mm-dd
            String date = y + "-" + String.format("%02d", m + 1) + "-" + String.format("%02d", d);
            targetField.setText(date);
        }, year, month, day).show();
    }
}
