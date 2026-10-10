package com.example.myapplicationjt;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.example.myapplicationjt.model.Appointment;
import com.example.myapplicationjt.model.Pet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AppointmentActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;
    private String selectedPetName = "Sania";
    private String selectedPetPhotoUri = "";
    private String selectedHospital = "โรงพยาบาลสัตว์พิษณุโลก";
    private String selectedService = "ตรวจทั่วไป";
    private String selectedDate = "10 ต.ค. 2569";
    private String selectedTime = "10:30";

    private TextView tvPetName;
    private TextView tvHospitalName;
    private TextView tvCurrentMonth;

    private int currentStep = 1;

    private final Calendar calendar = Calendar.getInstance();

    private static final String[] THAI_MONTHS = new String[]{
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
            "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
    };

    private static final String[] THAI_MONTHS_SHORT = new String[]{
            "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);

        // Set default calendar to October 2026
        calendar.set(Calendar.YEAR, 2026);
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);
        calendar.set(Calendar.DAY_OF_MONTH, 10);

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvPetName = findViewById(R.id.tvPetName);
        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);

        // Setup Stepper Bar Click Listeners
        setupStepperClicks();
        updateStepperProgress(1);

        // Load First Pet from Database if available
        String email = dbManager.getCurrentUserEmail();
        List<Pet> pets = dbManager.getPets(email);
        if (pets != null && !pets.isEmpty()) {
            applySelectedPet(pets.get(0));
        } else {
            if (tvPetName != null) tvPetName.setText("ยังไม่มีสัตว์เลี้ยง (กดเลือกเพื่อเพิ่ม)");
            TextView tvPetBreed = findViewById(R.id.tvPetBreed);
            if (tvPetBreed != null) tvPetBreed.setText("แตะที่นี่เพื่อเพิ่มสัตว์เลี้ยงตัวแรกของคุณ");
        }

        // 2. Pet Selection Card Click Listener
        MaterialCardView cardSelectPet = findViewById(R.id.cardSelectPet);
        if (cardSelectPet != null) {
            cardSelectPet.setOnClickListener(v -> showPetSelectionDialog());
        }

        // 3. Hospital Selection Card Click Listener
        MaterialCardView cardSelectHospital = findViewById(R.id.cardSelectHospital);
        if (cardSelectHospital != null) {
            cardSelectHospital.setOnClickListener(v -> showHospitalSelectionDialog());
        }

        // 4. Setup Service Clicks & Highlighting
        setupServiceSelection();

        // 5. Setup Full Dynamic 12-Month Calendar & Date Picker
        setupCalendarSelection();

        // 6. Setup Time Slot Clicks & Highlighting
        setupTimeSelection();

        // 7. Confirm Booking Button
        MaterialButton btnBook = findViewById(R.id.btnBook);
        EditText etNote = findViewById(R.id.etNote);

        if (btnBook != null) {
            btnBook.setOnClickListener(v -> {
                String note = etNote != null ? etNote.getText().toString().trim() : "";

                Appointment appt = new Appointment(
                        UUID.randomUUID().toString(),
                        email,
                        email,
                        selectedPetName,
                        selectedHospital,
                        selectedDate + " (" + selectedService + ")",
                        selectedTime,
                        note.isEmpty() ? "Confirmed" : note
                );
                appt.book();

                dbManager.addAppointment(appt);
                updateStepperProgress(5);

                // Navigate to Success Booking Screen
                Intent successIntent = new Intent(this, AppointmentSuccessActivity.class);
                successIntent.putExtra("petName", selectedPetName);
                successIntent.putExtra("hospitalName", selectedHospital);
                successIntent.putExtra("serviceName", selectedService);
                successIntent.putExtra("date", selectedDate);
                successIntent.putExtra("time", selectedTime);
                successIntent.putExtra("note", note.isEmpty() ? "อาเจียน" : note);
                if (selectedPetPhotoUri != null) {
                    successIntent.putExtra("photoUri", selectedPetPhotoUri);
                }
                startActivity(successIntent);
                finish();
            });
        }
    }

    private void setupStepperClicks() {
        int[] layoutIds = new int[]{
                R.id.step1Layout, R.id.step2Layout, R.id.step3Layout, R.id.step4Layout, R.id.step5Layout
        };

        for (int i = 0; i < layoutIds.length; i++) {
            final int step = i + 1;
            LinearLayout layout = findViewById(layoutIds[i]);
            if (layout != null) {
                layout.setOnClickListener(v -> updateStepperProgress(step));
            }
        }
    }

    private void updateStepperProgress(int activeStep) {
        currentStep = activeStep;
        TextView[] stepNums = new TextView[]{
                findViewById(R.id.tvStep1Num),
                findViewById(R.id.tvStep2Num),
                findViewById(R.id.tvStep3Num),
                findViewById(R.id.tvStep4Num),
                findViewById(R.id.tvStep5Num)
        };
        TextView[] stepLabels = new TextView[]{
                findViewById(R.id.tvStep1Label),
                findViewById(R.id.tvStep2Label),
                findViewById(R.id.tvStep3Label),
                findViewById(R.id.tvStep4Label),
                findViewById(R.id.tvStep5Label)
        };

        for (int i = 0; i < 5; i++) {
            boolean active = (i + 1) <= activeStep;
            if (stepNums[i] != null) {
                if (active) {
                    stepNums[i].setBackgroundResource(R.drawable.circle_blue_active);
                    stepNums[i].setTextColor(0xFFFFFFFF);
                } else {
                    stepNums[i].setBackgroundResource(R.drawable.circle_grey_inactive);
                    stepNums[i].setTextColor(0xFF78909C);
                }
            }
            if (stepLabels[i] != null) {
                if (active) {
                    stepLabels[i].setTextColor(0xFF1E88E5);
                    stepLabels[i].setTypeface(null, Typeface.BOLD);
                } else {
                    stepLabels[i].setTextColor(0xFF78909C);
                    stepLabels[i].setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }

    private void showPetSelectionDialog() {
        String email = dbManager.getCurrentUserEmail();
        List<Pet> pets = dbManager.getPets(email);

        List<String> options = new ArrayList<>();
        if (pets != null) {
            for (Pet p : pets) {
                options.add(p.getPetName() + " (" + (p.getPetBreed() != null && !p.getPetBreed().isEmpty() ? p.getPetBreed() : "สัตว์เลี้ยง") + ")");
            }
        }
        options.add("+ เพิ่มสัตว์เลี้ยงใหม่");

        String[] nameArray = options.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("เลือกสัตว์เลี้ยงที่ต้องการนัดหมาย");
        builder.setItems(nameArray, (dialog, which) -> {
            if (which == nameArray.length - 1) {
                showAddNewPetDialog();
            } else if (pets != null && which < pets.size()) {
                Pet selected = pets.get(which);
                applySelectedPet(selected);
            }
        });
        builder.show();
    }

    private void applySelectedPet(Pet pet) {
        if (pet == null) return;
        selectedPetName = pet.getPetName();
        selectedPetPhotoUri = pet.getPhotoUri() != null ? pet.getPhotoUri() : "";
        if (tvPetName != null) {
            tvPetName.setText(pet.getPetName());
        }
        TextView tvPetBreed = findViewById(R.id.tvPetBreed);
        if (tvPetBreed != null) {
            String breed = pet.getPetBreed() != null && !pet.getPetBreed().isEmpty() ? pet.getPetBreed() : "สัตว์เลี้ยง";
            tvPetBreed.setText(breed);
        }
        ImageView ivPetPhoto = findViewById(R.id.ivPetPhoto);
        if (ivPetPhoto != null && pet.getPhotoUri() != null && !pet.getPhotoUri().isEmpty()) {
            try {
                ivPetPhoto.setImageURI(Uri.parse(pet.getPhotoUri()));
            } catch (Exception ignored) {
            }
        }
        updateStepperProgress(1);
    }

    private void showAddNewPetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("เพิ่มสัตว์เลี้ยงใหม่");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2);

        final EditText inputPetName = new EditText(this);
        inputPetName.setHint("ชื่อสัตว์เลี้ยง (เช่น boy, Milo)");

        final EditText inputPetBreed = new EditText(this);
        inputPetBreed.setHint("ชนิด/สายพันธุ์ (เช่น แมว • cat/55/200)");

        container.addView(inputPetName);
        container.addView(inputPetBreed);

        builder.setView(container);

        builder.setPositiveButton("เพิ่ม", (dialog, which) -> {
            String name = inputPetName.getText().toString().trim();
            String breed = inputPetBreed.getText().toString().trim();

            if (!name.isEmpty()) {
                String email = dbManager.getCurrentUserEmail();
                Pet newPet = new Pet(
                        UUID.randomUUID().toString(),
                        email,
                        "Owner",
                        "088-xxx-xxxx",
                        name,
                        breed.isEmpty() ? "สัตว์เลี้ยง" : breed,
                        ""
                );
                dbManager.addPet(newPet);
                applySelectedPet(newPet);
                Toast.makeText(this, "เพิ่มสัตว์เลี้ยง (" + name + ") เรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "กรุณากรอกชื่อสัตว์เลี้ยง", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showHospitalSelectionDialog() {
        String[] hospitals = new String[]{
                "โรงพยาบาลสัตว์พิษณุโลก",
                "โรงพยาบาลสัตว์มหาวิทยาลัยนเรศวร",
                "โรงพยาบาลสัตว์เอกอารักษ์ พิษณุโลก",
                "คลินิกรักษาสัตว์ แอนิมอลแลนด์ พิษณุโลก"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("เลือกโรงพยาบาลสัตว์");
        builder.setItems(hospitals, (dialog, which) -> {
            selectedHospital = hospitals[which];
            if (tvHospitalName != null) {
                tvHospitalName.setText(selectedHospital);
            }
            updateStepperProgress(2);
            Toast.makeText(this, "เลือกโรงพยาบาล: " + selectedHospital, Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void setupServiceSelection() {
        int[] serviceIds = new int[]{
                R.id.svcCheckup, R.id.svcVaccine, R.id.svcBlood, R.id.svcSurgery, R.id.svcOther
        };
        String[] serviceNames = new String[]{
                "ตรวจทั่วไป", "ฉีดวัคซีน", "ตรวจเลือด", "ผ่าตัด", "อื่นๆ"
        };

        for (int i = 0; i < serviceIds.length; i++) {
            final int index = i;
            LinearLayout layout = findViewById(serviceIds[i]);
            if (layout != null) {
                layout.setOnClickListener(v -> {
                    selectedService = serviceNames[index];
                    updateServiceHighlight(serviceIds, index);
                    updateStepperProgress(3);
                    Toast.makeText(this, "เลือกบริการ: " + selectedService, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void updateServiceHighlight(int[] serviceIds, int selectedIndex) {
        for (int i = 0; i < serviceIds.length; i++) {
            LinearLayout layout = findViewById(serviceIds[i]);
            if (layout != null) {
                if (i == selectedIndex) {
                    layout.setBackgroundColor(0xFFE3F2FD);
                    if (layout.getChildCount() > 1 && layout.getChildAt(1) instanceof TextView) {
                        TextView tv = (TextView) layout.getChildAt(1);
                        tv.setTextColor(0xFF1E88E5);
                        tv.setTypeface(null, Typeface.BOLD);
                    }
                    if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof ImageView) {
                        ImageView iv = (ImageView) layout.getChildAt(0);
                        iv.setColorFilter(0xFF1E88E5);
                    }
                } else {
                    layout.setBackgroundColor(0xFFF5F7FA);
                    if (layout.getChildCount() > 1 && layout.getChildAt(1) instanceof TextView) {
                        TextView tv = (TextView) layout.getChildAt(1);
                        tv.setTextColor(0xFF37474F);
                        tv.setTypeface(null, Typeface.NORMAL);
                    }
                    if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof ImageView) {
                        ImageView iv = (ImageView) layout.getChildAt(0);
                        iv.setColorFilter(0xFF546E7A);
                    }
                }
            }
        }
    }

    private void setupCalendarSelection() {
        ImageView btnPrevMonth = findViewById(R.id.btnPrevMonth);
        ImageView btnNextMonth = findViewById(R.id.btnNextMonth);

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, -1);
                renderDynamicCalendar();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, 1);
                renderDynamicCalendar();
            });
        }

        renderDynamicCalendar();
    }

    private void renderDynamicCalendar() {
        int yearBE = calendar.get(Calendar.YEAR) + 543;
        int monthIdx = calendar.get(Calendar.MONTH);
        String monthYearText = THAI_MONTHS[monthIdx] + " " + yearBE;

        if (tvCurrentMonth != null) {
            tvCurrentMonth.setText(monthYearText);
        }

        GridLayout gridCalendar = findViewById(R.id.gridCalendar);
        if (gridCalendar == null) return;

        gridCalendar.removeAllViews();

        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK); // 1=Sun, 2=Mon, ... 7=Sat
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Previous Month Trailing Days
        Calendar prevCal = (Calendar) tempCal.clone();
        prevCal.add(Calendar.MONTH, -1);
        int prevDaysInMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int startPrevDay = prevDaysInMonth - (firstDayOfWeek - 2);

        // 1. Fill trailing days from previous month
        for (int day = startPrevDay; day <= prevDaysInMonth; day++) {
            TextView tv = createDayTextView(String.valueOf(day), 0xFFB0BEC5, false);
            gridCalendar.addView(tv);
        }

        // 2. Fill days of current month
        int selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInMonth; day++) {
            boolean isSelected = (day == selectedDay);
            TextView tv = createDayTextView(
                    String.valueOf(day),
                    isSelected ? 0xFF1E88E5 : 0xFF37474F,
                    true
            );
            if (isSelected) {
                tv.setBackgroundColor(0xFFE3F2FD);
                tv.setTypeface(null, Typeface.BOLD);
            }

            final int currentDay = day;
            tv.setOnClickListener(v -> {
                calendar.set(Calendar.DAY_OF_MONTH, currentDay);
                selectedDate = currentDay + " " + THAI_MONTHS_SHORT[monthIdx] + " " + yearBE;
                updateStepperProgress(4);
                renderDynamicCalendar();
                Toast.makeText(this, "เลือกวันที่: " + selectedDate, Toast.LENGTH_SHORT).show();
            });

            gridCalendar.addView(tv);
        }

        // 3. Fill leading days for next month to complete 35 or 42 grid cells
        int currentChildCount = gridCalendar.getChildCount();
        int targetTotal = currentChildCount > 35 ? 42 : 35;
        int nextDay = 1;
        while (gridCalendar.getChildCount() < targetTotal) {
            TextView tv = createDayTextView(String.valueOf(nextDay++), 0xFFB0BEC5, false);
            gridCalendar.addView(tv);
        }
    }

    private TextView createDayTextView(String text, int textColor, boolean isCurrentMonth) {
        TextView tv = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = (int) (32 * getResources().getDisplayMetrics().density);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        tv.setLayoutParams(params);
        tv.setGravity(Gravity.CENTER);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setTextSize(12f);
        if (!isCurrentMonth) {
            tv.setAlpha(0.5f);
        }
        return tv;
    }

    private void setupTimeSelection() {
        int[] timeIds = new int[]{
                R.id.time900, R.id.time930, R.id.time1000, R.id.time1030,
                R.id.time1100, R.id.time1300, R.id.time1400, R.id.time1500
        };

        for (int i = 0; i < timeIds.length; i++) {
            final int index = i;
            TextView tv = findViewById(timeIds[i]);
            if (tv != null) {
                tv.setOnClickListener(v -> {
                    selectedTime = tv.getText().toString();
                    updateTimeHighlight(timeIds, index);
                    updateStepperProgress(5);
                    Toast.makeText(this, "เลือกเวลานัดหมาย: " + selectedTime + " น.", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void updateTimeHighlight(int[] timeIds, int selectedIndex) {
        for (int i = 0; i < timeIds.length; i++) {
            TextView tv = findViewById(timeIds[i]);
            if (tv != null) {
                if (i == selectedIndex) {
                    tv.setBackgroundColor(0xFFE3F2FD);
                    tv.setTextColor(0xFF1E88E5);
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    tv.setBackgroundColor(0xFFFFFFFF);
                    tv.setTextColor(0xFF1E88E5);
                    tv.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    }
}