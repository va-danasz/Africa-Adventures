package com.example.gamemodel;

import com.example.viewmodel.BuyableView;

public interface Buyable {
    Coordinate getCoord();
    CanBuy getType();
    double getPrice();
    double getVision();

    BuyableView getView();
    void setView(BuyableView viewComponent);
}
