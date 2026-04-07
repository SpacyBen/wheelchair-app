package com.wheel.wheelchairjava;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class ControlActivity extends AppCompatActivity {

    private SeekBar speedSeekBar;
    private TextView tvSpeed, tvBattery, tvConnectionStatus, tvObstacleStatus, tvLiveTime;
    private Button btnUp, btnDown, btnLeft, btnRight, btnEmergencyStop;
    private ImageButton btnVoice;
    private CardView cardMovement;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnMenu;

    private boolean isListening = false;
    private boolean appControlEnabled = true;
    private boolean wheelchairOnlyMode = false;
    private final Handler timeHandler = new Handler();
    private Runnable timeRunnable;
    private String deviceName;
    private String connectionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_drawer);

        deviceName = getIntent().getStringExtra("device_name");
        connectionType = getIntent().getStringExtra("connection_type");

        initViews();
        setupListeners();
        setupNavigationDrawer();
        startLiveTime();
        setupBackPressedCallback();
    }

    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initViews() {
        speedSeekBar = findViewById(R.id.speedSeekBar);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvBattery = findViewById(R.id.tvBattery);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvObstacleStatus = findViewById(R.id.tvObstacleStatus);
        tvLiveTime = findViewById(R.id.tvLiveTime);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnEmergencyStop = findViewById(R.id.btnEmergencyStop);
        btnVoice = findViewById(R.id.btnVoice);
        cardMovement = findViewById(R.id.cardMovement);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenu = findViewById(R.id.btnMenu);

        // Set initial values using string resources
        tvBattery.setText(getString(R.string.battery_percent_format, 85));

        String connectionText = getString(R.string.connected_via,
                connectionType != null ? connectionType : getString(R.string.bluetooth));
        tvConnectionStatus.setText(connectionText);

        tvObstacleStatus.setText(R.string.clear);
        tvSpeed.setText(getString(R.string.speed_percent_format, 50));
        speedSeekBar.setProgress(50);

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSpeed.setText(getString(R.string.speed_percent_format, progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupNavigationDrawer() {
        // Inflate header if not already present
        if (navigationView.getHeaderCount() == 0) {
            navigationView.addHeaderView(getLayoutInflater().inflate(R.layout.nav_header, navigationView, false));
        }

        View headerView = navigationView.getHeaderView(0);

        TextView navBattery = headerView.findViewById(R.id.navBattery);
        TextView navConnection = headerView.findViewById(R.id.navConnection);
        TextView navObstacle = headerView.findViewById(R.id.navObstacle);
        TextView navDeviceName = headerView.findViewById(R.id.navDeviceName);
        SwitchCompat switchAppControl = headerView.findViewById(R.id.navSwitchAppControl);
        SwitchCompat switchWheelchairOnly = headerView.findViewById(R.id.navSwitchWheelchairOnly);
        Button btnBackToDashboard = headerView.findViewById(R.id.navBackToDashboard);
        Button btnDisconnect = headerView.findViewById(R.id.navDisconnect);

        // Set current status
        navBattery.setText(tvBattery.getText());
        navConnection.setText(tvConnectionStatus.getText());
        navObstacle.setText(tvObstacleStatus.getText());
        if (navDeviceName != null && deviceName != null) {
            navDeviceName.setText(deviceName);
        }
        switchAppControl.setChecked(appControlEnabled);
        switchWheelchairOnly.setChecked(wheelchairOnlyMode);

        // Switch listeners
        switchAppControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (wheelchairOnlyMode) {
                switchAppControl.setChecked(false);
                Toast.makeText(this, R.string.cannot_enable_app_control, Toast.LENGTH_SHORT).show();
                return;
            }
            appControlEnabled = isChecked;
            updateControlUI();
        });

        switchWheelchairOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            wheelchairOnlyMode = isChecked;
            if (isChecked) {
                appControlEnabled = false;
                switchAppControl.setChecked(false);
                isListening = false;
                btnVoice.setBackgroundTintList(getColorStateList(R.color.blue_600));
                Toast.makeText(this, R.string.wheelchair_only_mode_active, Toast.LENGTH_SHORT).show();
            }
            updateControlUI();
        });

        // Button listeners
        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ControlActivity.this, DashboardActivity.class);
            intent.putExtra("device_name", deviceName);
            intent.putExtra("connection_type", connectionType);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.END);
            finish();
        });

        btnDisconnect.setOnClickListener(v -> {
            Intent intent = new Intent(ControlActivity.this, ConnectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupListeners() {
        View.OnClickListener movementListener = v -> {
            if (!appControlEnabled || wheelchairOnlyMode) {
                Toast.makeText(this, R.string.app_controls_disabled, Toast.LENGTH_SHORT).show();
                return;
            }
            String direction = "";
            if (v.getId() == R.id.btnUp) direction = getString(R.string.forward);
            else if (v.getId() == R.id.btnDown) direction = getString(R.string.backward);
            else if (v.getId() == R.id.btnLeft) direction = getString(R.string.left);
            else if (v.getId() == R.id.btnRight) direction = getString(R.string.right);
            Toast.makeText(this, getString(R.string.moving, direction), Toast.LENGTH_SHORT).show();
        };

        btnUp.setOnClickListener(movementListener);
        btnDown.setOnClickListener(movementListener);
        btnLeft.setOnClickListener(movementListener);
        btnRight.setOnClickListener(movementListener);

        btnEmergencyStop.setOnClickListener(v -> {
            Toast.makeText(this, R.string.emergency_stop_activated, Toast.LENGTH_LONG).show();
            speedSeekBar.setProgress(0);
            tvSpeed.setText(getString(R.string.speed_percent_format, 0));
        });

        btnVoice.setOnClickListener(v -> {
            if (!appControlEnabled || wheelchairOnlyMode) {
                Toast.makeText(this, R.string.voice_control_disabled, Toast.LENGTH_SHORT).show();
                return;
            }
            isListening = !isListening;
            btnVoice.setBackgroundTintList(getColorStateList(isListening ?
                    R.color.red_600 : R.color.blue_600));
            Toast.makeText(this, isListening ? R.string.listening_voice : R.string.voice_stopped, Toast.LENGTH_SHORT).show();
        });

        // Open drawer when menu button clicked
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                updateDrawerHeader();
                drawerLayout.openDrawer(GravityCompat.END);
            });
        }
    }

    private void updateDrawerHeader() {
        if (navigationView.getHeaderCount() > 0) {
            View headerView = navigationView.getHeaderView(0);
            TextView navBattery = headerView.findViewById(R.id.navBattery);
            TextView navConnection = headerView.findViewById(R.id.navConnection);
            TextView navObstacle = headerView.findViewById(R.id.navObstacle);
            SwitchCompat switchAppControl = headerView.findViewById(R.id.navSwitchAppControl);
            SwitchCompat switchWheelchairOnly = headerView.findViewById(R.id.navSwitchWheelchairOnly);

            if (navBattery != null) navBattery.setText(tvBattery.getText());
            if (navConnection != null) navConnection.setText(tvConnectionStatus.getText());
            if (navObstacle != null) navObstacle.setText(tvObstacleStatus.getText());
            if (switchAppControl != null) switchAppControl.setChecked(appControlEnabled);
            if (switchWheelchairOnly != null) switchWheelchairOnly.setChecked(wheelchairOnlyMode);
        }
    }

    private void updateControlUI() {
        boolean controlsDisabled = !appControlEnabled || wheelchairOnlyMode;
        float alpha = controlsDisabled ? 0.4f : 1.0f;
        cardMovement.setAlpha(alpha);
        btnVoice.setEnabled(!controlsDisabled);
        speedSeekBar.setEnabled(!controlsDisabled);
    }

    private void startLiveTime() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm:ss a",
                        java.util.Locale.getDefault());
                tvLiveTime.setText(sdf.format(new java.util.Date()));
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }
}