package com.example.viewmodel;

import com.example.gamemodel.Pond;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class PondView implements BuyableView {
    private final Circle circle;

    public PondView(Pond model) {
        this.circle = new Circle(model.getCoord().getX(), model.getCoord().getY(), 40, Color.BLUE);

        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/pond.jpg")));
        circle.setFill(new ImagePattern(img));
    }

    public Circle getCircle() {
        return circle;
    }
}
