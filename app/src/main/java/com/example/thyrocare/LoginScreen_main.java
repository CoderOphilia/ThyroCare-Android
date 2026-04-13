package com.example.thyrocare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginScreen_main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_screen_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.loginBtn2).setOnClickListener(this::onClickLoginBtn2);
        findViewById(R.id.btnGoToSignup).setOnClickListener(view ->
                startActivity(new Intent(LoginScreen_main.this, SignupActivity.class)));
    }


    public void onClickLoginBtn2(View view) {
        android.widget.EditText usernameEditText = findViewById(R.id.loginEmail);
        android.widget.EditText passwordEditText = findViewById(R.id.loginPassword);
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (username.isEmpty()) {
            usernameEditText.setError(getString(R.string.login_required));
            Toast.makeText(this, R.string.login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.password_required));
            passwordEditText.requestFocus();
            return;
        }

        if (!AppStorage.hasAccount(this)) {
            Toast.makeText(this, R.string.no_account_yet, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SignupActivity.class));
            return;
        }

        if (!AppStorage.validateLogin(this, username, password)) {
            Toast.makeText(this, R.string.login_invalid_credentials, Toast.LENGTH_LONG).show();
            return;
        }

        String displayName = AppStorage.getAccountName(this);
        AppStorage.saveDisplayName(this, displayName);

        Intent intent = new Intent(LoginScreen_main.this, home.class);
        intent.putExtra("USER_NAME", displayName);
        startActivity(intent);
    }
}
