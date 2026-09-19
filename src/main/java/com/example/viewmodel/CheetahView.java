package com.example.viewmodel;

import com.example.gamemodel.Cheetah;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

public class CheetahView extends AnimalView {
    public CheetahView(Cheetah model) {
        super(model);
        getCircle().setFill(Color.DARKORANGE);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/cheetah.jpg")));
        getCircle().setFill(new ImagePattern(img));
    }
}