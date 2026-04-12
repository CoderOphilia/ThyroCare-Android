package com.example.thyrocare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginScreen_main extends AppCompatActivity {

    private Button loginBtn2;


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

        loginBtn2 = findViewById(R.id.loginBtn2);

    }


    public void onClickLoginBtn2(View view) {
        EditText usernameEditText = findViewById(R.id.loginEmail);
        String username = usernameEditText.getText().toString();

        Intent intent = new Intent(LoginScreen_main.this, home.class);
        intent.putExtra("USER_NAME", username);
        startActivity(intent);
    }
}