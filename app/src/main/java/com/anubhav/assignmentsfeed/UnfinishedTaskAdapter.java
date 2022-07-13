package com.anubhav.assignmentsfeed;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UnfinishedTaskAdapter extends RecyclerView.Adapter<UnfinishedTaskAdapter.MyViewHolder> {

    private ArrayList<Task> tasksList;
    private Context context;

    public UnfinishedTaskAdapter(ArrayList<Task> tasksList) {
        this.tasksList = tasksList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView taskName;
        private ImageView taskIconImage;

        public MyViewHolder(final View itemView) {
            super(itemView);

            taskName = itemView.findViewById(R.id.taskName);
            taskIconImage = itemView.findViewById(R.id.taskIconImage);
        }
    }

    @NonNull
    @Override
    public UnfinishedTaskAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        View itemView = LayoutInflater.from(context).inflate(R.layout.task_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UnfinishedTaskAdapter.MyViewHolder holder, int position) {
        String taskName = tasksList.get(position).getName();
        holder.taskName.setText(taskName);

        int taskIconImage = tasksList.get(position).getIcon();
        holder.taskIconImage.setImageResource(taskIconImage);

        if (holder.taskName.getText().equals("Profile Image"))
            holder.itemView.setOnClickListener(v -> sendUserToSetProfileImageActivity());

        else if (holder.taskName.getText().equals("Student or Professor?"))
            holder.itemView.setOnClickListener(v -> sendUserToStudentOrProfessorActivity());

        else
            holder.itemView.setOnClickListener(v -> sendUserToSetBasicCredentialsActivity());
    }

    private void sendUserToSetBasicCredentialsActivity() {
        Intent otherCredentialsIntent = new Intent(context, BasicCredentialsActivity.class);
        context.startActivity(otherCredentialsIntent);
    }

    private void sendUserToStudentOrProfessorActivity() {
        Intent phoneNumberIntent = new Intent(context, StudentOrProfessorActivity.class);
        context.startActivity(phoneNumberIntent);
    }

    private void sendUserToSetProfileImageActivity() {
        Intent profileImageIntent = new Intent(context, SetProfileImageActivity.class);
        context.startActivity(profileImageIntent);
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }
}