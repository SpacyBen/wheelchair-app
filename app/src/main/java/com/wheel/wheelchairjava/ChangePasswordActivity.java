package com.wheel.wheelchairjava;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrent, etNewPassword, etConfirmPassword;
    private Button btnUpdate;

    private static final String PREFS_NAME = "WheelchairPrefs";
    private static final String KEY_PASSWORD = "wheelchair_password";
    private static final String DEFAULT_PASSWORD = "admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> changePassword());
    }

    private void initViews() {
        etCurrent = findViewById(R.id.etCurrent);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdate = findViewById(R.id.btnUpdate);
    }

    private void changePassword() {
        String current = etCurrent.getText().toString();
        String newPass = etNewPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedPassword = prefs.getString(KEY_PASSWORD, DEFAULT_PASSWORD);

        if (!current.equals(storedPassword)) {
            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit().putString(KEY_PASSWORD, newPass).apply();
        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}