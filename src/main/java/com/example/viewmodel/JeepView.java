package com.example.viewmodel;

import com.example.gamemodel.Jeep;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class JeepView implements BuyableView{
    private final Jeep model;
    private final Circle circle;

    public JeepView(Jeep model) {
        this.model = model;
        this.circle = new Circle(model.getCoord().getX(), model.getCoord().getY(), 20, Color.GRAY);

        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/jeep.jpg")));
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