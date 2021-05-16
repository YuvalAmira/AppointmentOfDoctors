package com.example.appointmentofdoctors;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListDoctorAdapter extends RecyclerView.Adapter<ListDoctorAdapter.ListDoctorViewHolder> {

    private Context mContext;
    private List<Doctor> doctorList;
    long periodAppointment = 180000;//3 sec
    FirebaseUser currentPatient;
    CountDownTimer countDownTimer, countDownTimer1;
    List<Patient> WaitingList = new ArrayList<Patient>();
    ListPatientAdapter adapterWaitingList;

    public ListDoctorAdapter(Context mContext, List<Doctor> doctorList) {
        this.mContext = mContext;
        this.doctorList = doctorList;
    }

    public class ListDoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name_tv, availability_tv;
        Button register_bt, unsubscribe_bt, waitinglist_bt;

        public ListDoctorViewHolder(@NonNull View itemView) {
            super(itemView);

            name_tv = itemView.findViewById(R.id.doctorName);
            availability_tv = itemView.findViewById(R.id.availableDoc);
            register_bt = itemView.findViewById(R.id.appointment_bt);
            unsubscribe_bt = itemView.findViewById(R.id.Cancel_bt);
            waitinglist_bt = itemView.findViewById(R.id.ListWaiting_bt);
        }
    }

    @NonNull
    @Override
    public ListDoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item, parent, false);
        ListDoctorViewHolder ListDoctorViewHolderViewHolder = new ListDoctorViewHolder(view);
        return ListDoctorViewHolderViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListDoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        holder.name_tv.setText(doctor.getUserName());
        currentPatient = FirebaseAuth.getInstance().getCurrentUser();
        updateWaitingList(doctor);
        DatabaseReference PatientsListReference = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("WaitingList");
        PatientsListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int flagPatientRegister = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    if (patient.getId().equals(currentPatient.getUid())) {
                        flagPatientRegister = 1;
                    }
                }

                if (flagPatientRegister==1) {
                    holder.register_bt.setVisibility(View.INVISIBLE);
                    holder.unsubscribe_bt.setVisibility(View.VISIBLE);
                } else {
                    holder.register_bt.setVisibility(View.VISIBLE);
                    holder.unsubscribe_bt.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (doctor.getTimeToBeAvailable() - System.currentTimeMillis() < 0) {
            //  if (System.currentTimeMillis() - doctor.getTimeToBeAvailable() > 0) {
            doctor.setAvailability(true);
            FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("availability").setValue(true);
            holder.availability_tv.setText("available");
            holder.availability_tv.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            doctor.setAvailability(false);
            FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("availability").setValue(false);
            holder.availability_tv.setText("Unavailable");
            holder.availability_tv.setTextColor(Color.parseColor("#F44336"));
        }

        holder.register_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doctor.getTimeToBeAvailable() - System.currentTimeMillis() < 0) {
              //  if (System.currentTimeMillis() - doctor.getTimeToBeAvailable() > 0) {
                    DoWhenDoctorAvailable(doctor);
                } else {
                    DoWhenDoctorUnavailable(doctor);
                }
            }
        });
        holder.unsubscribe_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                removePatientFromWaitingList(doctor);
                doctor.setAvailability(true);
                FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("availability").setValue(true);
                FirebaseDatabase.getInstance().getReference("Users").child("Patients").child(currentPatient.getUid()).child("appointmentime").setValue(System.currentTimeMillis());
            }
        });
        holder.waitinglist_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder DialogBuilder = new AlertDialog.Builder(mContext);
                View mView = LayoutInflater.from(mContext).inflate(R.layout.waitinglist_dialog, null);
                TextView titleTv = mView.findViewById(R.id.title);
                titleTv.setText("Waiting list for Dr:" +" "+ doctor.getUserName());
                RecyclerView recyclerWaitingList = mView.findViewById(R.id.recyclerview_dialogWL);
                recyclerWaitingList.setLayoutManager(new LinearLayoutManager(mContext));
                recyclerWaitingList.setHasFixedSize(true);

                DatabaseReference  DoctorsReference = FirebaseDatabase.getInstance().getReference("Users").child("Doctors");
                DoctorsReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Doctor doctornew  =  snapshot.getValue(Doctor.class);
                            if(doctornew.getId().equals(doctor.getId()))
                            {
                                DatabaseReference referenceWaitingList = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("WaitingList");
                                referenceWaitingList.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        WaitingList.clear();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            WaitingList.add(snapshot.getValue(Patient.class));
                                        }
                                        adapterWaitingList = new ListPatientAdapter(WaitingList,mContext);
                                        recyclerWaitingList.setAdapter(adapterWaitingList);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                AlertDialog waitingListDialog = DialogBuilder.create();
                waitingListDialog.setView(mView);
                Button exitBtn = mView.findViewById(R.id.exitDialog);
                exitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        waitingListDialog.dismiss();
                    }
                });
                waitingListDialog.setCanceledOnTouchOutside(false);
                waitingListDialog.show();
            }

        });
    }

    private void DoWhenDoctorAvailable(Doctor doctor) {
        FirebaseDatabase.getInstance().getReference("Users").child("Patients").child(currentPatient.getUid()).child("appointmentime").setValue(System.currentTimeMillis());
        AddAppointmentToWaitingList(doctor);
        DatabaseReference CurrentDoctorReference = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId());
        CurrentDoctorReference.child("availability").setValue(false);
        doctor.setAvailability(false);
        CurrentDoctorReference.child("timeToBeAvailable").setValue(System.currentTimeMillis() + periodAppointment);
        doctor.setTimeToBeAvailable(System.currentTimeMillis() + periodAppointment);
        sendnotification();
        DatabaseReference CurrentPatRef = FirebaseDatabase.getInstance().getReference("Users").child("Patients").child(currentPatient.getUid());
        CurrentPatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Patient patient = snapshot.getValue(Patient.class);
                countDownTimer1 = new CountDownTimer(periodAppointment, 1000) {
                    @Override
                    public void onTick(long l) {
                    }

                    @Override
                    public void onFinish() {
                        removePatientFromWaitingList(doctor);
                    }
                }.start();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void DoWhenDoctorUnavailable(Doctor doctor) {
        FirebaseDatabase.getInstance().getReference("Users").child("Patients").child(currentPatient.getUid()).child("appointmentime").setValue(doctor.getTimeToBeAvailable());
        AddAppointmentToWaitingList(doctor);
        DatabaseReference CurrentDoctorReference = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId());
        CurrentDoctorReference.child("timeToBeAvailable").setValue(doctor.getTimeToBeAvailable() + periodAppointment);
        doctor.setTimeToBeAvailable(doctor.getTimeToBeAvailable() + periodAppointment);
        DatabaseReference CurrentPatRef = FirebaseDatabase.getInstance().getReference("Users").child("Patients").child(currentPatient.getUid());
        CurrentPatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Patient patient = snapshot.getValue(Patient.class);
                long PatientTurnTime = (patient.getAppointmentime() - System.currentTimeMillis());
                countDownTimer = new CountDownTimer(PatientTurnTime, 1000) {
                    @Override
                    public void onTick(long l) { }
                    @Override
                    public void onFinish() {
                        sendnotification();
                        removePatientFromWaitingList(doctor);
                    }
                }.start();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void removePatientFromWaitingList(Doctor doctor) {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference PatientsListRef = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("WaitingList");
        PatientsListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient.getId().equals(current.getUid()))
                    {   snapshot.getRef().removeValue();}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void AddAppointmentToWaitingList(Doctor doctor) {
        DatabaseReference PatientDBReference = FirebaseDatabase.getInstance().getReference("Users").child("Patients").child(currentPatient.getUid());
        PatientDBReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference WaitingListRef = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("WaitingList").child(currentPatient.getUid());
                WaitingListRef.setValue(snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendnotification() {
        OreoNotification oreoNotification = new OreoNotification(mContext);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = oreoNotification.getNotification("Doctor's appointments", "Your turn now", defaultSound);
        oreoNotification.getManager().notify(1, builder.build());
    }

    private void updateWaitingList(Doctor doctor) {
        DatabaseReference PatientsListRef = FirebaseDatabase.getInstance().getReference("Users").child("Doctors").child(doctor.getId()).child("WaitingList");
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
    public int getItemCount() {
        return doctorList.size();
    }
}

