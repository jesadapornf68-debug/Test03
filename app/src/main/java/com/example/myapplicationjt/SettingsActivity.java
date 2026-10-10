package com.example.myapplicationjt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.topToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        dbManager = PetCareDBManager.getInstance(this);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> startActivity(new Intent(this, LogoutActivity.class)));
        }

        MaterialButton btnViewRecords = findViewById(R.id.btnViewRecords);
        if (btnViewRecords != null) {
            btnViewRecords.setOnClickListener(v -> {
                startActivity(new Intent(this, DataRecordsActivity.class));
            });
        }

        MaterialButton btnBackup = findViewById(R.id.btnBackup);
        if (btnBackup != null) {
            btnBackup.setText("📊 ตรวจสอบฐานข้อมูล SQLite (OOP)");
            btnBackup.setOnClickListener(v -> showDatabaseStats());
        }

        MaterialButton btnRestore = findViewById(R.id.btnRestore);
        if (btnRestore != null) {
            btnRestore.setText("ℹ️ เกี่ยวกับระบบ OOP Architecture");
            btnRestore.setOnClickListener(v -> showOOPInfoDialog());
        }

        MaterialButton btnBrowseFiles = findViewById(R.id.btnBrowseFiles);
        if (btnBrowseFiles != null) {
            btnBrowseFiles.setOnClickListener(v -> {
                String email = dbManager.getCurrentUserEmail();
                Toast.makeText(this, "ผู้ใช้ปัจจุบัน: " + email, Toast.LENGTH_SHORT).show();
            });
        }

        MaterialButton btnResetData = findViewById(R.id.btnResetData);
        if (btnResetData != null) {
            btnResetData.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("รีเซ็ตข้อมูลทั้งหมด?")
                        .setMessage("ข้อมูลที่บันทึกไว้ในฐานข้อมูลทั้งหมด (ที่อยู่, สัตว์เลี้ยง, ประวัติการติดตาม, การนัดหมาย) จะถูกลบทิ้ง")
                        .setNegativeButton("ยกเลิก", null)
                        .setPositiveButton("ยืนยันรีเซ็ต", (dialog, which) -> {
                            dbManager.resetAllData();
                            Toast.makeText(this, "รีเซ็ตข้อมูลทั้งหมดเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            });
        }
    }

    private void showDatabaseStats() {
        int usersCount = dbManager.getAllUsers().size();
        int petsCount = dbManager.getPets("").size();
        int apptsCount = dbManager.getAppointments("").size();
        int trackingCount = dbManager.getTrackingRecords("").size();

        String stats = "🗄️ ฐานข้อมูล SQLite ในเครื่อง (Local DB):\n\n" +
                "• ตาราง Users: " + usersCount + " บัญชี\n" +
                "• ตาราง Pets: " + petsCount + " ตัว\n" +
                "• ตาราง Appointments: " + apptsCount + " นัดหมาย\n" +
                "• ตาราง Pet Tracking: " + trackingCount + " ประวัติพิกัด\n\n" +
                "บันทึกในเครื่องอัตโนมัติด้วยโครงสร้าง OOP";

        new AlertDialog.Builder(this)
                .setTitle("สถิติฐานข้อมูล Localhost / Local DB")
                .setMessage(stats)
                .setPositiveButton("ตกลง", null)
                .show();
    }

    private void showOOPInfoDialog() {
        String info = "โครงสร้าง Object-Oriented Programming (OOP):\n\n" +
                "1. Abstraction: AbstractPet\n" +
                "2. Inheritance: Dog & Cat extends AbstractPet\n" +
                "3. Polymorphism: getInfo(), getSpecies()\n" +
                "4. Encapsulation: User, Pet, Appointment, PetTrackingRecord\n" +
                "5. Design Pattern: Singleton (PetCareDBManager) & DAO Pattern (UserDAO, PetDAO, AppointmentDAO, TrackingDAO)";

        new AlertDialog.Builder(this)
                .setTitle("OOP Architecture")
                .setMessage(info)
                .setPositiveButton("ตกลง", null)
                .show();
    }
}
