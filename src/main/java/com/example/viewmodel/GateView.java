package com.example.viewmodel;

import com.example.gamemodel.Gate;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class GateView {
    private final Rectangle rect;

    public GateView(Gate model) {
        this.rect = new Rectangle(model.getCoord().getX(), model.getCoord().getY(), model.getSize(), model.getSize());

        Image img = new Image(String.valueOf(getClass().getResource(
                model.isEntrance() ? "/com/example/africaadventures/images/entrance.png"
                        : "/com/example/africaadventures/images/exit.png")));
        rect.setFill(new ImagePattern(img));
    }

    public Rectangle getRect() {
        return rect;
    }
}