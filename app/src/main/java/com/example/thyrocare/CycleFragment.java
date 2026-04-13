package com.example.thyrocare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.thyrocare.databinding.FragmentCycleBinding;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CycleFragment extends Fragment {

    private FragmentCycleBinding binding;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat entryDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat monthHeaderFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat selectedDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
    private String selectedStartDate = "";
    private String selectedEndDate = "";
    private Date selectedCalendarDate = new Date();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCycleBinding.inflate(inflater, container, false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.cycle_flow_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFlow.setAdapter(adapter);

        binding.btnStartDate.setOnClickListener(view -> showDatePicker(true));
        binding.btnEndDate.setOnClickListener(view -> showDatePicker(false));
        binding.btnSaveCycle.setOnClickListener(view -> saveCycleEntry());
        binding.cycleCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            selectedCalendarDate = selected.getTime();
            renderSelectedDateEntries(AppStorage.getCycleLogs(requireContext()));
        });

        selectedCalendarDate = new Date(binding.cycleCalendarView.getDate());
        renderHistory();
        return binding.getRoot();
    }

    private void showDatePicker(boolean selectingStart) {
        Calendar calendar = selectingStart ? startCalendar : endCalendar;

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String formattedDate = formatDate(calendar);
                    if (selectingStart) {
                        selectedStartDate = formattedDate;
                        binding.btnStartDate.setText(formattedDate);
                    } else {
                        selectedEndDate = formattedDate;
                        binding.btnEndDate.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private String formatDate(Calendar calendar) {
        return entryDateFormat.format(calendar.getTime());
    }

    private void saveCycleEntry() {
        if (TextUtils.isEmpty(selectedStartDate) || TextUtils.isEmpty(selectedEndDate)) {
            Toast.makeText(getContext(), "Choose both a start and end date.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endCalendar.before(startCalendar)) {
            Toast.makeText(getContext(), "The end date must be on or after the start date.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> symptoms = new ArrayList<>();
        if (binding.cbFatigue.isChecked()) {
            symptoms.add("Fatigue");
        }
        if (binding.cbHeavyBleeding.isChecked()) {
            symptoms.add("Heavy bleeding");
        }
        if (binding.cbMoodShift.isChecked()) {
            symptoms.add("Mood shifts");
        }
        if (binding.cbCramps.isChecked()) {
            symptoms.add("Cramps");
        }
        if (binding.cbIrregularity.isChecked()) {
            symptoms.add("Irregular timing");
        }

        String notes = binding.etCycleNotes.getText().toString().trim();
        CycleLog log = new CycleLog(
                selectedStartDate,
                selectedEndDate,
                binding.spinnerFlow.getSelectedItem().toString(),
                notes,
                symptoms
        );

        AppStorage.addCycleLog(requireContext(), log);
        Toast.makeText(getContext(), "Cycle entry saved.", Toast.LENGTH_SHORT).show();
        clearForm();
        renderHistory();
    }

    private void clearForm() {
        selectedStartDate = "";
        selectedEndDate = "";
        binding.btnStartDate.setText(R.string.select_start_date);
        binding.btnEndDate.setText(R.string.select_end_date);
        binding.spinnerFlow.setSelection(0);
        binding.cbFatigue.setChecked(false);
        binding.cbHeavyBleeding.setChecked(false);
        binding.cbMoodShift.setChecked(false);
        binding.cbCramps.setChecked(false);
        binding.cbIrregularity.setChecked(false);
        binding.etCycleNotes.setText("");
    }

    private void renderHistory() {
        List<CycleLog> logs = AppStorage.getCycleLogs(requireContext());
        binding.historyContainer.removeAllViews();

        if (logs.isEmpty()) {
            binding.tvLatestSummary.setText("No cycle logs yet. Save your first entry to start building a health history.");
            binding.tvSelectedDateSummary.setText(selectedDateFormat.format(selectedCalendarDate) + ": no entries saved for this date yet.");
            binding.selectedDateContainer.removeAllViews();
            return;
        }

        CycleLog latestLog = logs.get(0);
        String latestSummary = latestLog.getStartDate() + " to " + latestLog.getEndDate()
                + " | Flow: " + latestLog.getFlow()
                + " | Symptoms: " + joinSymptoms(latestLog.getSymptoms());
        binding.tvLatestSummary.setText(latestSummary);

        renderSelectedDateEntries(logs);

        String lastMonthHeader = "";
        for (CycleLog log : logs) {
            String monthHeader = getMonthHeader(log.getStartDate());
            if (!monthHeader.equals(lastMonthHeader)) {
                binding.historyContainer.addView(createMonthHeader(monthHeader));
                lastMonthHeader = monthHeader;
            }
            binding.historyContainer.addView(createLogCard(log));
        }
    }

    private void renderSelectedDateEntries(List<CycleLog> logs) {
        binding.selectedDateContainer.removeAllViews();
        List<CycleLog> matchingLogs = new ArrayList<>();

        for (CycleLog log : logs) {
            if (matchesSelectedDate(log)) {
                matchingLogs.add(log);
            }
        }

        if (matchingLogs.isEmpty()) {
            binding.tvSelectedDateSummary.setText(selectedDateFormat.format(selectedCalendarDate) + ": no entries saved for this date yet.");
            return;
        }

        binding.tvSelectedDateSummary.setText(selectedDateFormat.format(selectedCalendarDate) + ": " + matchingLogs.size() + " matching entr" + (matchingLogs.size() == 1 ? "y" : "ies") + ".");
        for (CycleLog log : matchingLogs) {
            binding.selectedDateContainer.addView(createLogCard(log));
        }
    }

    private boolean matchesSelectedDate(CycleLog log) {
        try {
            Date startDate = entryDateFormat.parse(log.getStartDate());
            Date endDate = entryDateFormat.parse(log.getEndDate());
            if (startDate == null || endDate == null) {
                return false;
            }
            return !selectedCalendarDate.before(startDate) && !selectedCalendarDate.after(endDate);
        } catch (ParseException ignored) {
            return false;
        }
    }

    private View createMonthHeader(String monthHeader) {
        TextView header = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dpToPx(6);
        params.bottomMargin = dpToPx(10);
        header.setLayoutParams(params);
        header.setText(monthHeader);
        header.setTextColor(0xFF5F3F63);
        header.setTextSize(18);
        header.setTypeface(header.getTypeface(), android.graphics.Typeface.BOLD);
        return header;
    }

    private View createLogCard(CycleLog log) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dpToPx(12);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(18));
        cardView.setCardBackgroundColor(0xFFFFFFFF);
        cardView.setStrokeColor(0xFFB86A84);
        cardView.setStrokeWidth(dpToPx(1));

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        TextView title = new TextView(requireContext());
        title.setText(log.getStartDate() + " to " + log.getEndDate());
        title.setTextSize(16);
        title.setTextColor(0xFF5F3F63);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        TextView subtitle = new TextView(requireContext());
        subtitle.setText("Flow: " + log.getFlow() + " | Symptoms: " + joinSymptoms(log.getSymptoms()));
        subtitle.setTextColor(0xFF6F6573);

        TextView noteLabel = new TextView(requireContext());
        noteLabel.setText("Notes for " + log.getStartDate());
        noteLabel.setTextColor(0xFF5F3F63);
        noteLabel.setTypeface(noteLabel.getTypeface(), android.graphics.Typeface.BOLD);
        noteLabel.setPadding(0, dpToPx(10), 0, 0);

        TextView note = new TextView(requireContext());
        String notes = log.getNotes().isEmpty() ? "No notes added." : log.getNotes();
        note.setText(notes);
        note.setTextColor(0xFF5F3F63);
        note.setPadding(0, dpToPx(6), 0, 0);

        container.addView(title);
        container.addView(subtitle);
        container.addView(noteLabel);
        container.addView(note);
        cardView.addView(container);
        return cardView;
    }

    private String getMonthHeader(String startDate) {
        try {
            Date parsedDate = entryDateFormat.parse(startDate);
            if (parsedDate != null) {
                return monthHeaderFormat.format(parsedDate);
            }
        } catch (ParseException ignored) {
        }
        return "Unknown month";
    }

    private String joinSymptoms(List<String> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return "none logged";
        }
        return TextUtils.join(", ", symptoms);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
