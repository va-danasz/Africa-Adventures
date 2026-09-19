package com.example.viewmodel;

import com.example.gamemodel.Ranger;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class RangerView implements BuyableView{
    private final Ranger model;
    private final Circle circle;

    public RangerView(Ranger model) {
        this.model = model;
        this.circle = new Circle(model.getCoord().getX(), model.getCoord().getY(), 20, Color.CHOCOLATE);

        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/ranger.jpg")));
        circle.setFill(new ImagePattern(img));
    }

    public Circle getCircle() {
        return circle;
    }

    /**
     * Updates the object's position visually
     */
    public void updatePosition() {
        circle.setCenterX(model.getCoord().getX());
        circle.setCenterY(model.getCoord().getY());
    }
}