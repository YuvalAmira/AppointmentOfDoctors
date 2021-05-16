package com.example.appointmentofdoctors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PatientScreenActivity extends AppCompatActivity {
    TextView helloTv;
    CheckBox FilteravailableDoctors;
    RecyclerView recyclerView;
    ListDoctorAdapter adapter;
    List<Doctor> allDoctorList = new ArrayList<Doctor>();
    List<Doctor> availableDoctorsList = new ArrayList<Doctor>();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_screen);
        FilteravailableDoctors = findViewById(R.id.availbleDoctorCheckbox);
        helloTv = findViewById(R.id.hello);
        DatabaseReference NameUser = FirebaseDatabase.getInstance().getReference("Users").child("Patients");
        NameUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Patient curpatient = snapshot1.getValue(Patient.class);
                    if (curpatient.getId().equals(firebaseUser.getUid())) {
                        helloTv.setText("Hello" + " " + curpatient.getUserName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerView = findViewById(R.id.recyclerview_doctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        UpdateAllDoctor();

        FilteravailableDoctors.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    UpdateavailableDoctors();
                } else {
                    UpdateAllDoctor();
                }
            }
        });
    }

    private void UpdateavailableDoctors() {
        DatabaseReference DoctorReference = FirebaseDatabase.getInstance().getReference("Users").child("Doctors");
        DoctorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                availableDoctorsList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Doctor doctor = snapshot1.getValue(Doctor.class);
                        if (doctor.getAvailability() == true) {
                            availableDoctorsList.add(doctor);
                        }
                    }
                    adapter = new ListDoctorAdapter(PatientScreenActivity.this, availableDoctorsList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void UpdateAllDoctor() {
        DatabaseReference DoctorReference = FirebaseDatabase.getInstance().getReference("Users").child("Doctors");
        DoctorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allDoctorList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Doctor doctor = snapshot1.getValue(Doctor.class);
                        allDoctorList.add(doctor);
                    }
                    adapter = new ListDoctorAdapter(PatientScreenActivity.this, allDoctorList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Logout:
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(PatientScreenActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}