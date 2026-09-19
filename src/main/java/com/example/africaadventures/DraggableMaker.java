package com.example.africaadventures;

import com.example.gamemodel.Coordinate;
import javafx.scene.Node;

public class DraggableMaker {
    private boolean shopActive = false;
    private double mouseAnchorX, mouseAnchorY;
    private Coordinate mouseLocation = new Coordinate(50, 50);

    /**
     * Creates a draggable circle to move, updates it's location.
     * @param node
     */
    public void makeDraggable(Node node) {
        if (!shopActive) {
            mouseAnchorX = node.getTranslateX();
            mouseAnchorY = node.getTranslateY();
        }
        else{
            node.setOnMousePressed(mouseEvent -> {
                mouseAnchorX = mouseEvent.getSceneX() - node.getTranslateX();
                mouseAnchorY = mouseEvent.getSceneY() - node.getTranslateY();

            });
            node.setOnMouseDragged(mouseEvent -> {
                node.setTranslateX(mouseEvent.getSceneX() - mouseAnchorX);
                node.setTranslateY(mouseEvent.getSceneY() - mouseAnchorY);
                mouseLocation = new Coordinate(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            });
        }
    }

    public void setShopActive(boolean shopActive) {
        this.shopActive = shopActive;
    }

    public Coordinate getMouseLocation() {
        return mouseLocation;
    }

    /**
     * Resets to a default position.
     */
    public void setDefaultMouseLocation() {
        mouseLocation = new Coordinate(50, 50);
    }
}
