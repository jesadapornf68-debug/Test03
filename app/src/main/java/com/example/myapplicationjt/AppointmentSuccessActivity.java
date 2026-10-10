package com.example.myapplicationjt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class AppointmentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_success);

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

        // 2. Read Intent Extras
        Intent intent = getIntent();
        String petName = intent.getStringExtra("petName");
        String hospitalName = intent.getStringExtra("hospitalName");
        String serviceName = intent.getStringExtra("serviceName");
        String apptDate = intent.getStringExtra("date");
        String apptTime = intent.getStringExtra("time");
        String photoUri = intent.getStringExtra("photoUri");
        String note = intent.getStringExtra("note");

        TextView tvPetName = findViewById(R.id.tvPetName);
        TextView tvHospitalName = findViewById(R.id.tvHospitalName);
        TextView tvServiceName = findViewById(R.id.tvServiceName);
        TextView tvApptDate = findViewById(R.id.tvApptDate);
        TextView tvApptTime = findViewById(R.id.tvApptTime);
        TextView tvApptNote = findViewById(R.id.tvApptNote);
        ImageView ivPetPhoto = findViewById(R.id.ivPetPhoto);

        if (petName != null && !petName.isEmpty() && tvPetName != null) {
            tvPetName.setText(petName);
        }
        if (hospitalName != null && !hospitalName.isEmpty() && tvHospitalName != null) {
            tvHospitalName.setText(hospitalName);
        }
        if (serviceName != null && !serviceName.isEmpty() && tvServiceName != null) {
            tvServiceName.setText(serviceName);
        }
        if (apptDate != null && !apptDate.isEmpty() && tvApptDate != null) {
            tvApptDate.setText(apptDate);
        }
        if (apptTime != null && !apptTime.isEmpty() && tvApptTime != null) {
            tvApptTime.setText(apptTime);
        }
        if (tvApptNote != null) {
            if (note != null && !note.isEmpty()) {
                tvApptNote.setText(note);
            } else {
                tvApptNote.setText("อาเจียน");
            }
        }
        if (photoUri != null && !photoUri.isEmpty() && ivPetPhoto != null) {
            try {
                ivPetPhoto.setImageURI(Uri.parse(photoUri));
            } catch (Exception ignored) {
            }
        }

        // 3. View Details Button
        MaterialButton btnViewDetails = findViewById(R.id.btnViewDetails);
        if (btnViewDetails != null) {
            btnViewDetails.setOnClickListener(v -> {
                Toast.makeText(this, "ดูรายละเอียดใบนัดหมาย", Toast.LENGTH_SHORT).show();
            });
        }

        // 4. Return Home Button
        MaterialButton btnGoHome = findViewById(R.id.btnGoHome);
        if (btnGoHome != null) {
            btnGoHome.setOnClickListener(v -> {
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                finish();
            });
        }
    }
}