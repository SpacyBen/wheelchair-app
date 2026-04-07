package com.wheel.wheelchairjava;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvDeviceName;
    private ProgressBar progressBar;
    private String deviceName;
    private String connectionType;

    private static final String DEFAULT_PASSWORD = "admin123";
    private static final String PREFS_NAME = "WheelchairPrefs";
    private static final String KEY_PASSWORD = "wheelchair_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        deviceName = getIntent().getStringExtra("device_name");
        connectionType = getIntent().getStringExtra("connection_type");
        if (deviceName == null) deviceName = "Smart Wheelchair";

        initViews();
    }

    private void initViews() {
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvDeviceName = findViewById(R.id.tvDeviceName);
        progressBar = findViewById(R.id.progressBar);

        // Find tvDefaultHint if it exists in your layout, otherwise remove this line
        TextView tvDefaultHint = findViewById(R.id.tvDefaultHint);
        if (tvDefaultHint != null) {
            tvDefaultHint.setText(R.string.default_password_hint);
        }

        tvDeviceName.setText(String.format(getString(R.string.connecting_to), deviceName));

        btnLogin.setOnClickListener(v -> authenticate());
    }

    private void authenticate() {
        String enteredPassword = etPassword.getText() != null ? etPassword.getText().toString() : "";

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedPassword = prefs.getString(KEY_PASSWORD, DEFAULT_PASSWORD);

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        etPassword.setEnabled(false);

        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            etPassword.setEnabled(true);

            if (enteredPassword.equals(storedPassword)) {
                Toast.makeText(LoginActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();

                // Clear any existing back stack and start Dashboard
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra("device_name", deviceName);
                intent.putExtra("connection_type", connectionType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                if (etPassword != null) {
                    etPassword.setText("");
                }
            }
        }, 1000);
    }
}