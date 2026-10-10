package com.example.myapplicationjt;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.google.android.material.button.MaterialButton;

public class ChangePasswordActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;
    private String currentUserEmail;

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;

    private boolean isCurrentVisible = false;
    private boolean isNewVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);
        currentUserEmail = dbManager.getCurrentUserEmail();

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // 2. Toggle Password Visibilities
        ImageView btnToggleCurrent = findViewById(R.id.btnToggleCurrent);
        if (btnToggleCurrent != null) {
            btnToggleCurrent.setOnClickListener(v -> {
                isCurrentVisible = !isCurrentVisible;
                toggleVisibility(etCurrentPassword, btnToggleCurrent, isCurrentVisible);
            });
        }

        ImageView btnToggleNew = findViewById(R.id.btnToggleNew);
        if (btnToggleNew != null) {
            btnToggleNew.setOnClickListener(v -> {
                isNewVisible = !isNewVisible;
                toggleVisibility(etNewPassword, btnToggleNew, isNewVisible);
            });
        }

        ImageView btnToggleConfirm = findViewById(R.id.btnToggleConfirm);
        if (btnToggleConfirm != null) {
            btnToggleConfirm.setOnClickListener(v -> {
                isConfirmVisible = !isConfirmVisible;
                toggleVisibility(etConfirmPassword, btnToggleConfirm, isConfirmVisible);
            });
        }

        // 3. Submit Change Password Button
        MaterialButton btnSubmitChangePassword = findViewById(R.id.btnSubmitChangePassword);
        if (btnSubmitChangePassword != null) {
            btnSubmitChangePassword.setOnClickListener(v -> submitChangePassword());
        }
    }

    private void toggleVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (editText == null || toggleIcon == null) return;
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_eye_off);
        }
        editText.setSelection(editText.getText().length());
    }

    private void submitChangePassword() {
        String currentPass = etCurrentPassword != null ? etCurrentPassword.getText().toString().trim() : "";
        String newPass = etNewPassword != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

        if (currentPass.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกรหัสผ่านปัจจุบัน", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกรหัสผ่านใหม่", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 8) {
            Toast.makeText(this, "รหัสผ่านใหม่ต้องมีความยาวอย่างน้อย 8 ตัวอักษร", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "รหัสผ่านใหม่และการยืนยันรหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate user in DB
        boolean isValid = dbManager.validateUser(currentUserEmail, currentPass);
        if (!isValid && !currentUserEmail.equalsIgnoreCase("guest")) {
            Toast.makeText(this, "รหัสผ่านปัจจุบันไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save new password into DB
        dbManager.updateUserProfile(currentUserEmail, null, newPass);
        Toast.makeText(this, "เปลี่ยนรหัสผ่านสำเร็จเรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show();
        finish();
    }
}