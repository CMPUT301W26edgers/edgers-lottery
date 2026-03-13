package com.example.edgers_lottery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class FilterEventsFragment extends DialogFragment {
    private static final String TAG = "FilterEventsFragment";
    EditText editInterests;
    EditText editAvailabilityStart;
    EditText editAvailabilityEnd;
    interface EditFilterDialogListener {
//        void editFilter(String interests, String registrationStart, String registrationEnd);

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
        editInterests = view.findViewById(R.id.edit_interests_text);
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
                    String interests = editInterests.getText().toString().trim();
                    String availabilityStart = editAvailabilityStart.getText().toString().trim();
                    String availabilityEnd = editAvailabilityEnd.getText().toString().trim();

                    listener.onFilterApplied(interests, availabilityStart, availabilityEnd);

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
