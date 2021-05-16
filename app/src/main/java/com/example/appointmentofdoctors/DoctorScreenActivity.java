package com.example.appointmentofdoctors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DoctorScreenActivity extends AppCompatActivity {
    TextView helloTv, waitinglistemptyTV;
    RecyclerView recyclerCuurent, recyclerWaitingList;
    ListPatientAdapter adapterCuurent, adapterWaitingList;
    List<Patient> CuurentPatient = new ArrayList<Patient>();
    List<Patient> WaitingList = new ArrayList<Patient>();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    long periodAppointment = 180000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_screen);
        helloTv = findViewById(R.id.hello);
        waitinglistemptyTV = findViewById(R.id.waitinglistempty);
        waitinglistemptyTV.setVisibility(View.INVISIBLE);
        DatabaseReference NameUser = FirebaseDatabase.getInstance().getReference("Users").child("Doctors");
        NameUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Doctor curDoctor = snapshot1.getValue(Doctor.class);
                    if (curDoctor.getId().equals(firebaseUser.getUid())) {
                        helloTv.setText("Hello" + " " + curDoctor.getUserName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerCuurent = findViewById(R.id.recyclerview_cuurent);
        recyclerCuurent.setLayoutManager(new LinearLayoutManager(this));
        recyclerCuurent.setHasFixedSize(true);
        recyclerWaitingList = findViewById(R.id.recyclerview_WaitingList);
        recyclerWaitingList.setLayoutManager(new LinearLayoutManager(this));
        recyclerWaitingList.setHasFixedSize(true);
        UpdateWaitingList();
    }

    private void UpdateWaitingList() {
        DatabaseReference WaitingListReference = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(firebaseUser.getUid());
        WaitingListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("WaitingList")) {
                    waitinglistemptyTV.setVisibility(View.INVISIBLE);
                    updateWaitingList();
                    CuurentPatient.clear();
                    WaitingList.clear();
                    for (DataSnapshot snapshot1 : snapshot.child("WaitingList").getChildren()) {
                        WaitingList.add(snapshot1.getValue(Patient.class));
                    }
                    CuurentPatient.clear();
                    CuurentPatient.add(WaitingList.get(0));
                    adapterCuurent = new ListPatientAdapter(CuurentPatient, DoctorScreenActivity.this);
                    recyclerCuurent.setAdapter(adapterCuurent);
                    adapterCuurent.notifyDataSetChanged();

                    WaitingList.remove(0);
                    adapterWaitingList = new ListPatientAdapter(WaitingList, DoctorScreenActivity.this);
                    recyclerWaitingList.setAdapter(adapterWaitingList);
                    adapterWaitingList.notifyDataSetChanged();
                } else {
                    waitinglistemptyTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateWaitingList() {
        DatabaseReference PatientsListRef = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(firebaseUser.getUid()).child("WaitingList");
        PatientsListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if ((patient.getAppointmentime() + periodAppointment) < (System.currentTimeMillis())) {
                        snapshot.getRef().removeValue();
                    }
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

                Intent intent = new Intent(DoctorScreenActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}