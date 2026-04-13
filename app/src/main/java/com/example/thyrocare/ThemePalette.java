package com.example.thyrocare;

import android.graphics.Color;

public class ThemePalette {
    public final String levelName;
    public final String statusMessage;
    public final int accentColor;
    public final int backgroundColor;
    public final int surfaceColor;
    public final int nextGoal;

    private ThemePalette(String levelName, String statusMessage, String accentHex, String backgroundHex, String surfaceHex, int nextGoal) {
        this.levelName = levelName;
        this.statusMessage = statusMessage;
        this.accentColor = Color.parseColor(accentHex);
        this.backgroundColor = Color.parseColor(backgroundHex);
        this.surfaceColor = Color.parseColor(surfaceHex);
        this.nextGoal = nextGoal;
    }

    public static ThemePalette fromPoints(int totalPoints) {
        if (totalPoints >= 1500) {
            return new ThemePalette(
                    "Level 3: Sage Mastery",
                    "You unlocked Sage Green for long-term discipline.",
                    "#6B8F71",
                    "#F5FBF4",
                    "#E9F4EA",
                    -1
            );
        }

        if (totalPoints >= 501) {
            return new ThemePalette(
                    "Level 2: Lilac Consistency",
                    "You unlocked Lilac for staying on track.",
                    "#8B6FB1",
                    "#FBF8FF",
                    "#F2EBFF",
                    1500
            );
        }

        return new ThemePalette(
                "Level 1: Pink Foundation",
                "Default theme unlocked. The next theme opens at 501 points.",
                "#B86A84",
                "#FFF8FB",
                "#FFF0F6",
                501
        );
    }
}
