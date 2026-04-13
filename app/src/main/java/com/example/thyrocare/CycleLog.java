package com.example.thyrocare;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CycleLog {
    private final String startDate;
    private final String endDate;
    private final String flow;
    private final String notes;
    private final List<String> symptoms;

    public CycleLog(String startDate, String endDate, String flow, String notes, List<String> symptoms) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.flow = flow;
        this.notes = notes;
        this.symptoms = symptoms;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getFlow() {
        return flow;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONArray symptomArray = new JSONArray();
        for (String symptom : symptoms) {
            symptomArray.put(symptom);
        }

        try {
            object.put("startDate", startDate);
            object.put("endDate", endDate);
            object.put("flow", flow);
            object.put("notes", notes);
            object.put("symptoms", symptomArray);
        } catch (Exception ignored) {
        }

        return object;
    }

    public static CycleLog fromJson(JSONObject object) {
        JSONArray symptomArray = object.optJSONArray("symptoms");
        List<String> symptoms = new ArrayList<>();
        if (symptomArray != null) {
            for (int index = 0; index < symptomArray.length(); index++) {
                symptoms.add(symptomArray.optString(index));
            }
        }

        return new CycleLog(
                object.optString("startDate"),
                object.optString("endDate"),
                object.optString("flow"),
                object.optString("notes"),
                symptoms
        );
    }
}
