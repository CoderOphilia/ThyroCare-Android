package com.example.thyrocare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.thyrocare.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.signupRoot, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnCreateAccount.setOnClickListener(view -> attemptSignup());
        binding.btnBackToLogin.setOnClickListener(view -> finish());
    }

    private void attemptSignup() {
        String name = binding.signupName.getText().toString().trim();
        String email = binding.signupEmail.getText().toString().trim();
        String password = binding.signupPassword.getText().toString();
        String confirmPassword = binding.signupConfirmPassword.getText().toString();

        if (name.isEmpty()) {
            binding.signupName.setError(getString(R.string.signup_name_required));
            binding.signupName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            binding.signupEmail.setError(getString(R.string.signup_email_required));
            binding.signupEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.signupPassword.setError(getString(R.string.signup_password_short));
            binding.signupPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.signupConfirmPassword.setError(getString(R.string.signup_password_mismatch));
            binding.signupConfirmPassword.requestFocus();
            return;
        }

        AppStorage.saveAccount(this, name, email, password);
        Toast.makeText(this, R.string.signup_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, home.class);
        intent.putExtra("USER_NAME", name);
        startActivity(intent);
        finishAffinity();
    }
}
