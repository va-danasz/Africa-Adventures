package com.example.gamemodel;

import com.example.viewmodel.BuyableView;
import com.example.viewmodel.RangerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Ranger extends AbstractBuyable implements Serializable {
    private final Coordinate coord;
    private final CanBuy type = CanBuy.RANGER;
    private final double price;
    private final double vision = 200.0;
    private double speed;
    private int hp = 100;
    private final double baseSpeed = 1.0;
    private boolean isInTargetingMode = false;
    private Coordinate targetCoord;
    private final ArrayList<Animal> targets = new ArrayList<>();
    private final ArrayList<Poacher> poacherTargets = new ArrayList<>();
    private transient RangerView view;
    private final double sceneWidth;
    private final double sceneHeight;
    private transient GameModel gm;

    public Ranger(Coordinate coord, double price, GameModel gm, double w, double h) {
        this.coord = coord;
        this.price = price;
        this.gm = gm;
        this.speed = baseSpeed;
        this.sceneWidth = w;
        this.sceneHeight = h;
    }

    /**
     * The core logic of rangers, their priority: hunt animals, hunt poachers, random wandering.
     */
    public void hunt() {
        battlePoachers();
        cleanInvalidTargets();

        double huntingSpeed = baseSpeed * 2;
        if (!targets.isEmpty()) {
            Animal target = targets.getFirst();
            speed = huntingSpeed;
            targetCoord = target.getCoord();
            if (coord.distanceTo(targetCoord, 2 * 20)) {
                if (!target.isCaptured()) {
                    gm.changeMoney(target.getPrice() * 0.75);
                    gm.huntAnimal(target);
                }
                targets.removeFirst();
                speed = baseSpeed;
            } else {
                moveTowards(targetCoord);
            }
            return;
        }

        poacherTargets.removeIf(p -> p == null || p.getHp() <= 0 || !gm.getPoachers().contains(p));
        if (!poacherTargets.isEmpty()) {
            Poacher poach = poacherTargets.getFirst();
            Coordinate target = poach.getCoord();
            speed = huntingSpeed;
            moveTowards(target);
            return;
        }

        speed = baseSpeed;
        if (targetCoord == null || coord.distanceTo(targetCoord, 20)) {
            setRandomWanderTarget();
        }
        moveTowards(targetCoord);
    }

    /**
     * If a ranger gets close enough to a poacher, they start a battle. They shot at each other, then one of them dies.
     * After death, the method cleans up the defeated.
     */
    private void battlePoachers() {
        ArrayList<Poacher> toRemove = new ArrayList<>();
        Random rand = new Random();

        for (Poacher poach : gm.getPoachers()) {
            double dx = poach.getCoord().getX() - coord.getX();
            double dy = poach.getCoord().getY() - coord.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < this.vision / 2) {
                int rangerDamage = rand.nextInt(15);
                int poacherDamage = rand.nextInt(10);

                this.hp -= poacherDamage;
                poach.setHp(poach.getHp() - rangerDamage);

                if (this.hp < 1) {
                    gm.markRanger(this);
                    if (poach.getHp() < 1) {
                        poacherTargets.remove(poach);
                        toRemove.add(poach);
                    }
                    break;
                }
                if (poach.getHp() < 1) {
                    poacherTargets.remove(poach);
                    toRemove.add(poach);
                    getBounty();
                }
            }
        }

        toRemove.forEach(gm::removePoacher);
    }

    /**
     * When a ranger kills a poacher, the player gets the poacher bounty.
     * Updates the model's money by adding the poacher bounty.
     */
    private void getBounty() {
        double poacherBounty = 2000;
        gm.changeMoney(poacherBounty);
    }

    /**
     * Deletes already dead targets
     */
    void cleanInvalidTargets() {
        targets.removeIf(animal -> animal == null || animal.isCaptured() || !gm.getAnimals().contains(animal));
    }

    /**
     * Implements the ranger's movement, uses simple vectors.
     *
     * @param target the target coordinate
     */
    private void moveTowards(Coordinate target) {
        if (target == null) return;

        double dx = target.getX() - coord.getX();
        double dy = target.getY() - coord.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        double step = Math.min(speed * gm.getSpeedModifier(), dist);
        double xSpeed = (dx / dist) * step;
        double ySpeed = (dy / dist) * step;

        coord.setX(coord.getX() + xSpeed);
        coord.setY(coord.getY() + ySpeed);
    }

    /**
     * Generates a random target coordinate for wandering.
     */
    private void setRandomWanderTarget() {
        double randX = Math.random() * sceneWidth * 0.9 + sceneWidth * 0.05;
        double randY = Math.random() * sceneHeight * 0.9 + sceneHeight * 0.05;
        this.targetCoord = new Coordinate(randX, randY);
    }

    /**
     * Adds the animal to its target list if it's not already in it
     *
     * @param a The seen animal
     */
    public void putAnimalNotAlreadyHunting(Animal a) {
        if (!this.targets.contains(a)) {
            this.targets.add(a);
        }
    }

    @Override
    public double getVision() {
        return vision;
    }

    public RangerView getView() {
        return this.view;
    }

    @Override
    public void setView(BuyableView viewComponent) {
        this.view = (RangerView) viewComponent;
    }

    @Override
    public Coordinate getCoord() {
        return coord;
    }

    @Override
    public CanBuy getType() {
        return type;
    }

    @Override
    public double getPrice() {
        return price;
    }

    public Coordinate getTargetCoord() {
        return targetCoord;
    }

    public ArrayList<Animal> getTargets() {
        return targets;
    }

    public ArrayList<Poacher> getPoacherTargets() {
        return poacherTargets;
    }

    public boolean isInTargetingMode() {
        return isInTargetingMode;
    }

    public void setTargetingMode(boolean enabled) {
        this.isInTargetingMode = enabled;
    }

    public static double getSalary() {return 500; }

    public int getHp() {return hp; }

    public void setGameModel(GameModel gm) {
        this.gm = gm;
    }
}
