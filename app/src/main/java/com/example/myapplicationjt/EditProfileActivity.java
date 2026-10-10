package com.example.myapplicationjt;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;
    private SharedPreferences prefs;
    private String currentUserEmail;

    private ImageView ivOwnerAvatar;
    private TextView tvOwnerName;
    private TextView tvOwnerPhone;
    private TextView tvOwnerEmail;

    private EditText etEditName;
    private EditText etEditEmail;
    private EditText etEditPhone;
    private EditText etEditDob;

    private EditText etHouseNo;
    private EditText etMoo;
    private EditText etRoad;
    private EditText etSubDistrict;
    private EditText etDistrict;
    private EditText etProvince;

    private Uri selectedAvatarUri = null;

    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    selectedAvatarUri = uri;
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }
                    if (ivOwnerAvatar != null) {
                        ivOwnerAvatar.setImageURI(uri);
                    }
                    Toast.makeText(this, "เปลี่ยนรูปโปรไฟล์เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);
        prefs = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE);
        currentUserEmail = dbManager.getCurrentUserEmail();

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ivOwnerAvatar = findViewById(R.id.ivOwnerAvatar);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerPhone = findViewById(R.id.tvOwnerPhone);
        tvOwnerEmail = findViewById(R.id.tvOwnerEmail);

        etEditName = findViewById(R.id.etEditName);
        etEditEmail = findViewById(R.id.etEditEmail);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditDob = findViewById(R.id.etEditDob);

        etHouseNo = findViewById(R.id.etHouseNo);
        etMoo = findViewById(R.id.etMoo);
        etRoad = findViewById(R.id.etRoad);
        etSubDistrict = findViewById(R.id.etSubDistrict);
        etDistrict = findViewById(R.id.etDistrict);
        etProvince = findViewById(R.id.etProvince);

        // Date Picker Listener for DOB
        if (etEditDob != null) {
            etEditDob.setFocusable(false);
            etEditDob.setClickable(true);
            etEditDob.setOnClickListener(v -> showDatePickerDialog());
        }

        // 2. Change Avatar Image Button
        MaterialCardView btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        }

        // 3. Load Existing Profile Data
        loadExistingData();

        // 4. Save Changes Button
        MaterialButton btnSaveChange = findViewById(R.id.btnSaveChange);
        if (btnSaveChange != null) {
            btnSaveChange.setOnClickListener(v -> saveChanges());
        }
    }

    private static final String[] THAI_MONTHS_SHORT = new String[]{
            "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    };

    private void showDatePickerDialog() {
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    int yearBE = selectedYear + 543;
                    String formattedDate = selectedDay + " " + THAI_MONTHS_SHORT[selectedMonth] + " " + yearBE;
                    if (etEditDob != null) {
                        etEditDob.setText(formattedDate);
                    }
                    Toast.makeText(this, "เลือกวันเกิด: " + formattedDate, Toast.LENGTH_SHORT).show();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadExistingData() {
        if (currentUserEmail != null && !currentUserEmail.equalsIgnoreCase("guest")) {
            if (tvOwnerEmail != null) tvOwnerEmail.setText("✉ " + currentUserEmail);
            if (etEditEmail != null) etEditEmail.setText(currentUserEmail);

            String dbName = dbManager.getUserName(currentUserEmail);
            String nameToUse = (dbName != null && !dbName.trim().isEmpty()) ? dbName.trim() : currentUserEmail.split("@")[0];
            if (tvOwnerName != null) tvOwnerName.setText("ชื่อ " + nameToUse);
            if (etEditName != null) etEditName.setText(nameToUse);
        } else {
            if (tvOwnerName != null) tvOwnerName.setText("ชื่อ User");
            if (tvOwnerEmail != null) tvOwnerEmail.setText("✉ user@gmail.com");
            if (etEditName != null) etEditName.setText("User");
            if (etEditEmail != null) etEditEmail.setText("user@gmail.com");
        }

        String userPrefix = (currentUserEmail != null && !currentUserEmail.isEmpty()) ? currentUserEmail.toLowerCase().trim() + "_" : "guest_";

        String savedPhone = prefs.getString(userPrefix + "phone", "");
        if (tvOwnerPhone != null) {
            tvOwnerPhone.setText(!savedPhone.isEmpty() ? "📞 " + savedPhone : "📞 -");
        }
        if (etEditPhone != null) etEditPhone.setText(savedPhone);

        String savedDob = prefs.getString(userPrefix + "dob", "");
        if (etEditDob != null) etEditDob.setText(savedDob);

        // Address Fields
        if (etHouseNo != null) etHouseNo.setText(prefs.getString(userPrefix + "house_no", ""));
        if (etMoo != null) etMoo.setText(prefs.getString(userPrefix + "moo", ""));
        if (etRoad != null) etRoad.setText(prefs.getString(userPrefix + "road", ""));
        if (etSubDistrict != null) etSubDistrict.setText(prefs.getString(userPrefix + "sub_district", ""));
        if (etDistrict != null) etDistrict.setText(prefs.getString(userPrefix + "district", ""));
        if (etProvince != null) etProvince.setText(prefs.getString(userPrefix + "province", ""));

        String savedAvatar = prefs.getString(userPrefix + "avatar_uri", "");
        if (ivOwnerAvatar != null && !savedAvatar.isEmpty()) {
            try {
                ivOwnerAvatar.setImageURI(Uri.parse(savedAvatar));
            } catch (Exception ignored) {
            }
        }
    }

    private void saveChanges() {
        String name = etEditName != null ? etEditName.getText().toString().trim() : "";
        String phone = etEditPhone != null ? etEditPhone.getText().toString().trim() : "";
        String dob = etEditDob != null ? etEditDob.getText().toString().trim() : "";

        String houseNo = etHouseNo != null ? etHouseNo.getText().toString().trim() : "";
        String moo = etMoo != null ? etMoo.getText().toString().trim() : "";
        String road = etRoad != null ? etRoad.getText().toString().trim() : "";
        String subDistrict = etSubDistrict != null ? etSubDistrict.getText().toString().trim() : "";
        String district = etDistrict != null ? etDistrict.getText().toString().trim() : "";
        String province = etProvince != null ? etProvince.getText().toString().trim() : "";

        // Combine full address string
        StringBuilder fullAddress = new StringBuilder();
        if (!houseNo.isEmpty()) fullAddress.append(houseNo);
        if (!moo.isEmpty()) fullAddress.append(" หมู่ ").append(moo);
        if (!road.isEmpty()) fullAddress.append(" ถ.").append(road);
        if (!subDistrict.isEmpty()) fullAddress.append(" ต.").append(subDistrict);
        if (!district.isEmpty()) fullAddress.append(" อ.").append(district);
        if (!province.isEmpty()) fullAddress.append(" จ.").append(province);

        String finalAddress = fullAddress.length() > 0 ? fullAddress.toString().trim() : "-";

        // Update SQLite Database
        if (!name.isEmpty()) {
            dbManager.updateUserProfile(currentUserEmail, name, null);
        }
        dbManager.saveHomeAddress(currentUserEmail, finalAddress);

        // Update SharedPreferences per email
        String userPrefix = (currentUserEmail != null && !currentUserEmail.isEmpty()) ? currentUserEmail.toLowerCase().trim() + "_" : "guest_";
        SharedPreferences.Editor editor = prefs.edit();
        if (!name.isEmpty()) editor.putString(userPrefix + "name", name);
        if (!phone.isEmpty()) editor.putString(userPrefix + "phone", phone);
        if (!dob.isEmpty()) editor.putString(userPrefix + "dob", dob);
        editor.putString(userPrefix + "house_no", houseNo);
        editor.putString(userPrefix + "moo", moo);
        editor.putString(userPrefix + "road", road);
        editor.putString(userPrefix + "sub_district", subDistrict);
        editor.putString(userPrefix + "district", district);
        editor.putString(userPrefix + "province", province);
        if (selectedAvatarUri != null) {
            editor.putString(userPrefix + "avatar_uri", selectedAvatarUri.toString());
        }
        editor.apply();
        editor.apply();

        Toast.makeText(this, "บันทึกการเปลี่ยนแปลงเรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show();
        finish();
    }
}