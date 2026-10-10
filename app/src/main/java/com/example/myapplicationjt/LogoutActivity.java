package com.example.myapplicationjt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplicationjt.database.PetCareDBManager;
import com.google.android.material.button.MaterialButton;

public class LogoutActivity extends AppCompatActivity {

    private PetCareDBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logout);

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

        // 2. Cancel Button
        MaterialButton btnCancelLogout = findViewById(R.id.btnCancelLogout);
        if (btnCancelLogout != null) {
            btnCancelLogout.setOnClickListener(v -> finish());
        }

        // 3. Confirm Logout Button
        MaterialButton btnConfirmLogout = findViewById(R.id.btnConfirmLogout);
        if (btnConfirmLogout != null) {
            btnConfirmLogout.setOnClickListener(v -> {
                dbManager.logout();
                Toast.makeText(this, "ออกจากระบบเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}