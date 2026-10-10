package com.example.myapplicationjt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.example.myapplicationjt.model.Appointment;
import com.example.myapplicationjt.model.Pet;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;
    private TextView tvSavedAddress;
    private TextView tvUserName;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);
        currentUserEmail = dbManager.getCurrentUserEmail();

        tvSavedAddress = findViewById(R.id.tvSavedAddress);
        tvUserName = findViewById(R.id.tvUserName);

        // Load Header User Name
        loadUserName();

        // Load Home Address
        loadHomeAddress();

        // Edit Home Address Listener
        TextView btnEditAddress = findViewById(R.id.btnEditAddress);
        if (btnEditAddress != null) {
            btnEditAddress.setOnClickListener(v -> showEditAddressDialog());
        }

        // Notification Button
        MaterialCardView btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> Toast.makeText(this, "ไม่มีการแจ้งเตือนใหม่", Toast.LENGTH_SHORT).show());
        }

        // Exit App / Logout Button
        MaterialCardView btnExitApp = findViewById(R.id.btnExitApp);
        if (btnExitApp != null) {
            btnExitApp.setOnClickListener(v -> showExitDialog());
        }

        // Register OnBackPressedCallback to handle device back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });

        // Pet Info Card
        MaterialCardView cardPetInfo = findViewById(R.id.cardPetInfo);
        if (cardPetInfo != null) {
            cardPetInfo.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        // Next Appointment Card & Details
        loadLatestAppointment();

        // Vaccine Due Detail Button & Card
        TextView btnVaccineDetail = findViewById(R.id.btnVaccineDetail);
        if (btnVaccineDetail != null) {
            btnVaccineDetail.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));
        }
        MaterialCardView cardVaccineDue = findViewById(R.id.cardVaccineDue);
        if (cardVaccineDue != null) {
            cardVaccineDue.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));
        }

        // Quick Menu Cards
        MaterialCardView cardHospital = findViewById(R.id.cardHospital);
        if (cardHospital != null) {
            cardHospital.setOnClickListener(v -> startActivity(new Intent(this, HospitalSearchActivity.class)));
        }

        MaterialCardView cardAppointment = findViewById(R.id.cardAppointment);
        if (cardAppointment != null) {
            cardAppointment.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));
        }

        MaterialCardView cardPetInfoMenu = findViewById(R.id.cardPetInfoMenu);
        if (cardPetInfoMenu != null) {
            cardPetInfoMenu.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        MaterialCardView cardSettings = findViewById(R.id.cardSettings);
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        MaterialCardView cardPetTracking = findViewById(R.id.cardPetTracking);
        if (cardPetTracking != null) {
            cardPetTracking.setOnClickListener(v -> startActivity(new Intent(this, PetTrackingActivity.class)));
        }

        // Bottom Navigation Listeners
        LinearLayout navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> Toast.makeText(this, "คุณอยู่ที่หน้าหลักแล้ว", Toast.LENGTH_SHORT).show());
        }

        LinearLayout navHospital = findViewById(R.id.navHospital);
        if (navHospital != null) {
            navHospital.setOnClickListener(v -> startActivity(new Intent(this, HospitalSearchActivity.class)));
        }

        LinearLayout navAppointment = findViewById(R.id.navAppointment);
        if (navAppointment != null) {
            navAppointment.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));
        }

        LinearLayout navOwner = findViewById(R.id.navOwner);
        if (navOwner != null) {
            navOwner.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        LinearLayout navMore = findViewById(R.id.navMore);
        if (navMore != null) {
            navMore.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUserEmail = dbManager.getCurrentUserEmail();
        loadUserName();
        loadHomeAddress();
        loadPetProfile();
        loadLatestAppointment();
    }

    private void loadLatestAppointment() {
        TextView tvApptDateTime = findViewById(R.id.tvApptDateTime);
        TextView tvApptHospital = findViewById(R.id.tvApptHospital);
        TextView btnApptDetail = findViewById(R.id.btnApptDetail);
        MaterialCardView cardNextAppointment = findViewById(R.id.cardNextAppointment);

        TextView tvVaccineName = findViewById(R.id.tvVaccineName);
        TextView tvVaccineDate = findViewById(R.id.tvVaccineDate);
        TextView btnVaccineDetail = findViewById(R.id.btnVaccineDetail);
        MaterialCardView cardVaccineDue = findViewById(R.id.cardVaccineDue);

        List<Appointment> appts = dbManager.getAppointments(currentUserEmail);
        if (appts != null && !appts.isEmpty()) {
            final Appointment latest = appts.get(appts.size() - 1);

            if (tvApptDateTime != null) {
                tvApptDateTime.setText("🗓 " + latest.getDate() + "   " + latest.getTime() + " น.");
            }
            if (tvApptHospital != null) {
                tvApptHospital.setText("📍 " + latest.getHospitalName());
            }
            if (btnApptDetail != null) {
                btnApptDetail.setText("ดูรายละเอียด");
            }

            if (tvVaccineName != null) {
                tvVaccineName.setText("ฉีดวัคซีน / " + latest.getPetName());
            }
            if (tvVaccineDate != null) {
                tvVaccineDate.setText(latest.getDate());
            }
            if (btnVaccineDetail != null) {
                btnVaccineDetail.setText("ดูรายละเอียด");
            }

            View.OnClickListener openDetailsListener = v -> {
                Intent intent = new Intent(this, AppointmentSuccessActivity.class);
                intent.putExtra("petName", latest.getPetName());
                intent.putExtra("hospitalName", latest.getHospitalName());
                intent.putExtra("date", latest.getDate());
                intent.putExtra("time", latest.getTime());
                startActivity(intent);
            };

            if (btnApptDetail != null) btnApptDetail.setOnClickListener(openDetailsListener);
            if (cardNextAppointment != null) cardNextAppointment.setOnClickListener(openDetailsListener);
            if (btnVaccineDetail != null) btnVaccineDetail.setOnClickListener(openDetailsListener);
            if (cardVaccineDue != null) cardVaccineDue.setOnClickListener(openDetailsListener);

        } else {
            if (tvApptDateTime != null) {
                tvApptDateTime.setText("ยังไม่มีนัดหมาย");
            }
            if (tvApptHospital != null) {
                tvApptHospital.setText("แตะที่นี่เพื่อทำการนัดหมายล่วงหน้า");
            }
            if (btnApptDetail != null) {
                btnApptDetail.setText("นัดหมาย");
            }

            if (tvVaccineName != null) {
                tvVaccineName.setText("ยังไม่มีกำหนดฉีดวัคซีน");
            }
            if (tvVaccineDate != null) {
                tvVaccineDate.setText("นัดหมายฉีดวัคซีนได้ที่ปุ่มนัดหมาย");
            }
            if (btnVaccineDetail != null) {
                btnVaccineDetail.setText("นัดหมาย");
            }

            View.OnClickListener openBookListener = v -> startActivity(new Intent(this, AppointmentActivity.class));
            if (btnApptDetail != null) btnApptDetail.setOnClickListener(openBookListener);
            if (cardNextAppointment != null) cardNextAppointment.setOnClickListener(openBookListener);
            if (btnVaccineDetail != null) btnVaccineDetail.setOnClickListener(openBookListener);
            if (cardVaccineDue != null) cardVaccineDue.setOnClickListener(openBookListener);
        }
    }

    private void loadUserName() {
        if (tvUserName == null) return;

        String displayName = "user";

        // 1. Get user name from SQLite Database by email
        if (currentUserEmail != null && !currentUserEmail.equalsIgnoreCase("guest")) {
            String dbName = dbManager.getUserName(currentUserEmail);
            if (dbName != null && !dbName.trim().isEmpty()) {
                displayName = dbName.trim();
            } else if (currentUserEmail.contains("@")) {
                displayName = currentUserEmail.split("@")[0];
            }
        }

        // 2. Check profile_name from UserProfilePrefs if name is default
        if (displayName.equalsIgnoreCase("user") || displayName.isEmpty()) {
            SharedPreferences profilePrefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
            String profileName = profilePrefs.getString("profile_name", "");
            if (profileName != null && !profileName.trim().isEmpty()) {
                displayName = profileName.trim();
            }
        }

        tvUserName.setText("คุณ " + displayName);
    }

    private void loadHomeAddress() {
        String savedAddress = dbManager.getHomeAddress(currentUserEmail);
        if (savedAddress != null && !savedAddress.trim().isEmpty() && !savedAddress.equals("ยังไม่ได้ระบุที่อยู่")) {
            tvSavedAddress.setText(savedAddress);
        } else {
            tvSavedAddress.setText("ยังไม่ได้ระบุที่อยู่บ้าน (กดแก้ไขเพื่อระบุ)");
        }
    }

    private void loadPetProfile() {
        TextView tvPetName = findViewById(R.id.tvPetName);
        TextView tvPetGender = findViewById(R.id.tvPetGender);
        TextView tvPetBreedAge = findViewById(R.id.tvPetBreedAge);
        TextView tvPetWeight = findViewById(R.id.tvPetWeight);
        TextView tvPetStatus = findViewById(R.id.tvPetStatus);

        List<Pet> pets = dbManager.getPets(currentUserEmail);
        if (pets != null && !pets.isEmpty()) {
            Pet firstPet = pets.get(0);

            if (tvPetName != null) {
                tvPetName.setText(firstPet.getPetName());
            }
            if (tvPetGender != null) {
                tvPetGender.setText("♂");
            }
            if (tvPetBreedAge != null) {
                String breed = firstPet.getPetBreed() != null && !firstPet.getPetBreed().isEmpty()
                        ? firstPet.getPetBreed() : "สัตว์เลี้ยง";
                tvPetBreedAge.setText(breed);
            }
            if (tvPetWeight != null) {
                tvPetWeight.setText("น้ำหนัก 5.5 กก.");
            }
            if (tvPetStatus != null) {
                tvPetStatus.setText("✚ สุขภาพปกติ");
            }
        } else {
            if (tvPetName != null) {
                tvPetName.setText("ยังไม่มีสัตว์เลี้ยง");
            }
            if (tvPetGender != null) {
                tvPetGender.setText("");
            }
            if (tvPetBreedAge != null) {
                tvPetBreedAge.setText("แตะที่นี่เพื่อเพิ่มสัตว์เลี้ยงตัวแรกของคุณ");
            }
            if (tvPetWeight != null) {
                tvPetWeight.setText("");
            }
            if (tvPetStatus != null) {
                tvPetStatus.setText("+ เพิ่มสัตว์เลี้ยง");
            }
        }
    }

    private void showEditAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("แก้ไขที่อยู่บ้าน");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("กรอกที่อยู่บ้านของคุณที่นี่...");
        String currentAddress = tvSavedAddress.getText().toString();
        input.setText(currentAddress);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("บันทึก", (dialog, which) -> {
            String newAddress = input.getText().toString().trim();
            if (!newAddress.isEmpty()) {
                dbManager.saveHomeAddress(currentUserEmail, newAddress);
                tvSavedAddress.setText(newAddress);
                Toast.makeText(this, "บันทึกที่อยู่บ้านสำเร็จ!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "กรุณากรอกที่อยู่บ้าน", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ออกจากแอปพลิเคชัน / ออกจากระบบ");
        builder.setMessage("คุณต้องการปิดแอปพลิเคชัน หรือ ออกจากระบบ?");

        builder.setPositiveButton("ปิดแอปพลิเคชัน", (dialog, which) -> finishAffinity());

        builder.setNeutralButton("ออกจากระบบ", (dialog, which) -> {
            dbManager.logout();
            Toast.makeText(this, "ออกจากระบบเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        builder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}