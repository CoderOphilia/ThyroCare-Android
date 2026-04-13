package com.example.thyrocare;

public class RecipeSuggestion {
    private final String title;
    private final String detail;
    private final String sourceUrl;
    private final String imageUrl;

    public RecipeSuggestion(String title, String detail, String sourceUrl, String imageUrl) {
        this.title = title;
        this.detail = detail;
        this.sourceUrl = sourceUrl;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
