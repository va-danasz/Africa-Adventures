package com.example.gamemodel;

import com.example.viewmodel.BuyableView;
import com.example.viewmodel.PlantView;
import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Plant extends AbstractBuyable implements Serializable {
    private final Coordinate coord;
    private final CanBuy type;
    private final double price;
    private int quantity;
    private final int maxQuantity;
    private boolean isBeingEaten = false;
    private transient PlantView view;
    private GameModel model;

    public Plant(Coordinate coord, double price, int quantity, GameModel model) {
        this.coord = coord;
        this.price = price;
        this.type = CanBuy.PLANT;
        this.quantity = quantity;
        this.maxQuantity = quantity;
        this.model = model;

        ScheduledExecutorService regrowExecutor = Executors.newSingleThreadScheduledExecutor();
        regrowExecutor.scheduleAtFixedRate(() -> {
            if (!isBeingEaten && this.quantity < maxQuantity) {
                this.quantity++;
            }
        }, 5, 10, TimeUnit.SECONDS);
    }

    /**
     * Decreases the plant's quantity, and then gradually regrows it.
     */
    public void consume() {
        if (quantity > 1) {
            quantity--;
            isBeingEaten = true;
            Executors.newSingleThreadScheduledExecutor().schedule(() -> isBeingEaten = false, 5, TimeUnit.SECONDS);
        } else {
            model.removePlant(this);
        }

    }

    public void setModel(GameModel model) {this.model = model;}

    public int getQuantity() {
        return quantity;
    }

    @Override
    public double getVision() {return 50; }

    @Override
    public BuyableView getView() {
        return view;
    }

    @Override
    public void setView(BuyableView viewComponent) {
        this.view = (PlantView)viewComponent;
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
}