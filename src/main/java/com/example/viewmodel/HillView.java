package com.example.viewmodel;

import com.example.gamemodel.Hill;
import javafx.scene.paint.Color;

public class HillView extends ObstacleView {
    public HillView(Hill model) {
        super(model);
        this.circle.setFill(Color.GRAY);
        this.circle.setRadius(30);
    }
}