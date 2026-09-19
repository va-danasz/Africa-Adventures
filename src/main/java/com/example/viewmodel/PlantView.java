package com.example.viewmodel;

import com.example.gamemodel.Plant;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PlantView implements BuyableView {
    private final Circle circle;

    public PlantView(Plant model) {
        this.circle = new Circle(model.getCoord().getX(), model.getCoord().getY(), 20, Color.GREEN);
    }

    public Circle getCircle() {
        return circle;
    }
}