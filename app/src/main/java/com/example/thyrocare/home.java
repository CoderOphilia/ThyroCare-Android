package com.example.thyrocare;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.thyrocare.databinding.ActivityHomeBinding;

public class home extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set Default Fragment (Home Dashboard) when app opens
        loadFragment(new HomeFragment());

        // Setup Bottom Navigation Listener
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_period) {
                loadFragment(new CycleFragment()); // For Track Cycle
                return true;
            } else if (id == R.id.nav_diet) {
                loadFragment(new DietFragment()); // For Food Options
                return true;
            }
            return false;
        });
    }

    // Helper method to switch between different ThyroCare screens
    private void loadFragment(Fragment fragment) {
        // 1. Get the username from the Intent
        String username = getIntent().getStringExtra("USER_NAME");

        // 2. Create a Bundle to send the name to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("USER_NAME", username);
        fragment.setArguments(bundle);


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}