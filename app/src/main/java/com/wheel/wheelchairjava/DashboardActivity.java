package com.wheel.wheelchairjava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView cardControl, cardChangePassword;
    private Button btnDisconnect;
    private String deviceName;
    private String connectionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        deviceName = getIntent().getStringExtra("device_name");
        connectionType = getIntent().getStringExtra("connection_type");

        initViews();
    }

    private void initViews() {
        cardControl = findViewById(R.id.cardControl);
        cardChangePassword = findViewById(R.id.cardChangePassword);
        btnDisconnect = findViewById(R.id.btnDisconnect);

        cardControl.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ControlActivity.class);
            intent.putExtra("device_name", deviceName);
            intent.putExtra("connection_type", connectionType);
            startActivity(intent);
        });

        cardChangePassword.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ChangePasswordActivity.class)));

        btnDisconnect.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ConnectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}