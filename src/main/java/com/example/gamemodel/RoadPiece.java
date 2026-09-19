package com.example.gamemodel;

import com.example.viewmodel.BuyableView;
import com.example.viewmodel.RoadPieceView;
import java.io.Serializable;

public class RoadPiece extends AbstractBuyable implements Serializable {
    private final CanBuy type = CanBuy.PATH;
    private final double price;
    private final Coordinate endPoint1;
    private final Coordinate endPoint2;
    private transient RoadPieceView view;

    public RoadPiece(double price, Coordinate endPoint1, Coordinate endPoint2) {
        this.price = price;
        this.endPoint1 = endPoint1;
        this.endPoint2 = endPoint2;
    }

    @Override
    public double getVision() {
        return 0;
    }

    @Override
    public BuyableView getView() {
        return view;
    }

    @Override
    public void setView(BuyableView viewComponent) {
        this.view = (RoadPieceView) viewComponent;
    }

    @Override
    public Coordinate getCoord() {return null; }

    @Override
    public CanBuy getType() {return type; }

    @Override
    public double getPrice() {
        return price;
    }

    public Coordinate getEndPoint1() {
        return endPoint1;
    }

    public Coordinate getEndPoint2() {
        return endPoint2;
    }
}