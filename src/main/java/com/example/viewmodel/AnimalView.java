package com.example.viewmodel;

import com.example.gamemodel.Animal;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AnimalView implements BuyableView {
    private final Animal model;
    private final Circle circle;

    public AnimalView(Animal model) {
        this.model = model;
        this.circle = new Circle(model.getCoord().getX(), model.getCoord().getY(), 20, Color.MAGENTA);
    }

    public Circle getCircle() {
        return circle;
    }
    
    public Animal getModel() {
        return model;
    }

    /**
     * Updates the object's position visually
     */
    public void updatePosition() {
        circle.setCenterX(model.getCoord().getX());
        circle.setCenterY(model.getCoord().getY());
    }
}
