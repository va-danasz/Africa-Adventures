package com.example.viewmodel;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

public interface BuyableView {
    Circle getCircle();

    default void setOnClickAction(EventHandler<MouseEvent> handler) {
        if (getCircle() != null) {
            getCircle().setOnMouseClicked(handler);
        }
    }
}
