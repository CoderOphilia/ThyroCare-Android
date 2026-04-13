package com.example.thyrocare;

public class FoodDirectoryItem {
    private final String name;
    private final String category;
    private final String summary;
    private final String focus;

    public FoodDirectoryItem(String name, String category, String summary, String focus) {
        this.name = name;
        this.category = category;
        this.summary = summary;
        this.focus = focus;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSummary() {
        return summary;
    }

    public String getFocus() {
        return focus;
    }

    public boolean matches(String query) {
        String normalized = query.toLowerCase().trim();
        return normalized.isEmpty()
                || name.toLowerCase().contains(normalized)
                || category.toLowerCase().contains(normalized)
                || summary.toLowerCase().contains(normalized)
                || focus.toLowerCase().contains(normalized);
    }
}
