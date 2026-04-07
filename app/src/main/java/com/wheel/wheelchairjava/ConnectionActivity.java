package com.wheel.wheelchairjava;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.HashMap;

public class ConnectionActivity extends AppCompatActivity {

    private Button btnBluetooth, btnWifi;
    private LinearLayout deviceListContainer;
    private ProgressBar progressBar;
    private TextView tvScanningStatus;
    private String currentConnectionType = null;

    private HashMap<String, ArrayList<DeviceInfo>> mockDevices = new HashMap<>();

    static class DeviceInfo {
        String id, name;
        int signal;
        DeviceInfo(String id, String name, int signal) {
            this.id = id; this.name = name; this.signal = signal;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        initMockData();
        initViews();
    }

    private void initMockData() {
        ArrayList<DeviceInfo> bluetoothDevices = new ArrayList<>();
        bluetoothDevices.add(new DeviceInfo("bt-001", "Smart Wheelchair #1", 95));
        bluetoothDevices.add(new DeviceInfo("bt-002", "Smart Wheelchair #2", 78));
        bluetoothDevices.add(new DeviceInfo("bt-003", "Wheelchair Unit A", 62));

        ArrayList<DeviceInfo> wifiDevices = new ArrayList<>();
        wifiDevices.add(new DeviceInfo("wifi-001", "Smart Wheelchair #1", 92));
        wifiDevices.add(new DeviceInfo("wifi-002", "Smart Wheelchair #2", 85));
        wifiDevices.add(new DeviceInfo("wifi-003", "Wheelchair Unit A", 70));

        mockDevices.put("bluetooth", bluetoothDevices);
        mockDevices.put("wifi", wifiDevices);
    }

    private void initViews() {
        btnBluetooth = findViewById(R.id.btnBluetooth);
        btnWifi = findViewById(R.id.btnWifi);
        deviceListContainer = findViewById(R.id.deviceListContainer);
        progressBar = findViewById(R.id.progressBar);
        tvScanningStatus = findViewById(R.id.tvScanningStatus);

        btnBluetooth.setOnClickListener(v -> startScan("bluetooth"));
        btnWifi.setOnClickListener(v -> startScan("wifi"));
    }

    private void startScan(String type) {
        currentConnectionType = type;
        deviceListContainer.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);
        tvScanningStatus.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            tvScanningStatus.setVisibility(View.GONE);
            showDevices(mockDevices.get(type));
        }, 2000);
    }

    private void showDevices(ArrayList<DeviceInfo> devices) {
        View headerView = getLayoutInflater().inflate(R.layout.device_header, null);
        deviceListContainer.addView(headerView);

        for (DeviceInfo device : devices) {
            View deviceView = getLayoutInflater().inflate(R.layout.device_item, null);

            TextView tvDeviceName = deviceView.findViewById(R.id.tvDeviceName);
            TextView tvSignalStrength = deviceView.findViewById(R.id.tvSignalStrength);
            ImageView ivIcon = deviceView.findViewById(R.id.ivDeviceIcon);

            tvDeviceName.setText(device.name);
            tvSignalStrength.setText("Signal: " + device.signal + "%");
            ivIcon.setImageResource(currentConnectionType.equals("bluetooth") ?
                    R.drawable.ic_bluetooth : R.drawable.ic_wifi);

            deviceView.setOnClickListener(v -> {
                Intent intent = new Intent(ConnectionActivity.this, LoginActivity.class);
                intent.putExtra("device_name", device.name);
                intent.putExtra("connection_type", currentConnectionType);
                startActivity(intent);
            });

            deviceListContainer.addView(deviceView);
        }

        Button backButton = new Button(this);
        backButton.setText("Back to Connection Options");
        backButton.setBackgroundResource(R.drawable.btn_outline);
        backButton.setTextColor(getColor(R.color.gray_300));
        backButton.setOnClickListener(v -> resetToOptions());
        deviceListContainer.addView(backButton);
    }

    private void resetToOptions() {
        deviceListContainer.removeAllViews();
        currentConnectionType = null;
        btnBluetooth.setVisibility(View.VISIBLE);
        btnWifi.setVisibility(View.VISIBLE);
    }
}