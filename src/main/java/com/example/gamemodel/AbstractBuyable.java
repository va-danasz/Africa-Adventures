package com.example.gamemodel;

import com.example.viewmodel.BuyableView;

public abstract class AbstractBuyable implements Buyable {
    private transient BuyableView view;

    public void setView(BuyableView view) {
        this.view = view;
    }
    public BuyableView getView() {
        return view;
    }
}