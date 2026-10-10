package com.example.myapplicationjt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.example.myapplicationjt.model.Pet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PetTrackingActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private PetCareDBManager dbManager;

    private String selectedPetName = "NongJ";
    private TextView tvPetNameTop;
    private TextView tvPetNameStatus;
    private TextView tvLastUpdated;
    private TextView tvDistanceBadge;

    private final ActivityResultLauncher<String[]> requestLocationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                boolean granted = (fine != null && fine) || (coarse != null && coarse);

                if (granted) {
                    fetchLocation();
                } else {
                    Toast.makeText(this, "ใช้ตำแหน่งเริ่มต้น (พิษณุโลก)", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pet_tracking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvPetNameTop = findViewById(R.id.tvPetNameTop);
        tvPetNameStatus = findViewById(R.id.tvPetNameStatus);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        tvDistanceBadge = findViewById(R.id.tvDistanceBadge);

        // Load First Pet Name from DB if available
        String email = dbManager.getCurrentUserEmail();
        List<Pet> pets = dbManager.getPets(email);
        if (pets != null && !pets.isEmpty()) {
            selectedPetName = pets.get(0).getPetName();
            if (tvPetNameTop != null) tvPetNameTop.setText(selectedPetName);
            if (tvPetNameStatus != null) tvPetNameStatus.setText(selectedPetName);
        }

        // 2. Select Pet Dropdown
        MaterialCardView cardSelectPet = findViewById(R.id.cardSelectPet);
        if (cardSelectPet != null) {
            cardSelectPet.setOnClickListener(v -> showPetSelectionDialog(pets));
        }

        // 3. Recenter Map Target Button
        MaterialCardView btnRecenterMap = findViewById(R.id.btnRecenterMap);
        if (btnRecenterMap != null) {
            btnRecenterMap.setOnClickListener(v -> {
                Toast.makeText(this, "ซูมตำแหน่งของ " + selectedPetName + " (พิษณุโลก)", Toast.LENGTH_SHORT).show();
            });
        }

        // 4. Refresh Button
        ImageView btnRefreshStatus = findViewById(R.id.btnRefreshStatus);
        if (btnRefreshStatus != null) {
            btnRefreshStatus.setOnClickListener(v -> refreshTrackingStatus());
        }

        // 5. Geofence Alert Switch
        SwitchMaterial switchGeofence = findViewById(R.id.switchGeofence);
        if (switchGeofence != null) {
            switchGeofence.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Toast.makeText(this, "เปิดการแจ้งเตือนเมื่อออกนอกพื้นที่ 500 เมตรแล้ว", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ปิดการแจ้งเตือนออกนอกพื้นที่", Toast.LENGTH_SHORT).show();
                }
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
    }

    private void showPetSelectionDialog(List<Pet> pets) {
        List<String> petNames = new ArrayList<>();
        if (pets != null && !pets.isEmpty()) {
            for (Pet p : pets) {
                petNames.add(p.getPetName());
            }
        } else {
            petNames.add("NongJ");
            petNames.add("Sania");
            petNames.add("Milo");
        }

        String[] nameArray = petNames.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("เลือกสัตว์เลี้ยงที่ต้องการติดตาม");
        builder.setItems(nameArray, (dialog, which) -> {
            selectedPetName = nameArray[which];
            if (tvPetNameTop != null) tvPetNameTop.setText(selectedPetName);
            if (tvPetNameStatus != null) tvPetNameStatus.setText(selectedPetName);
            Toast.makeText(this, "กำลังติดตาม: " + selectedPetName, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void refreshTrackingStatus() {
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        if (tvLastUpdated != null) {
            tvLastUpdated.setText("อัปเดตล่าสุด " + currentTime + " น.");
        }
        Toast.makeText(this, "อัปเดตตำแหน่งล่าสุดเรียบร้อยแล้ว (" + currentTime + " น.)", Toast.LENGTH_SHORT).show();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            requestLocationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void fetchLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                refreshTrackingStatus();
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}