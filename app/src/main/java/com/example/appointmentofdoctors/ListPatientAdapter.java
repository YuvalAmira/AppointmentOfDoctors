package com.example.appointmentofdoctors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListPatientAdapter extends RecyclerView.Adapter<ListPatientAdapter.PatientViewHolder> {
    private Context mContext;
    private List<Patient> patientList;

    public ListPatientAdapter(List<Patient> patientList, Context mContext) {
        this.patientList = patientList;
        this.mContext = mContext;
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView NamePatientTv;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            NamePatientTv = itemView.findViewById(R.id.Patient_Name);
        }
    }

    @NonNull
    @Override
    public ListPatientAdapter.PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item, parent, false);
        PatientViewHolder patientViewHolder = new PatientViewHolder(view);
        return patientViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListPatientAdapter.PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.NamePatientTv.setText(patient.getUserName());
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

}
