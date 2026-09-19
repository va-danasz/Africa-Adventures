package com.example.viewmodel;

import com.example.gamemodel.Poacher;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class PoacherView {
    private final Poacher model;
    private final Circle circle;

    public PoacherView(Poacher model) {
        this.model = model;
        this.circle = new Circle(model.getCoord().getX(), model.getCoord().getY(), 20, Color.MAROON);

        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/poacher.jpg")));
        circle.setFill(new ImagePattern(img));
    }

    /**
     * Updates the object's position visually
     */
    public void updatePosition() {
        circle.setCenterX(model.getCoord().getX());
        circle.setCenterY(model.getCoord().getY());
    }

    public Circle getCircle() {return circle; }

    public void setOnClickAction(EventHandler<MouseEvent> handler) {
        if (getCircle() != null) {
            getCircle().setOnMouseClicked(handler);
        }
    }
}