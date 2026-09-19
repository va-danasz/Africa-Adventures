package com.example.viewmodel;

import com.example.gamemodel.River;
import javafx.scene.paint.Color;

public class RiverView extends ObstacleView {
    public RiverView(River model) {
        super(model);
        this.circle.setFill(Color.LIGHTBLUE);
    }
}