package com.example.gamemodel;

import com.example.viewmodel.ShopCategoryView;
import java.io.Serializable;
import java.util.ArrayList;

public class ShopCategory implements Serializable {
    private final String categoryName;
    private final ArrayList<ShopItem> items;
    private transient ShopCategoryView view;

    public ShopCategory(String categoryName, ArrayList<ShopItem> items) {
        this.categoryName = categoryName;
        this.items = items;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public ArrayList<ShopItem> getItems() {
        return items;
    }

    public void setView(ShopCategoryView view) {
        this.view = view;
    }

    public ShopCategoryView getView() {
        return view;
    }
}