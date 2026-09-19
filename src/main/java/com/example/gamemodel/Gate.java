package com.example.gamemodel;

import com.example.viewmodel.GateView;

import java.io.Serializable;

public class Gate implements Serializable {
    private final Coordinate coord;
    private final double size;
    private final boolean isEntrance;
    private transient GateView view;

    public Gate(Coordinate c, double size, boolean isEntrance) {
        this.coord = c;
        this.size = size;
        this.isEntrance = isEntrance;
    }

    public GateView getView() {
        return view;
    }

    public void setView(GateView view) {
        this.view = view;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public double getSize() {
        return size;
    }

    public boolean isEntrance() {
        return isEntrance;
    }
}