package com.example.viewmodel;

import com.example.gamemodel.Obstacle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ObstacleView {
    protected final Circle circle;

    public ObstacleView(Obstacle model) {
        this.circle = new Circle(model.getCenter().getX(), model.getCenter().getY(), 20, Color.MAGENTA);
    }

    public Circle getCircle() {
        return circle;
    }
}