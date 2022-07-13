package com.anubhav.assignmentsfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FinishedTaskAdapter extends RecyclerView.Adapter<FinishedTaskAdapter.MyViewHolder>{

    private ArrayList<Task> tasksList;
    private Context context;

    public FinishedTaskAdapter(ArrayList<Task> tasksList) {
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
    public FinishedTaskAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        View itemView = LayoutInflater.from(context).inflate(R.layout.task_list, parent, false);
        return new FinishedTaskAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FinishedTaskAdapter.MyViewHolder holder, int position) {
        String taskName = tasksList.get(position).getName();
        holder.taskName.setText(taskName);

        int taskIconImage = tasksList.get(position).getIcon();
        holder.taskIconImage.setImageResource(taskIconImage);
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }
}