package com.example.myapplicationjt;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;
    private String generatedOtp = "";
    private String targetAccount = "";

    private EditText etEmailOrPhone;
    private EditText etOtpCode;
    private EditText etNewPassword;
    private EditText etConfirmPassword;

    private MaterialCardView cardOtpSection;
    private MaterialCardView cardResetPasswordSection;
    private TextView tvOtpHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);

        // 1. Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etEmailOrPhone = findViewById(R.id.etEmailOrPhone);
        etOtpCode = findViewById(R.id.etOtpCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        cardOtpSection = findViewById(R.id.cardOtpSection);
        cardResetPasswordSection = findViewById(R.id.cardResetPasswordSection);
        tvOtpHint = findViewById(R.id.tvOtpHint);

        // 2. Step 1: Send OTP
        MaterialButton btnSendOtp = findViewById(R.id.btnSendOtp);
        if (btnSendOtp != null) {
            btnSendOtp.setOnClickListener(v -> sendOtp());
        }

        // 3. Step 2: Verify OTP
        MaterialButton btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        if (btnVerifyOtp != null) {
            btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        }

        // 4. Step 3: Reset Password
        MaterialButton btnResetPassword = findViewById(R.id.btnResetPassword);
        if (btnResetPassword != null) {
            btnResetPassword.setOnClickListener(v -> resetPassword());
        }
    }

    private void sendOtp() {
        targetAccount = etEmailOrPhone != null ? etEmailOrPhone.getText().toString().trim() : "";

        if (targetAccount.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกอีเมล หรือ เบอร์โทรศัพท์ที่ลงทะเบียนไว้", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate Random 6-Digit OTP Code
        int random6Digit = 100000 + new Random().nextInt(900000);
        generatedOtp = String.valueOf(random6Digit);

        // Display OTP Verification Section
        if (cardOtpSection != null) {
            cardOtpSection.setVisibility(View.VISIBLE);
        }

        if (tvOtpHint != null) {
            tvOtpHint.setText("📲 ส่งรหัส OTP 6 หลักไปยัง " + targetAccount + " เรียบร้อยแล้ว\n(รหัส OTP ของคุณคือ: " + generatedOtp + ")");
        }

        // Pop up alert simulating SMS / Email Inbox delivery
        new AlertDialog.Builder(this)
                .setTitle("📲 ข้อความแจ้งเตือนรหัส OTP")
                .setMessage("รหัส OTP 6 หลักของคุณคือ: " + generatedOtp + "\n\n(นำรหัส 6 หลักนี้ไปกรอกในช่องเพื่อยืนยันตัวตน)")
                .setPositiveButton("ตกลง", null)
                .show();
    }

    private void verifyOtp() {
        String enteredOtp = etOtpCode != null ? etOtpCode.getText().toString().trim() : "";

        if (enteredOtp.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกรหัส OTP 6 หลัก", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOtp.equals(generatedOtp)) {
            Toast.makeText(this, "ยืนยันรหัส OTP สำเร็จ! กรุณาตั้งรหัสผ่านใหม่", Toast.LENGTH_SHORT).show();
            if (cardResetPasswordSection != null) {
                cardResetPasswordSection.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "รหัส OTP 6 หลักไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword() {
        String newPass = etNewPassword != null ? etNewPassword.getText().toString().trim() : "";
        String confirmPass = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

        if (newPass.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกรหัสผ่านใหม่", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 8) {
            Toast.makeText(this, "รหัสผ่านต้องมีความยาวอย่างน้อย 8 ตัวอักษร", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "รหัสผ่านใหม่และการยืนยันรหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save updated password in SQLite
        dbManager.updateUserProfile(targetAccount, null, newPass);
        Toast.makeText(this, "บันทึกรหัสผ่านใหม่สำเร็จแล้ว! กรุณาเข้าสู่ระบบด้วยรหัสผ่านใหม่", Toast.LENGTH_LONG).show();
        finish();
    }
}