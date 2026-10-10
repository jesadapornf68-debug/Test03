package com.example.myapplicationjt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.example.myapplicationjt.model.Pet;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;
    private String currentUserEmail;

    private TextView tvOwnerName;
    private TextView tvOwnerPhone;
    private TextView tvOwnerEmail;
    private TextView tvOwnerAddress;

    private final ActivityResultLauncher<String[]> pickProfileImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }
                    String userPrefix = (currentUserEmail != null && !currentUserEmail.isEmpty()) ? currentUserEmail.toLowerCase().trim() + "_" : "guest_";
                    SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
                    prefs.edit().putString(userPrefix + "avatar_uri", uri.toString()).apply();

                    ImageView ivOwnerAvatar = findViewById(R.id.ivOwnerAvatar);
                    if (ivOwnerAvatar != null) {
                        ivOwnerAvatar.setImageURI(uri);
                    }
                    Toast.makeText(this, "เปลี่ยนรูปโปรไฟล์เรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);
        currentUserEmail = dbManager.getCurrentUserEmail();

        // Profile Avatar Click to Pick Image
        ImageView ivOwnerAvatar = findViewById(R.id.ivOwnerAvatar);
        if (ivOwnerAvatar != null) {
            ivOwnerAvatar.setOnClickListener(v -> pickProfileImageLauncher.launch(new String[]{"image/*"}));
        }

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 2. Header Gear Button
        ImageView btnHeaderGear = findViewById(R.id.btnHeaderGear);
        if (btnHeaderGear != null) {
            btnHeaderGear.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerPhone = findViewById(R.id.tvOwnerPhone);
        tvOwnerEmail = findViewById(R.id.tvOwnerEmail);
        tvOwnerAddress = findViewById(R.id.tvOwnerAddress);

        // Load Owner Information & My Pets List
        loadOwnerData();
        renderMyPetsList();

        // 3. Edit Profile Button
        MaterialCardView btnEditOwnerProfile = findViewById(R.id.btnEditOwnerProfile);
        if (btnEditOwnerProfile != null) {
            btnEditOwnerProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        }

        // 4. Edit Address Row Click
        RelativeLayout btnEditAddressRow = findViewById(R.id.btnEditAddressRow);
        if (btnEditAddressRow != null) {
            btnEditAddressRow.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        }

        // 5. Add Pet Button
        TextView btnAddPet = findViewById(R.id.btnAddPet);
        if (btnAddPet != null) {
            btnAddPet.setOnClickListener(v -> showAddPetDialog());
        }

        // 6. Settings Section Actions
        RelativeLayout btnSettingEditInfo = findViewById(R.id.btnSettingEditInfo);
        if (btnSettingEditInfo != null) {
            btnSettingEditInfo.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        }

        RelativeLayout btnSettingChangePass = findViewById(R.id.btnSettingChangePass);
        if (btnSettingChangePass != null) {
            btnSettingChangePass.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
        }

        RelativeLayout btnSettingPrivacy = findViewById(R.id.btnSettingPrivacy);
        if (btnSettingPrivacy != null) {
            btnSettingPrivacy.setOnClickListener(v -> startActivity(new Intent(this, PrivacySettingsActivity.class)));
        }

        RelativeLayout btnSettingLogout = findViewById(R.id.btnSettingLogout);
        if (btnSettingLogout != null) {
            btnSettingLogout.setOnClickListener(v -> startActivity(new Intent(this, LogoutActivity.class)));
        }

        // 7. Bottom Navigation Listeners
        LinearLayout navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }

        LinearLayout navHospital = findViewById(R.id.navHospital);
        if (navHospital != null) {
            navHospital.setOnClickListener(v -> {
                startActivity(new Intent(this, HospitalSearchActivity.class));
                finish();
            });
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
            navOwner.setOnClickListener(v -> Toast.makeText(this, "คุณอยู่ที่หน้าข้อมูลเจ้าของแล้ว", Toast.LENGTH_SHORT).show());
        }

        LinearLayout navMore = findViewById(R.id.navMore);
        if (navMore != null) {
            navMore.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOwnerData();
        renderMyPetsList();
    }

    private void renderMyPetsList() {
        LinearLayout containerPets = findViewById(R.id.containerPets);
        if (containerPets == null) return;

        containerPets.removeAllViews();

        String email = dbManager.getCurrentUserEmail();
        List<Pet> pets = dbManager.getPets(email);

        if (pets == null || pets.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("ยังไม่มีสัตว์เลี้ยง กด '+ เพิ่มสัตว์เลี้ยง' ด้านบนเพื่อเพิ่มได้เลย");
            tvEmpty.setTextColor(0xFF78909C);
            tvEmpty.setTextSize(13f);
            tvEmpty.setPadding(8, 8, 8, 8);
            containerPets.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < pets.size(); i++) {
            final Pet p = pets.get(i);

            RelativeLayout row = new RelativeLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            int paddingPx = (int) (6 * getResources().getDisplayMetrics().density);
            row.setPadding(0, paddingPx, 0, paddingPx);
            row.setClickable(true);
            row.setFocusable(true);

            // Card Image Container
            MaterialCardView cardImg = new MaterialCardView(this);
            int cardImgId = View.generateViewId();
            cardImg.setId(cardImgId);
            int imgSizePx = (int) (56 * getResources().getDisplayMetrics().density);
            RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(imgSizePx, imgSizePx);
            imgParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            imgParams.addRule(RelativeLayout.CENTER_VERTICAL);
            cardImg.setLayoutParams(imgParams);
            cardImg.setRadius(10 * getResources().getDisplayMetrics().density);
            cardImg.setCardElevation(0f);
            cardImg.setStrokeWidth(0);

            ImageView ivPhoto = new ImageView(this);
            ivPhoto.setLayoutParams(new MaterialCardView.LayoutParams(
                    MaterialCardView.LayoutParams.MATCH_PARENT,
                    MaterialCardView.LayoutParams.MATCH_PARENT
            ));
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (p.getPhotoUri() != null && !p.getPhotoUri().isEmpty()) {
                try {
                    ivPhoto.setImageURI(Uri.parse(p.getPhotoUri()));
                } catch (Exception ignored) {
                    ivPhoto.setImageResource(R.drawable.ic_pet_logo);
                }
            } else {
                ivPhoto.setImageResource(R.drawable.ic_pet_logo);
            }
            cardImg.addView(ivPhoto);
            row.addView(cardImg);

            // Chevron Right
            ImageView ivChevron = new ImageView(this);
            int chevronId = View.generateViewId();
            ivChevron.setId(chevronId);
            int chevSizePx = (int) (20 * getResources().getDisplayMetrics().density);
            RelativeLayout.LayoutParams chevParams = new RelativeLayout.LayoutParams(chevSizePx, chevSizePx);
            chevParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            chevParams.addRule(RelativeLayout.CENTER_VERTICAL);
            ivChevron.setLayoutParams(chevParams);
            ivChevron.setImageResource(R.drawable.ic_chevron_right);
            ivChevron.setColorFilter(0xFF1E88E5);
            row.addView(ivChevron);

            // Text Info Layout
            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams infoParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            infoParams.addRule(RelativeLayout.RIGHT_OF, cardImgId);
            infoParams.addRule(RelativeLayout.LEFT_OF, chevronId);
            infoParams.addRule(RelativeLayout.CENTER_VERTICAL);
            infoParams.setMarginStart((int) (12 * getResources().getDisplayMetrics().density));
            infoLayout.setLayoutParams(infoParams);

            TextView tvName = new TextView(this);
            tvName.setText(p.getPetName());
            tvName.setTextColor(0xFF0D47A1);
            tvName.setTextSize(16f);
            tvName.setTypeface(null, Typeface.BOLD);
            infoLayout.addView(tvName);

            TextView tvBreed = new TextView(this);
            String breedText = p.getPetBreed() != null && !p.getPetBreed().isEmpty() ? p.getPetBreed() : "สัตว์เลี้ยง";
            tvBreed.setText(breedText);
            tvBreed.setTextColor(0xFF546E7A);
            tvBreed.setTextSize(13f);
            LinearLayout.LayoutParams breedParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            breedParams.topMargin = (int) (2 * getResources().getDisplayMetrics().density);
            tvBreed.setLayoutParams(breedParams);
            infoLayout.addView(tvBreed);

            row.addView(infoLayout);

            // Click listener on pet row: opens Toast & option
            row.setOnClickListener(v -> {
                Toast.makeText(this, "สัตว์เลี้ยง: " + p.getPetName() + " (" + breedText + ")", Toast.LENGTH_SHORT).show();
            });

            containerPets.addView(row);

            // Divider Line
            if (i < pets.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (int) (1 * getResources().getDisplayMetrics().density)
                );
                divParams.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, (int) (8 * getResources().getDisplayMetrics().density));
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(0xFFECEFF1);
                containerPets.addView(divider);
            }
        }
    }



    private void loadOwnerData() {
        if (currentUserEmail != null && !currentUserEmail.equalsIgnoreCase("guest")) {
            if (tvOwnerEmail != null) tvOwnerEmail.setText("✉ " + currentUserEmail);

            String dbName = dbManager.getUserName(currentUserEmail);
            if (dbName != null && !dbName.trim().isEmpty()) {
                if (tvOwnerName != null) tvOwnerName.setText("ชื่อ " + dbName);
            } else if (currentUserEmail.contains("@")) {
                String prefix = currentUserEmail.split("@")[0];
                if (tvOwnerName != null) tvOwnerName.setText("ชื่อ " + prefix);
            }
        } else {
            if (tvOwnerName != null) tvOwnerName.setText("ชื่อ User");
            if (tvOwnerEmail != null) tvOwnerEmail.setText("✉ user@gmail.com");
        }

        SharedPreferences prefs = getSharedPreferences("UserProfilePrefs", MODE_PRIVATE);
        String userPrefix = (currentUserEmail != null && !currentUserEmail.isEmpty()) ? currentUserEmail.toLowerCase().trim() + "_" : "guest_";

        String phone = prefs.getString(userPrefix + "phone", "");
        if (tvOwnerPhone != null) {
            if (!phone.isEmpty() && !phone.equalsIgnoreCase("088-xxx-xxxx")) {
                tvOwnerPhone.setText("📞 " + phone);
            } else {
                tvOwnerPhone.setText("📞 -");
            }
        }

        String savedAddress = dbManager.getHomeAddress(currentUserEmail);
        if (savedAddress != null && !savedAddress.trim().isEmpty() && !savedAddress.contains("99/9 หมู่ 7") && !savedAddress.equals("ยังไม่ได้ระบุที่อยู่") && !savedAddress.equals("-")) {
            if (tvOwnerAddress != null) tvOwnerAddress.setText(savedAddress);
        } else {
            if (tvOwnerAddress != null) tvOwnerAddress.setText("-");
        }

        String avatarUri = prefs.getString(userPrefix + "avatar_uri", "");
        ImageView ivOwnerAvatar = findViewById(R.id.ivOwnerAvatar);
        if (ivOwnerAvatar != null && !avatarUri.isEmpty()) {
            try {
                ivOwnerAvatar.setImageURI(Uri.parse(avatarUri));
            } catch (Exception ignored) {
            }
        }
    }

    private void showAddPetDialog() {
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
                Pet newPet = new Pet(
                        UUID.randomUUID().toString(),
                        currentUserEmail,
                        tvOwnerName != null ? tvOwnerName.getText().toString() : "Owner",
                        "088-xxx-xxxx",
                        name,
                        breed.isEmpty() ? "สัตว์เลี้ยง" : breed,
                        ""
                );
                dbManager.addPet(newPet);
                renderMyPetsList();
                Toast.makeText(this, "เพิ่มสัตว์เลี้ยง (" + name + ") เรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "กรุณากรอกชื่อสัตว์เลี้ยง", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ยกเลิก", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}