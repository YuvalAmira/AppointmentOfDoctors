package com.example.appointmentofdoctors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText UsernameMET, EmailMET, PasswordMET;
    Button RegisterBtn;
    RadioGroup TypeUserRG;
    RadioButton TypeUserRB;
    String TypeUserStr = "Doctor";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        UsernameMET = findViewById(R.id.username_register);
        EmailMET = findViewById(R.id.email_register);
        PasswordMET = findViewById(R.id.password_register);
        RegisterBtn = findViewById(R.id.btn_register);
        TypeUserRG = findViewById(R.id.RG_sizechips);

        TypeUserRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioid = TypeUserRG.getCheckedRadioButtonId();
                TypeUserRB = findViewById(radioid);
                TypeUserStr = TypeUserRB.getText().toString();
            }
        });
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UsernameStr = UsernameMET.getText().toString();
                String EmailStr = EmailMET.getText().toString();
                String PasswordStr = PasswordMET.getText().toString();

                int radioid = TypeUserRG.getCheckedRadioButtonId();
                TypeUserRB = findViewById(radioid);
                TypeUserStr = TypeUserRB.getText().toString();

                if (TextUtils.isEmpty(UsernameStr) || TextUtils.isEmpty(EmailStr) || TextUtils.isEmpty(PasswordStr)) {
                    Toast.makeText(RegisterActivity.this, " All data must be entered", Toast.LENGTH_SHORT).show();
                } else if (PasswordStr.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(EmailStr, PasswordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                assert firebaseUser != null;
                                String userid = firebaseUser.getUid();
                                if (TypeUserStr.equals("Doctor")) {
                                    DatabaseReference referenceDoctors = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(userid);
                                    Doctor Doctor = new Doctor(userid, UsernameStr, true, System.currentTimeMillis());
                                    referenceDoctors.setValue(Doctor).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(RegisterActivity.this, DoctorScreenActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else
                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    });
                                } else if (TypeUserStr.equals("Patient")) {
                                    DatabaseReference referencePatient = FirebaseDatabase.getInstance().getReference("Users").child("Patients").child(userid);
                                    Patient Patient = new Patient(userid, UsernameStr, System.currentTimeMillis());
                                    referencePatient.setValue(Patient).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Intent intent = new Intent(RegisterActivity.this, PatientScreenActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else
                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    });
                                }
                            } else
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}