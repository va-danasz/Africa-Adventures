package com.example.viewmodel;

import com.example.gamemodel.Zebra;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

public class ZebraView extends AnimalView {
    public ZebraView(Zebra model) {
        super(model);
        getCircle().setFill(Color.BLACK);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/zebra.jpg")));
        getCircle().setFill(new ImagePattern(img));
    }
}