package com.example.viewmodel;

import com.example.gamemodel.RoadPiece;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;

public class RoadPieceView implements BuyableView {
    private final RoadPiece model;
    private final Rectangle rect;

    /**
     * places a rectangle as the visual representation of the road
     * @param model the road piece's model
     */
    public RoadPieceView(RoadPiece model) {
        this.model = model;
        double x1 = model.getEndPoint1().getX();
        double y1 = model.getEndPoint1().getY();
        double x2 = model.getEndPoint2().getX();
        double y2 = model.getEndPoint2().getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.hypot(dx, dy);
        double angle = Math.toDegrees(Math.atan2(dy, dx));

        rect = new Rectangle(length, 30);
        rect.setTranslateX((x1 + x2) / 2 - length / 2);
        rect.setTranslateY((y1 + y2) / 2 - 5);

        rect.setRotate(angle);
        rect.setViewOrder(10);
        rect.setArcWidth(50);
        rect.setArcHeight(50);
        Image img = new Image(String.valueOf(getClass().getResource("/com/example/africaadventures/images/roadpiece.jpg")));
        ImagePattern pattern = new ImagePattern(img, 0, 0, img.getWidth(), img.getHeight(), false);
        rect.setFill(pattern);
    }

    public Rectangle getRectangle() {
        return rect;
    }

    @Override
    public Circle getCircle() {
        return null;
    }

    public RoadPiece getModel() {
        return model;
    }
}
