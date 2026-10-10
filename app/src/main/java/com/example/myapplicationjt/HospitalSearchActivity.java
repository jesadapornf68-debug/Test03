package com.example.myapplicationjt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.model.Hospital;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HospitalSearchActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private final List<Hospital> hospitalList = new ArrayList<>();

    // Selected filter values
    private String selectedPet = "สุนัข";
    private String selectedService = "ตรวจทั่วไป";
    private String selectedDistance = "ใกล้ที่สุด";
    private String searchQuery = "";

    private MaterialCardView cardA, cardB, cardC;
    private TextView tvNoHospitalFound;
    private EditText etSearchHospital;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchUserLocation();
                } else {
                    Toast.makeText(this, "ไม่ได้เปิด GPS แสดงพิกัดเริ่มต้น", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hospital_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        cardA = findViewById(R.id.cardHospitalA);
        cardB = findViewById(R.id.cardHospitalB);
        cardC = findViewById(R.id.cardHospitalC);
        tvNoHospitalFound = findViewById(R.id.tvNoHospitalFound);
        etSearchHospital = findViewById(R.id.etSearchHospital);

        // Initialize Hospital Models (Phitsanulok, Thailand)
        hospitalList.add(new Hospital("h1", "โรงพยาบาลสัตว์พิษณุโลก", "อ.เมือง จ.พิษณุโลก", "055-258123", 16.8260, 100.2600));
        hospitalList.add(new Hospital("h2", "โรงพยาบาลสัตว์มหาวิทยาลัยนเรศวร", "ม.นเรศวร จ.พิษณุโลก", "055-965000", 16.7320, 100.1930));
        hospitalList.add(new Hospital("h3", "โรงพยาบาลสัตว์เอกอารักษ์ พิษณุโลก", "อ.เมือง จ.พิษณุโลก", "055-303333", 16.8150, 100.2700));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        // 2. Hospital Cards Click Listeners
        if (cardA != null) {
            cardA.setOnClickListener(v -> openGoogleMapsDirections("โรงพยาบาลสัตว์พิษณุโลก", 16.8260, 100.2600));
        }
        if (cardB != null) {
            cardB.setOnClickListener(v -> openGoogleMapsDirections("โรงพยาบาลสัตว์มหาวิทยาลัยนเรศวร", 16.7320, 100.1930));
        }
        if (cardC != null) {
            cardC.setOnClickListener(v -> openGoogleMapsDirections("โรงพยาบาลสัตว์เอกอารักษ์ พิษณุโลก", 16.8150, 100.2700));
        }

        // 3. View All Map Button
        MaterialButton btnViewAllMap = findViewById(R.id.btnViewAllMap);
        if (btnViewAllMap != null) {
            btnViewAllMap.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("geo:16.8211,100.2659?q=โรงพยาบาลสัตว์+พิษณุโลก");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Uri webUri = Uri.parse("https://www.google.com/maps/search/โรงพยาบาลสัตว์+พิษณุโลก/@16.8211,100.2659,13z");
                    startActivity(new Intent(Intent.ACTION_VIEW, webUri));
                }
            });
        }

        // 4. Setup Search TextWatcher
        if (etSearchHospital != null) {
            etSearchHospital.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString().trim().toLowerCase();
                    applyFilters();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // 5. Setup Filter Chip Clicks & Interactions
        setupFilterChips();

        // 6. Bottom Navigation Listeners
        LinearLayout navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }

        LinearLayout navHospital = findViewById(R.id.navHospital);
        if (navHospital != null) {
            navHospital.setOnClickListener(v -> Toast.makeText(this, "คุณอยู่ที่หน้าค้นหาโรงพยาบาลแล้ว", Toast.LENGTH_SHORT).show());
        }

        LinearLayout navAppointment = findViewById(R.id.navAppointment);
        if (navAppointment != null) {
            navAppointment.setOnClickListener(v -> {
                startActivity(new Intent(this, AppointmentActivity.class));
                finish();
            });
        }

        LinearLayout navOwner = findViewById(R.id.navOwner);
        if (navOwner != null) {
            navOwner.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            });
        }

        LinearLayout navMore = findViewById(R.id.navMore);
        if (navMore != null) {
            navMore.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            });
        }
    }

    private void setupFilterChips() {
        // Pet Type Chips
        int[] petIds = new int[]{R.id.chipDog, R.id.chipCat, R.id.chipRabbit, R.id.chipBird};
        String[] petNames = new String[]{"สุนัข", "แมว", "กระต่าย", "นก"};

        for (int i = 0; i < petIds.length; i++) {
            final int index = i;
            TextView tv = findViewById(petIds[i]);
            if (tv != null) {
                tv.setOnClickListener(v -> {
                    selectedPet = petNames[index];
                    updatePetChipStyles(petIds, index);
                    applyFilters();
                });
            }
        }

        // Service Chips
        int[] serviceIds = new int[]{R.id.chipCheckup, R.id.chipVaccine, R.id.chipSurgery, R.id.chipXray, R.id.chipBlood, R.id.chipMoreServices};
        String[] serviceNames = new String[]{"ตรวจทั่วไป", "ฉีดวัคซีน", "ผ่าตัด", "X-ray", "ตรวจเลือด", "เพิ่มเติม ˅"};

        for (int i = 0; i < serviceIds.length; i++) {
            final int index = i;
            TextView tv = findViewById(serviceIds[i]);
            if (tv != null) {
                tv.setOnClickListener(v -> {
                    selectedService = serviceNames[index];
                    updateServiceChipStyles(serviceIds, index);
                    applyFilters();
                });
            }
        }

        // Distance Chips
        int[] distIds = new int[]{R.id.chipNearest, R.id.chip5km, R.id.chip10km, R.id.chip20km, R.id.chip15km};
        String[] distNames = new String[]{"ใกล้ที่สุด", "5 กม.", "10 กม.", "20 กม.", "30 กม."};

        for (int i = 0; i < distIds.length; i++) {
            final int index = i;
            TextView tv = findViewById(distIds[i]);
            if (tv != null) {
                tv.setOnClickListener(v -> {
                    selectedDistance = distNames[index];
                    updateDistanceChipStyles(distIds, index);
                    applyFilters();
                });
            }
        }
    }

    private void updatePetChipStyles(int[] chipIds, int selectedIndex) {
        for (int i = 0; i < chipIds.length; i++) {
            TextView tv = findViewById(chipIds[i]);
            if (tv != null) {
                if (i == selectedIndex) {
                    tv.setBackgroundColor(0xFF1E88E5);
                    tv.setTextColor(0xFFFFFFFF);
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    tv.setBackgroundColor(0xFFF5F7FA);
                    tv.setTextColor(0xFF37474F);
                    tv.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }

    private void updateServiceChipStyles(int[] chipIds, int selectedIndex) {
        for (int i = 0; i < chipIds.length; i++) {
            TextView tv = findViewById(chipIds[i]);
            if (tv != null) {
                if (i == selectedIndex) {
                    tv.setBackgroundColor(0xFFE3F2FD);
                    tv.setTextColor(0xFF1E88E5);
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    tv.setBackgroundColor(0xFFF5F7FA);
                    tv.setTextColor(0xFF37474F);
                    tv.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }

    private void updateDistanceChipStyles(int[] chipIds, int selectedIndex) {
        for (int i = 0; i < chipIds.length; i++) {
            TextView tv = findViewById(chipIds[i]);
            if (tv != null) {
                if (i == selectedIndex) {
                    tv.setBackgroundColor(0xFF1E88E5);
                    tv.setTextColor(0xFFFFFFFF);
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    tv.setBackgroundColor(0xFFF5F7FA);
                    tv.setTextColor(0xFF37474F);
                    tv.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }

    private void applyFilters() {
        boolean matchA = true;
        boolean matchB = true;
        boolean matchC = true;

        // 1. Search Query Match
        if (!searchQuery.isEmpty()) {
            matchA = "โรงพยาบาลสัตว์พิษณุโลก".toLowerCase().contains(searchQuery);
            matchB = "โรงพยาบาลสัตว์มหาวิทยาลัยนเรศวร".toLowerCase().contains(searchQuery);
            matchC = "โรงพยาบาลสัตว์เอกอารักษ์ พิษณุโลก".toLowerCase().contains(searchQuery);
        }

        // 2. Pet Filter Match
        if (selectedPet.equals("นก")) {
            matchA = false;
            matchC = false; // Only Naresuan University supports Birds in model
        } else if (selectedPet.equals("กระต่าย")) {
            matchC = false;
        }

        // 3. Service Filter Match
        if (selectedService.equals("ผ่าตัด") || selectedService.equals("X-ray")) {
            matchC = false; // Hospital A and B have full surgery & X-ray facilities
        }

        if (cardA != null) cardA.setVisibility(matchA ? View.VISIBLE : View.GONE);
        if (cardB != null) cardB.setVisibility(matchB ? View.VISIBLE : View.GONE);
        if (cardC != null) cardC.setVisibility(matchC ? View.VISIBLE : View.GONE);

        int visibleCount = (matchA ? 1 : 0) + (matchB ? 1 : 0) + (matchC ? 1 : 0);
        if (tvNoHospitalFound != null) {
            tvNoHospitalFound.setVisibility(visibleCount == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchUserLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && location.getLongitude() > 90.0) {
                    updateDistances(location.getLatitude(), location.getLongitude());
                } else {
                    updateDistances(16.8211, 100.2659);
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
            updateDistances(16.8211, 100.2659);
        }
    }

    private void updateDistances(double lat, double lon) {
        int[] viewIds = new int[]{R.id.tvDistA, R.id.tvDistB, R.id.tvDistC};

        for (int i = 0; i < hospitalList.size() && i < viewIds.length; i++) {
            Hospital hosp = hospitalList.get(i);
            float[] results = new float[1];
            Location.distanceBetween(lat, lon, hosp.getLatitude(), hosp.getLongitude(), results);
            double distanceKm = results[0] / 1000.0;

            TextView tv = findViewById(viewIds[i]);
            if (tv != null) {
                tv.setText(String.format(Locale.getDefault(), "📍 %.1f กม.", distanceKm));
            }
        }
    }

    private void openGoogleMapsDirections(String hospitalName, double lat, double lon) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon + " (" + hospitalName + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }
}