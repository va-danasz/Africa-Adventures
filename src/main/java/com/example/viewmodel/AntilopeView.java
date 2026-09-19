package com.example.viewmodel;

import com.example.gamemodel.Antilope;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

public class AntilopeView extends AnimalView {
    public AntilopeView(Antilope model) {
        super(model);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/antilope.jpg")));
        getCircle().setFill(new ImagePattern(img));
    }
}