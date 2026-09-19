package com.example.gamemodel;

import com.example.viewmodel.BuyableView;
import com.example.viewmodel.PondView;
import java.io.Serializable;

public class Pond extends AbstractBuyable implements Serializable {
    private final Coordinate coord;
    private final CanBuy type = CanBuy.POND;
    private final double price;
    private int capacity;
    private transient PondView view;
    private transient GameModel model;

    public Pond(Coordinate coord, Double price, int capacity, GameModel model) {
        this.coord = coord;
        this.price = price;
        this.capacity = capacity;
        this.model = model;
    }

    @Override
    public double getVision() {return 75; }

    @Override
    public BuyableView getView() {
        return view;
    }

    @Override
    public void setView(BuyableView viewComponent) {
        this.view = (PondView)viewComponent;
    }

    @Override
    public Coordinate getCoord() {
        return coord;
    }

    @Override
    public CanBuy getType() {
        return type;
    }

    @Override
    public double getPrice() {
        return price;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Decreases the pond's current capacity.
     */
    public void drain() {
        if (capacity > 1) {
            capacity--;
        } else {
            model.removePond(this);
        }
    }

    public void setGameModel(GameModel gm) {this.model = gm; }
}
