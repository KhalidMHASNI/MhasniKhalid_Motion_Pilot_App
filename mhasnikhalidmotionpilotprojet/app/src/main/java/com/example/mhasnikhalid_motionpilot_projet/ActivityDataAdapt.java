package com.example.mhasnikhalid_motionpilot_projet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityDataAdapt extends RecyclerView.Adapter<ActivityDataAdapt.ViewHolder> {
    private List<ActivityData> activityData;

    public ActivityDataAdapt(List<ActivityData> activityData) {
        this.activityData = activityData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityData activityData = this.activityData.get(position);

        holder.activityNameTextView.setText(activityData.getActivityName());
        holder.dateTextView.setText(activityData.getDate());
        holder.startTimeTextView.setText(activityData.getStartTime());
        holder.endTimeTextView.setText(activityData.getEndTime());
    }

    @Override
    public int getItemCount() {
        return activityData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityNameTextView;
        TextView dateTextView;
        TextView startTimeTextView;
        TextView endTimeTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityNameTextView = itemView.findViewById(R.id.activity_name_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            startTimeTextView = itemView.findViewById(R.id.start_time_text_view);
            endTimeTextView = itemView.findViewById(R.id.end_time_text_view);
        }
    }
}

