package com.example.thyrocare;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        // 1. Core Logic Setup
        checkYearlyReset();
        totalPoints = sharedPreferences.getInt("TOTAL_POINTS", 0);

        // 2. UI Setup
        setRandomQuote();
        updateUI();
        updateYearlyProgressUI();

        // 3. Set Personal Greeting
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String username = bundle.getString("USER_NAME", "Ophelia");
            binding.tvFragmentWelcome.setText("Hi " + username);
        }

        // 4. Setup Daily Locks
        setupDailyLock("DONE_MEDICINE", binding.cbMedicine);
        setupDailyLock("DONE_GYM", binding.cbGym);
        setupDailyLock("DONE_GLUTEN_FREE", binding.cbGlutenFree);

        // 5. Checkbox Listeners
        binding.cbMedicine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !isTaskDoneToday("DONE_MEDICINE")) {
                playSparkleAnimation(binding.cbMedicine);
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

                if (hour < 6) {
                    addPoints(50);
                    Toast.makeText(getContext(), "Consistency Badge! +50 SP", Toast.LENGTH_SHORT).show();
                } else {
                    addPoints(10);
                }

                markTaskAsDone("DONE_MEDICINE");
                incrementYearlyCount("YEAR_MEDS_COUNT");
                binding.cbMedicine.setEnabled(false);
            }
        });

        binding.cbGym.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked && !isTaskDoneToday("DONE_GYM")) {
                playSparkleAnimation(binding.cbGym);
                addPoints(20);
                markTaskAsDone("DONE_GYM");
                incrementYearlyCount("YEAR_GYM_COUNT");
                binding.cbGym.setEnabled(false);
            }
        });

        binding.cbGlutenFree.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked && !isTaskDoneToday("DONE_GLUTEN_FREE")) {
                playSparkleAnimation(binding.cbGlutenFree);
                addPoints(20);
                markTaskAsDone("DONE_GLUTEN_FREE");
                incrementYearlyCount("YEAR_FOOD_COUNT");
                binding.cbGlutenFree.setEnabled(false);
            }
        });

        return binding.getRoot();
    }

    // --- QUOTES & UI REFRESH ---

    private void setRandomQuote() {
        String[] quotes = {
                "Small daily magic creates lifelong mastery.",
                "Your discipline is your strongest spell.",
                "Nourish to flourish.",
                "Consistency is the truest form of self-love.",
                "Healing happens one choice at a time."
        };
        int randomIndex = new Random().nextInt(quotes.length);
        binding.tvQuote.setText("“" + quotes[randomIndex] + "”");
    }

    // --- YEARLY PROGRESS TRACKER ---

    private void checkYearlyReset() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int savedYear = sharedPreferences.getInt("CURRENT_SAVED_YEAR", currentYear);

        // If the year changes (e.g., from 2026 to 2027), reset all trackers to 0
        if (currentYear != savedYear) {
            sharedPreferences.edit()
                    .putInt("CURRENT_SAVED_YEAR", currentYear)
                    .putInt("YEAR_MEDS_COUNT", 0)
                    .putInt("YEAR_GYM_COUNT", 0)
                    .putInt("YEAR_FOOD_COUNT", 0)
                    .apply();
        }
    }

    private void incrementYearlyCount(String key) {
        int currentCount = sharedPreferences.getInt(key, 0);
        sharedPreferences.edit().putInt(key, currentCount + 1).apply();
        updateYearlyProgressUI();
    }

    private void updateYearlyProgressUI() {
        int medsCount = sharedPreferences.getInt("YEAR_MEDS_COUNT", 0);
        int gymCount = sharedPreferences.getInt("YEAR_GYM_COUNT", 0);
        int foodCount = sharedPreferences.getInt("YEAR_FOOD_COUNT", 0);

        binding.tvMedsProgress.setText("Morning Magic: " + medsCount + "/365");
        binding.pbMeds.setProgress(medsCount);

        binding.tvGymProgress.setText("Fairy Strength: " + gymCount + "/365");
        binding.pbGym.setProgress(gymCount);

        binding.tvFoodProgress.setText("Pure Glow: " + foodCount + "/365");
        binding.pbFood.setProgress(foodCount);
    }

    // --- DATE LOGIC HELPERS ---

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean isTaskDoneToday(String taskKey) {
        return sharedPreferences.getString(taskKey, "").equals(getTodayDate());
    }

    private void markTaskAsDone(String taskKey) {
        sharedPreferences.edit().putString(taskKey, getTodayDate()).apply();
    }

    private void setupDailyLock(String taskKey, CheckBox checkBox) {
        if (isTaskDoneToday(taskKey)) {
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(true);
            checkBox.setEnabled(false);
        }
    }

    // --- POINTS & THEME LOGIC ---

    private void addPoints(int pts) {
        totalPoints += pts;
        sharedPreferences.edit().putInt("TOTAL_POINTS", totalPoints).apply();
        updateUI();
    }

    private void updateUI() {
        binding.tvPoints.setText("✨ " + totalPoints + " SP");

        if (totalPoints > 1500) {
            applyFairyTheme("#B2AC88"); // Sage Green
        } else if (totalPoints > 500) {
            applyFairyTheme("#C8A2C8"); // Lilac
        } else {
            applyFairyTheme("#F4C2C2"); // Baby Pink
        }
    }

    private void applyFairyTheme(String colorCode) {
        int color = Color.parseColor(colorCode);
        ColorStateList colorList = ColorStateList.valueOf(color);

        binding.tvPoints.setTextColor(color);
        binding.cbMedicine.setButtonTintList(colorList);
        binding.cbGym.setButtonTintList(colorList);
        binding.cbGlutenFree.setButtonTintList(colorList);

        binding.cardMedicine.setStrokeColor(color);
        binding.cardGym.setStrokeColor(color);
        binding.cardDiet.setStrokeColor(color);
    }

    private void playSparkleAnimation(View view) {
        view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}