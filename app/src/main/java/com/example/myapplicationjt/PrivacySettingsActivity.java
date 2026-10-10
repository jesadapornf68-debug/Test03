package com.example.myapplicationjt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class PrivacySettingsActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;
    private SharedPreferences prefs;

    private SwitchMaterial switchShowProfile;
    private SwitchMaterial switchSearchPhone;
    private SwitchMaterial switchHideAppt;
    private SwitchMaterial switchAppNotify;
    private SwitchMaterial switchEmailNotify;
    private SwitchMaterial switchSmsNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);
        prefs = getSharedPreferences("PrivacyPrefs", Context.MODE_PRIVATE);

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        switchShowProfile = findViewById(R.id.switchShowProfile);
        switchSearchPhone = findViewById(R.id.switchSearchPhone);
        switchHideAppt = findViewById(R.id.switchHideAppt);
        switchAppNotify = findViewById(R.id.switchAppNotify);
        switchEmailNotify = findViewById(R.id.switchEmailNotify);
        switchSmsNotify = findViewById(R.id.switchSmsNotify);

        // Load Saved Switch States
        loadSwitchStates();

        // Setup Switch Listeners
        setupSwitchListeners();

        // 2. Delete Account Button
        MaterialCardView btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }
    }

    private void loadSwitchStates() {
        if (switchShowProfile != null) switchShowProfile.setChecked(prefs.getBoolean("show_profile", true));
        if (switchSearchPhone != null) switchSearchPhone.setChecked(prefs.getBoolean("search_phone", true));
        if (switchHideAppt != null) switchHideAppt.setChecked(prefs.getBoolean("hide_appt", false));
        if (switchAppNotify != null) switchAppNotify.setChecked(prefs.getBoolean("app_notify", true));
        if (switchEmailNotify != null) switchEmailNotify.setChecked(prefs.getBoolean("email_notify", true));
        if (switchSmsNotify != null) switchSmsNotify.setChecked(prefs.getBoolean("sms_notify", true));
    }

    private void setupSwitchListeners() {
        if (switchShowProfile != null) {
            switchShowProfile.setOnCheckedChangeListener((btn, isChecked) -> {
                prefs.edit().putBoolean("show_profile", isChecked).apply();
                Toast.makeText(this, isChecked ? "เปิดแสดงโปรไฟล์" : "ปิดแสดงโปรไฟล์", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchSearchPhone != null) {
            switchSearchPhone.setOnCheckedChangeListener((btn, isChecked) -> {
                prefs.edit().putBoolean("search_phone", isChecked).apply();
                Toast.makeText(this, isChecked ? "อนุญาตการค้นหาด้วยเบอร์โทรศัพท์" : "ปิดการค้นหาด้วยเบอร์โทรศัพท์", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchHideAppt != null) {
            switchHideAppt.setOnCheckedChangeListener((btn, isChecked) -> {
                prefs.edit().putBoolean("hide_appt", isChecked).apply();
                Toast.makeText(this, isChecked ? "ซ่อนข้อมูลการนัดหมายแล้ว" : "เปิดแสดงข้อมูลการนัดหมาย", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchAppNotify != null) {
            switchAppNotify.setOnCheckedChangeListener((btn, isChecked) -> {
                prefs.edit().putBoolean("app_notify", isChecked).apply();
                Toast.makeText(this, isChecked ? "เปิดรับการแจ้งเตือน" : "ปิดการแจ้งเตือน", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchEmailNotify != null) {
            switchEmailNotify.setOnCheckedChangeListener((btn, isChecked) -> {
                prefs.edit().putBoolean("email_notify", isChecked).apply();
                Toast.makeText(this, isChecked ? "เปิดการแจ้งเตือนทางอีเมล" : "ปิดการแจ้งเตือนทางอีเมล", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchSmsNotify != null) {
            switchSmsNotify.setOnCheckedChangeListener((btn, isChecked) -> {
                prefs.edit().putBoolean("sms_notify", isChecked).apply();
                Toast.makeText(this, isChecked ? "เปิดการแจ้งเตือนทาง SMS" : "ปิดการแจ้งเตือนทาง SMS", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("ลบข้อมูลบัญชี?")
                .setMessage("คุณแน่ใจหรือว่าต้องการลบข้อมูลบัญชีและประวัติทั้งหมดของคุณออกจากระบบ? การดำเนินการนี้ไม่สามารถย้อนกลับได้")
                .setNegativeButton("ยกเลิก", null)
                .setPositiveButton("ลบข้อมูลบัญชี", (dialog, which) -> {
                    dbManager.logout();
                    Toast.makeText(this, "ลบข้อมูลบัญชีเรียบร้อยแล้ว", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}