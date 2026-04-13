package com.example.thyrocare;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SpoonacularClient {

    public interface Callback {
        void onSuccess(List<RecipeSuggestion> recipes);

        void onError(String message);
    }

    public void fetchRecipes(String query, Callback callback) {
        new Thread(() -> {
            if (BuildConfig.SPOONACULAR_API_KEY == null || BuildConfig.SPOONACULAR_API_KEY.trim().isEmpty()) {
                callback.onError("Add spoonacular.apiKey to local.properties, rebuild, and try again.");
                return;
            }

            HttpURLConnection connection = null;
            try {
                Uri uri = Uri.parse("https://api.spoonacular.com/recipes/complexSearch")
                        .buildUpon()
                        .appendQueryParameter("number", "5")
                        .appendQueryParameter("query", query == null || query.trim().isEmpty() ? "thyroid friendly breakfast" : query.trim())
                        .appendQueryParameter("diet", "gluten free")
                        .appendQueryParameter("sort", "healthiness")
                        .appendQueryParameter("addRecipeInformation", "true")
                        .appendQueryParameter("addRecipeNutrition", "true")
                        .appendQueryParameter("excludeIngredients", "soy,tofu,tempeh,broccoli,cauliflower,cabbage,kale")
                        .appendQueryParameter("apiKey", BuildConfig.SPOONACULAR_API_KEY)
                        .build();

                connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();
                InputStream inputStream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String responseBody = readStream(inputStream);
                if (responseCode < 200 || responseCode >= 300) {
                    callback.onError("Recipe service returned " + responseCode + ".");
                    return;
                }

                callback.onSuccess(parseRecipes(responseBody));
            } catch (Exception exception) {
                callback.onError("Could not load recipes right now.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private List<RecipeSuggestion> parseRecipes(String rawJson) throws Exception {
        List<RecipeSuggestion> recipes = new ArrayList<>();
        JSONObject payload = new JSONObject(rawJson);
        JSONArray results = payload.optJSONArray("results");

        if (results == null) {
            return recipes;
        }

        for (int index = 0; index < results.length(); index++) {
            JSONObject item = results.optJSONObject(index);
            if (item == null) {
                continue;
            }

            String title = item.optString("title", "Recipe idea");
            int readyInMinutes = item.optInt("readyInMinutes", 0);
            String sourceUrl = item.optString("sourceUrl");
            String imageUrl = item.optString("image");
            String selenium = findNutrientValue(item, "Selenium");
            String zinc = findNutrientValue(item, "Zinc");

            String detail = "Ready in " + readyInMinutes + " min";
            if (!selenium.isEmpty() || !zinc.isEmpty()) {
                detail += " | Selenium: " + (selenium.isEmpty() ? "n/a" : selenium);
                detail += " | Zinc: " + (zinc.isEmpty() ? "n/a" : zinc);
            }

            recipes.add(new RecipeSuggestion(title, detail, sourceUrl, imageUrl));
        }

        return recipes;
    }

    private String findNutrientValue(JSONObject recipe, String nutrientName) {
        JSONObject nutrition = recipe.optJSONObject("nutrition");
        if (nutrition == null) {
            return "";
        }

        JSONArray nutrients = nutrition.optJSONArray("nutrients");
        if (nutrients == null) {
            return "";
        }

        for (int index = 0; index < nutrients.length(); index++) {
            JSONObject nutrient = nutrients.optJSONObject(index);
            if (nutrient != null && nutrientName.equalsIgnoreCase(nutrient.optString("name"))) {
                String amount = nutrient.optString("amount");
                String unit = nutrient.optString("unit");
                if (!amount.isEmpty()) {
                    return amount + unit;
                }
            }
        }

        return "";
    }

    private String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }
}
