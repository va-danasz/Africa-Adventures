package com.example.viewmodel;

import com.example.gamemodel.Grass;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

public class GrassView extends PlantView {
    public GrassView(Grass model) {
        super(model);
        getCircle().setFill(Color.LIGHTGREEN);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/grass.png")));
        getCircle().setFill(new ImagePattern(img));
    }
}