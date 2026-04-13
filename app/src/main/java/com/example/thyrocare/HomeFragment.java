package com.example.thyrocare;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.thyrocare.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private int totalPoints = 0;
    private boolean syncingTaskState = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        Context context = requireContext();
        AppStorage.resetYearIfNeeded(context);
        totalPoints = AppStorage.getTotalPoints(context);

        setRandomQuote();
        setPersonalGreeting();
        updateUI();
        updateYearlyProgressUI();
        refreshDailyState();
        attachTaskListeners();

        binding.btnLogout.setOnClickListener(view -> {
            if (requireActivity() instanceof home) {
                ((home) requireActivity()).logoutToWelcome();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            totalPoints = AppStorage.getTotalPoints(requireContext());
            AppStorage.resetYearIfNeeded(requireContext());
            updateUI();
            updateYearlyProgressUI();
            refreshDailyState();
        }
    }

    private void setPersonalGreeting() {
        Bundle bundle = getArguments();
        String username = AppStorage.getDisplayName(requireContext());
        if (bundle != null) {
            username = bundle.getString("USER_NAME", username);
        }
        binding.tvFragmentWelcome.setText("Hi " + username);
    }

    private void setRandomQuote() {
        String[] quotes = {
                "Small daily steps create long-term healing.",
                "Discipline is how your future self feels supported.",
                "Nourish your body and your hormones will thank you.",
                "Consistency is stronger than motivation.",
                "Every check-in makes the next one easier."
        };
        int randomIndex = new Random().nextInt(quotes.length);
        binding.tvQuote.setText("\"" + quotes[randomIndex] + "\"");
    }

    private void refreshDailyState() {
        binding.tvTodayDate.setText(formatTodayDisplay());
        setupDailyLock("DONE_MEDICINE", binding.cbMedicine);
        setupDailyLock("DONE_GYM", binding.cbGym);
        setupDailyLock("DONE_GLUTEN_FREE", binding.cbGlutenFree);
    }

    private void attachTaskListeners() {
        attachTaskListener(binding.cbMedicine, "DONE_MEDICINE", () -> {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour < 6) {
                addPoints(50);
                Toast.makeText(getContext(), "Medication logged before 6:00 AM. +50 points.", Toast.LENGTH_SHORT).show();
            } else {
                addPoints(10);
                Toast.makeText(getContext(), "Medication logged. +10 points.", Toast.LENGTH_SHORT).show();
            }
            AppStorage.incrementCounter(requireContext(), "YEAR_MEDS_COUNT");
            updateYearlyProgressUI();
        });

        attachTaskListener(binding.cbGym, "DONE_GYM", () -> {
            addPoints(20);
            AppStorage.incrementCounter(requireContext(), "YEAR_GYM_COUNT");
            updateYearlyProgressUI();
        });

        attachTaskListener(binding.cbGlutenFree, "DONE_GLUTEN_FREE", () -> {
            addPoints(20);
            AppStorage.incrementCounter(requireContext(), "YEAR_FOOD_COUNT");
            updateYearlyProgressUI();
        });
    }

    private void attachTaskListener(CheckBox checkBox, String taskKey, Runnable onAward) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (syncingTaskState) {
                return;
            }

            if (!isChecked || isTaskDoneToday(taskKey)) {
                return;
            }

            playSparkleAnimation(checkBox);
            markTaskAsDone(taskKey);
            onAward.run();
            checkBox.setEnabled(false);
        });
    }

    private void updateYearlyProgressUI() {
        Context context = requireContext();
        int medsCount = AppStorage.getCounter(context, "YEAR_MEDS_COUNT");
        int gymCount = AppStorage.getCounter(context, "YEAR_GYM_COUNT");
        int foodCount = AppStorage.getCounter(context, "YEAR_FOOD_COUNT");

        binding.tvMedsProgress.setText("Medication discipline: " + medsCount + "/365");
        binding.pbMeds.setProgress(medsCount);

        binding.tvGymProgress.setText("Movement support: " + gymCount + "/365");
        binding.pbGym.setProgress(gymCount);

        binding.tvFoodProgress.setText("Nutrition support: " + foodCount + "/365");
        binding.pbFood.setProgress(foodCount);
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String formatTodayDisplay() {
        return new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(new Date());
    }

    private boolean isTaskDoneToday(String taskKey) {
        return AppStorage.getTaskDate(requireContext(), taskKey).equals(getTodayDate());
    }

    private void markTaskAsDone(String taskKey) {
        AppStorage.setTaskDate(requireContext(), taskKey, getTodayDate());
    }

    private void setupDailyLock(String taskKey, CheckBox checkBox) {
        syncingTaskState = true;
        boolean doneToday = isTaskDoneToday(taskKey);
        checkBox.setChecked(doneToday);
        checkBox.setEnabled(!doneToday);
        syncingTaskState = false;
    }

    private void addPoints(int pointsToAdd) {
        totalPoints += pointsToAdd;
        AppStorage.setTotalPoints(requireContext(), totalPoints);
        updateUI();
        if (requireActivity() instanceof home) {
            ((home) requireActivity()).refreshChromeTheme();
        }
    }

    private void updateUI() {
        ThemePalette palette = ThemePalette.fromPoints(totalPoints);
        binding.tvPoints.setText(totalPoints + " pts");
        binding.tvLevel.setText(palette.levelName);

        if (palette.nextGoal > 0) {
            int remaining = Math.max(0, palette.nextGoal - totalPoints);
            binding.tvMilestone.setText(palette.statusMessage + " " + remaining + " points until the next unlock.");
        } else {
            binding.tvMilestone.setText(palette.statusMessage);
        }

        applyTheme(palette);
    }

    private void applyTheme(ThemePalette palette) {
        ColorStateList accentList = ColorStateList.valueOf(palette.accentColor);

        binding.homeRoot.setBackgroundColor(palette.backgroundColor);
        binding.headerCard.setCardBackgroundColor(palette.surfaceColor);
        binding.tvPoints.setTextColor(palette.accentColor);
        binding.tvLevel.setTextColor(palette.accentColor);
        binding.cbMedicine.setButtonTintList(accentList);
        binding.cbGym.setButtonTintList(accentList);
        binding.cbGlutenFree.setButtonTintList(accentList);
        binding.cardMedicine.setStrokeColor(palette.accentColor);
        binding.cardGym.setStrokeColor(palette.accentColor);
        binding.cardDiet.setStrokeColor(palette.accentColor);
        binding.pbMeds.setProgressTintList(accentList);
        binding.pbGym.setProgressTintList(accentList);
        binding.pbFood.setProgressTintList(accentList);
        binding.btnLogout.setBackgroundTintList(accentList);
    }

    private void playSparkleAnimation(View view) {
        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(150)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                .start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
