package com.example.gamemodel;

import com.example.viewmodel.PoacherView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Poacher implements Serializable {
    private final Coordinate coord;
    private int hp;
    private double speed;
    private boolean isCaptured;
    private transient GameModel gm;
    private Coordinate targetCoord;
    private final ArrayList<Animal> targets;
    private final double vision;
    private boolean escaping;
    private Animal capturedAnimal;
    private boolean visibility;
    private transient PoacherView view;
    private final double sceneWidth;
    private final double sceneHeight;

    public Poacher(Coordinate coord, int hp, double speed, boolean isCaptured, GameModel gm, double w, double h) {
        this.coord = coord;
        this.hp = hp;
        this.speed = speed;
        this.isCaptured = isCaptured;
        this.gm = gm;
        this.vision = 200.0;
        this.targets = new ArrayList<>();
        this.escaping = false;
        this.sceneWidth = w;
        this.sceneHeight = h;
    }
    public boolean isVisibility() {return visibility; }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean getIsCaptured() {return isCaptured; }

    public void setCaptured(boolean captured) {isCaptured = captured; }

    /**
     * The core logic of poachers, they wander randomly unless they see an animal.
     * @param animals the poacher's prey list
     */
    public void hunt(ArrayList<Animal> animals) {
        cleanInvalidTargets();
        if (targets.isEmpty()) {
            if (targetCoord == null || this.coord.distanceTo(targetCoord, 5)) {
                setRandomWanderTarget();
            }
            moveTowards(targetCoord);
            for (Animal animal : animals) {
                double dx = coord.getX() - animal.getCoord().getX();
                double dy = coord.getY() - animal.getCoord().getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < this.vision && !isCaptured) {
                    if (!targets.contains(animal)) {
                        targets.add(animal);
                    }
                }
            }
            return;
        }

        Animal target = targets.getFirst();
        Coordinate targetPos = target.getCoord();

        if (coord.distanceTo(targetPos, 2*20)) {
            Random rand = new Random();
            if (rand.nextInt(2) == 0){
                gm.huntAnimal(target);
            } else {
                isCaptured = true;
                gm.captureAnimal(target, this);
            }
            targets.removeFirst();
        } else {
            moveTowards(targetPos);
        }
    }

    /**
     * Deletes already dead targets
     */
    private void cleanInvalidTargets() {
        targets.removeIf(animal -> animal == null || animal.isCaptured() || !gm.getAnimals().contains(animal));
    }

    /**
     * Captures the prey animal
     * @param animal the poacher's current prey
     */
    public void captureAnimal(Animal animal) {
        if (animal == null) return;
        targets.remove(animal);
    }

    /**
     * Implements the poacher's movement, uses simple vectors.
     * @param coord the target coordinate
     */
    private void moveTowards(Coordinate coord) {
        if (coord == null) return;

        double dx = coord.getX() - this.coord.getX();
        double dy = coord.getY() - this.coord.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist == 0) return;

        double xSpeed = (dx / dist) * speed * gm.getSpeedModifier();
        double ySpeed = (dy / dist) * speed * gm.getSpeedModifier();

        this.coord.setX(this.coord.getX() + xSpeed);
        this.coord.setY(this.coord.getY() + ySpeed);
    }

    /**
     * Generates a random target coordinate for wandering.
     */
    private void setRandomWanderTarget() {
        double randX = Math.random() * sceneWidth * 0.9 + sceneWidth * 0.05;
        double randY = Math.random() * sceneHeight * 0.9 + sceneHeight * 0.05;

        this.targetCoord = new Coordinate(randX, randY);
    }

    public boolean containsTarget(Animal target) {
        return targets.contains(target);
    }

    /**
     * Leaves the scene to the nearest edge
     * @return
     */
    public boolean escape() {
        double x = coord.getX();
        double y = coord.getY();

        if (x - 10 < 0 || x >= sceneWidth - 40 || y - 10 < 0 || y >= sceneHeight - 40) {
            return true;
        }

        double rightDist = sceneWidth - x;
        double bottomDist = sceneHeight - y;

        double minDist = Math.min(Math.min(x, rightDist), Math.min(y, bottomDist));
        Coordinate escapeCoord = new Coordinate();

        if (minDist == x) {
            escapeCoord.setX(0);
            escapeCoord.setY(y);
        } else if (minDist == rightDist) {
            escapeCoord.setX(sceneWidth);
            escapeCoord.setY(y);
        } else if (minDist == y) {
            escapeCoord.setX(x);
            escapeCoord.setY(0);
        } else {
            escapeCoord.setX(x);
            escapeCoord.setY(sceneHeight);
        }

        moveTowards(escapeCoord);
        return false;
    }

    public boolean isEscaping() {
        return escaping;
    }

    public void setEscaping(boolean escaping) {
        this.escaping = escaping;
    }

    public void setCapturedAnimal(Animal animal) {
        this.capturedAnimal = animal;
    }

    public Animal getCapturedAnimal() {
        return capturedAnimal;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, hp);
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @param rangers the list of rangers
     * @return Whether the poacher is near to any of the rangers
     */
    public boolean isVisibleForRangers(ArrayList<Ranger> rangers) {
        if (rangers == null || rangers.isEmpty()){
            return false;
        }

        for (Ranger ranger : rangers) {
            double dx = this.getCoord().getX() - ranger.getCoord().getX();
            double dy = this.getCoord().getY() - ranger.getCoord().getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < ranger.getVision() && !isCaptured) {
                return true;
            }
        }
        return false;
    }

    public void setView(PoacherView view) {this.view = view; }

    public PoacherView getView() {return view; }

    public void setGameModel(GameModel gm) {this.gm = gm; }
}