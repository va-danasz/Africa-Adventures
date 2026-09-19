package com.example.viewmodel;

import com.example.gamemodel.ShopCategory;
import com.example.gamemodel.ShopItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class ShopCategoryView {
    private final ShopCategory model;
    private final VBox categoryBox;
    private Consumer<ShopItem> onItemSelected;

    public ShopCategoryView(ShopCategory model) {
        this.model = model;
        this.categoryBox = new VBox(10);
        createCategoryUI();
    }

    public void setOnItemSelected(Consumer<ShopItem> handler) {this.onItemSelected = handler; }

    /**
     * Generates the UI elements (buttons & labels) for each category.
     */
    private void createCategoryUI() {
        categoryBox.getStyleClass().add("shop-box");
        categoryBox.setPadding(new Insets(10));
        categoryBox.setAlignment(Pos.CENTER);

        Label label = new Label(model.getCategoryName());
        label.getStyleClass().add("label-price");
        categoryBox.getChildren().add(label);

        for (ShopItem item : model.getItems()) {
            Button buyButton = new Button(item.getName());
            buyButton.getStyleClass().add("button-menu");
            Label priceLabel = new Label("$ "+item.getPrice());
            priceLabel.getStyleClass().add("label-price");

            buyButton.setOnAction(e -> {
                if (onItemSelected != null) {
                    onItemSelected.accept(item);
                }
            });
            categoryBox.getChildren().addAll(buyButton, priceLabel);
        }

    }

    public VBox getCategoryBox() {
        return categoryBox;
    }
}