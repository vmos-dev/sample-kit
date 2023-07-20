package com.samplekit.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.samplekit.app.databinding.ActivityMainBinding;
import com.samplekit.bean.FileItem;
import com.samplekit.dialog.DeviceFileSelectorDialog;
import com.samplekit.dialog.DeviceInstalledAppDialog;

public class MainActivity extends AppCompatActivity {
    private final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnFiles.setOnClickListener(v -> {
            startDeviceFileSelector(binding.btnFiles.getText().toString(), null, false);
        });
        binding.btnApk.setOnClickListener(v -> {
            startDeviceFileSelector(binding.btnApk.getText().toString(), new String[]{".apk"}, false);
        });
        binding.btnDir.setOnClickListener(v -> {
            startDeviceFileSelector(binding.btnDir.getText().toString(), null, true);
        });
        binding.btnInstalledPackages.setOnClickListener(v -> {
            new DeviceInstalledAppDialog(binding.btnInstalledPackages.getText().toString())
                    .setOnClickInstalledItemListener((item, position) ->
                            setMessage(gsonPretty.toJson(item)))
                    .show(getSupportFragmentManager());
        });
    }

    private void startDeviceFileSelector(String title, String[] fileSuffixes, boolean supportUseDirectory) {
        DeviceFileSelectorDialog.newInstance(title, fileSuffixes, supportUseDirectory)
                .setOnFileSelectorListener(new DeviceFileSelectorDialog.OnFileSelectorListener() {
                    @Override
                    public void onFileSelected(FileItem item) {
                        setMessage(item.getAbsolutePath());
                    }
                })
                .show(getSupportFragmentManager());
    }

    private void setMessage(CharSequence text) {
        binding.tvLog.setText(text);
    }
}