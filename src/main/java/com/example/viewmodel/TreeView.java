package com.example.viewmodel;

import com.example.gamemodel.Tree;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

public class TreeView extends PlantView {
    public TreeView(Tree model) {
        super(model);
        getCircle().setFill(Color.DARKGREEN);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/tree.jpg")));
        getCircle().setFill(new ImagePattern(img));
    }
}