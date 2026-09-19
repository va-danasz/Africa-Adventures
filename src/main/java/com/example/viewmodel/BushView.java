package com.example.viewmodel;

import com.example.gamemodel.Bush;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

public class BushView extends PlantView {
    public BushView(Bush model) {
        super(model);
        getCircle().setFill(Color.GREEN);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/bush.jpg")));
        getCircle().setFill(new ImagePattern(img));
    }
}