package com.example.viewmodel;

import com.example.gamemodel.Lion;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

public class LionView extends AnimalView {
    public LionView(Lion model) {
        super(model);
        getCircle().setFill(Color.YELLOW);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/lion.jpg")));
        getCircle().setFill(new ImagePattern(img));
    }
}