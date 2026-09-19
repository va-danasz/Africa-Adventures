package com.example.gamemodel;

import com.example.viewmodel.ObstacleView;
import java.io.Serializable;
import java.util.ArrayList;

public class Obstacle implements Serializable {
    protected ArrayList<Coordinate> edges;
    protected Coordinate center;
    protected transient ObstacleView view;

    public Obstacle(ArrayList<Coordinate> edges, Coordinate center) {
        this.edges = edges;
        this.center = center;
    }

    public ObstacleView getView() {
        return view;
    }

    public void setView(ObstacleView view) {
        this.view = view;
    }

    public Coordinate getCenter() {
        return center;
    }
}