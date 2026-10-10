package com.example.myapplicationjt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.example.myapplicationjt.model.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbManager = PetCareDBManager.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.topToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        TextInputEditText etName = findViewById(R.id.etRegName);
        TextInputEditText etEmail = findViewById(R.id.etRegEmail);
        TextInputEditText etPassword = findViewById(R.id.etRegPassword);
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitRegister);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                String cleanEmail = email.trim().toLowerCase();
                User newUser = new User(UUID.randomUUID().toString(), name, cleanEmail, password);

                boolean success = dbManager.registerUser(newUser);
                if (success) {
                    dbManager.switchUserSession(cleanEmail);
                    Toast.makeText(this, "สมัครสมาชิกสำเร็จ! ยินดีต้อนรับคุณ " + name, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "อีเมลนี้มีอยู่ในระบบแล้ว กรุณาใช้อีเมลอื่น", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบทุกช่อง", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
