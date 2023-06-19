package com.example.mhasnikhalid_motionpilot_projet;

public class ActivityData {
    private String activityName;
    private String date;
    private String startTime;
    private String endTime;

    public ActivityData() {
        // Default no-argument constructor required for Firestore deserialization
    }

    public ActivityData(String activityName, String date, String startTime, String endTime) {
        this.activityName = activityName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
