package com.example.gamemodel;

import java.io.Serializable;
import java.util.Objects;

public class Coordinate implements Serializable {
    private double x, y;

    public Coordinate() {
        this.x = 0;
        this.y = 0;
    }

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    /**
     * @param obj the other object
     * @return Whether this coordinate's values are equal to the other's
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinate that = (Coordinate) obj;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * @param targetCoord the taget coordinate
     * @param tolerance distance tolerance, the radius
     * @return Whether the distance to the target's position is less than or equal to the tolerance radius
     */
    public boolean distanceTo(Coordinate targetCoord, double tolerance) {
        double dx = targetCoord.getX() - this.getX();
        double dy = targetCoord.getY() - this.getY();
        return (dx * dx + dy * dy) <= tolerance*tolerance;
    }

    /**
     * @param a 1st coordinate
     * @param b 2nd coordinate
     * @return Calculates the distance between the two coordinates
     */
    public static double calcDistance(Coordinate a, Coordinate b){
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}


