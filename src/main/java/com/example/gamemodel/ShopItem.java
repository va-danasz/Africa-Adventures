package com.example.gamemodel;

import java.io.Serializable;

public class ShopItem implements Serializable {
    private final String name;
    private final double price;

    public ShopItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

