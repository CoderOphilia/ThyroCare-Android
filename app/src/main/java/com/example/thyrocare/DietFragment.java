package com.example.thyrocare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.thyrocare.databinding.FragmentDietBinding;
import com.google.android.material.card.MaterialCardView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DietFragment extends Fragment {

    private FragmentDietBinding binding;
    private final SpoonacularClient spoonacularClient = new SpoonacularClient();
    private List<FoodDirectoryItem> allFoods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDietBinding.inflate(inflater, container, false);
        allFoods = createFoodDirectory();

        binding.etDietSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderFoodDirectory(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.btnLoadRecipes.setOnClickListener(view -> loadRecipes());

        renderFoodDirectory("");
        renderRecipeState("Gluten-free recipe ideas will appear here. Add a Spoonacular key to local.properties to enable live results.");
        return binding.getRoot();
    }

    private void renderFoodDirectory(String query) {
        binding.foodDirectoryContainer.removeAllViews();
        int resultCount = 0;

        for (FoodDirectoryItem item : allFoods) {
            if (item.matches(query)) {
                binding.foodDirectoryContainer.addView(createFoodCard(item));
                resultCount++;
            }
        }

        if (resultCount == 0) {
            binding.foodDirectoryContainer.addView(createEmptyText("No foods matched that search yet."));
        }
    }

    private void loadRecipes() {
        String query = binding.etDietSearch.getText() == null ? "" : binding.etDietSearch.getText().toString().trim();
        binding.recipeProgress.setVisibility(View.VISIBLE);
        renderRecipeState("Loading gluten-free recipe ideas...");

        spoonacularClient.fetchRecipes(query, new SpoonacularClient.Callback() {
            @Override
            public void onSuccess(List<RecipeSuggestion> recipes) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    binding.recipeProgress.setVisibility(View.GONE);
                    binding.recipeResultsContainer.removeAllViews();

                    if (recipes.isEmpty()) {
                        renderRecipeState("No gluten-free recipes came back for that search. Try a broader term like breakfast, salmon, or snack.");
                        return;
                    }

                    binding.tvRecipeStatus.setText("Showing " + recipes.size() + " gluten-free recipe ideas.");
                    for (RecipeSuggestion recipe : recipes) {
                        binding.recipeResultsContainer.addView(createRecipeCard(recipe));
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    binding.recipeProgress.setVisibility(View.GONE);
                    renderRecipeState(message);
                });
            }
        });
    }

    private void renderRecipeState(String message) {
        binding.recipeResultsContainer.removeAllViews();
        binding.tvRecipeStatus.setText(message);
    }

    private View createFoodCard(FoodDirectoryItem item) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dpToPx(12);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(18));
        cardView.setStrokeColor("Avoid".equals(item.getCategory()) ? 0xFFB04D63 : 0xFF4E8B63);
        cardView.setStrokeWidth(dpToPx(1));
        cardView.setCardBackgroundColor(0xFFFFFFFF);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        TextView title = new TextView(requireContext());
        title.setText(item.getName() + " - " + item.getCategory());
        title.setTextColor(0xFF5F3F63);
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        TextView summary = new TextView(requireContext());
        summary.setText(item.getSummary());
        summary.setTextColor(0xFF6F6573);
        summary.setPadding(0, dpToPx(8), 0, 0);

        TextView focus = new TextView(requireContext());
        focus.setText("Focus: " + item.getFocus());
        focus.setTextColor(0xFF5F3F63);
        focus.setPadding(0, dpToPx(8), 0, 0);

        container.addView(title);
        container.addView(summary);
        container.addView(focus);
        cardView.addView(container);
        return cardView;
    }

    private View createRecipeCard(RecipeSuggestion recipe) {
        MaterialCardView cardView = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dpToPx(14);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(22));
        cardView.setStrokeColor(0xFF8B6FB1);
        cardView.setStrokeWidth(dpToPx(1));
        cardView.setCardBackgroundColor(0xFFFFFFFF);
        cardView.setClickable(!recipe.getSourceUrl().isEmpty());
        cardView.setFocusable(!recipe.getSourceUrl().isEmpty());

        if (!recipe.getSourceUrl().isEmpty()) {
            cardView.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getSourceUrl()))));
        }

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);

        ImageView imageView = new ImageView(requireContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(180)
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundColor(0xFFEFE8F6);
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            loadRecipeImage(recipe.getImageUrl(), imageView);
        }
        container.addView(imageView);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        TextView badge = new TextView(requireContext());
        badge.setText("Gluten-free recipe");
        badge.setTextColor(0xFF4E8B63);
        badge.setTypeface(badge.getTypeface(), android.graphics.Typeface.BOLD);

        TextView title = new TextView(requireContext());
        title.setText(recipe.getTitle());
        title.setTextColor(0xFF5F3F63);
        title.setTextSize(17);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        title.setPadding(0, dpToPx(8), 0, 0);

        TextView detail = new TextView(requireContext());
        detail.setText(recipe.getDetail());
        detail.setTextColor(0xFF6F6573);
        detail.setPadding(0, dpToPx(8), 0, 0);

        TextView action = new TextView(requireContext());
        action.setText(recipe.getSourceUrl().isEmpty() ? "Recipe link unavailable" : "Tap card to open full recipe");
        action.setTextColor(recipe.getSourceUrl().isEmpty() ? 0xFF6F6573 : 0xFF8B6FB1);
        action.setPadding(0, dpToPx(10), 0, 0);
        action.setGravity(Gravity.START);

        content.addView(badge);
        content.addView(title);
        content.addView(detail);
        content.addView(action);

        container.addView(content);
        cardView.addView(container);
        return cardView;
    }

    private void loadRecipeImage(String imageUrl, ImageView imageView) {
        new Thread(() -> {
            try {
                InputStream inputStream = new URL(imageUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    private View createEmptyText(String text) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextColor(0xFF6F6573);
        textView.setPadding(0, dpToPx(8), 0, dpToPx(8));
        return textView;
    }

    private List<FoodDirectoryItem> createFoodDirectory() {
        List<FoodDirectoryItem> foods = new ArrayList<>();
        foods.add(new FoodDirectoryItem("Brazil nuts", "Friendly", "A selenium-rich snack that supports thyroid hormone metabolism.", "Selenium"));
        foods.add(new FoodDirectoryItem("Eggs", "Friendly", "Helpful protein source with selenium, iodine, and vitamin D.", "Protein and iodine"));
        foods.add(new FoodDirectoryItem("Greek yogurt", "Friendly", "Can support protein intake and calcium if dairy works for you.", "Protein"));
        foods.add(new FoodDirectoryItem("Salmon", "Friendly", "Omega-3 fats plus protein make this a strong thyroid-support meal choice.", "Omega-3 and selenium"));
        foods.add(new FoodDirectoryItem("Lentils", "Friendly", "Fiber and iron support energy when paired with thyroid-safe meals.", "Iron and fiber"));
        foods.add(new FoodDirectoryItem("Pumpkin seeds", "Friendly", "Easy zinc boost for snacks or salads.", "Zinc"));
        foods.add(new FoodDirectoryItem("Soy milk", "Avoid", "Soy may interfere with thyroid medication timing for some people.", "Separate from medication"));
        foods.add(new FoodDirectoryItem("Raw kale smoothie", "Avoid", "Large amounts of raw cruciferous vegetables can be less helpful for some thyroid patients.", "Prefer cooked crucifers"));
        foods.add(new FoodDirectoryItem("Tofu scramble", "Avoid", "Soy-heavy meals are worth reviewing with timing and tolerance in mind.", "Soy caution"));
        foods.add(new FoodDirectoryItem("Seaweed snacks", "Caution", "Very high iodine intake can be unhelpful if it swings too far.", "Iodine moderation"));
        foods.add(new FoodDirectoryItem("Cauliflower rice", "Caution", "Usually fine cooked in moderate amounts, but not ideal as a constant raw staple.", "Cook and moderate"));
        return foods;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
