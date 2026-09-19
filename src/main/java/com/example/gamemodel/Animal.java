package com.example.gamemodel;

import com.example.viewmodel.AnimalView;
import com.example.viewmodel.BuyableView;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static com.example.gamemodel.Coordinate.calcDistance;

public class Animal extends AbstractBuyable implements Serializable {
    private transient AnimalView view;
    private final Coordinate coord;
    private final CanBuy type;
    private final double price;
    private double vision = 200.0;
    private double age;
    private Coordinate targetCoord;
    private final boolean isCarnivore;
    private boolean isChipped;
    private double speed = 1.0;
    private Animal leader;
    private transient GameModel gameModel;
    private final ArrayList<Coordinate> drinkingPlaces = new ArrayList<>();
    private final ArrayList<Coordinate> eatingPlaces = new ArrayList<>();
    private final ArrayList<Animal> targets = new ArrayList<>();
    private int eatingCooldown = 0;
    private int drinkingCooldown = 0;
    private int reproduceCooldown = 10;
    private boolean captured = false;
    private boolean isEating = false;
    private boolean isDrinking = false;
    private Animal followingLeader = null;

    public Animal(Coordinate coord, Double price, GameModel gameModel, boolean isCarnivore) {
        this.coord = coord;
        this.type = CanBuy.ANIMAL;
        this.price = price;
        this.isCarnivore = isCarnivore;
        this.age = 0.0;
        this.targetCoord = null;
        this.isChipped = false;
        this.gameModel = gameModel;
    }

    /**
     * If the animal, and the other animal reproduceCooldown is 0 or lower, it executes the createAnimal function, and resets the cooldowns
     * @param other The other animal
     */
    public boolean reproduce(Animal other) {
        if (this.canReproduceWith(other) && other.reproduceCooldown <= 0 && this.reproduceCooldown <= 0){
            this.reproduceCooldown = 40;
            other.reproduceCooldown = 40;
            return true;
        }
        return false;
    }

    private boolean canReproduceWith(Animal other) {
        return this.isMature() && other.isMature() && this.type == other.type;
    }

    private boolean isMature() {
        return age > 1;
    }

    /**
     * Making the animal move towards its target coordinate while keep updating ArrayLists to save
     * coordinates of ponds and plants. Also checking the capacity of them and whether they became extinct.
     * @return Whether the animal reached the target position or not
     */
    public boolean migrate() {
        if (isDrinking || isEating || targetCoord == null) return false;
        if (hasArrived()) return true;

        double dx = targetCoord.getX() - coord.getX();
        double dy = targetCoord.getY() - coord.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        double xSpeed = (dx / dist) * speed * gameModel.getSpeedModifier();
        double ySpeed = (dy / dist) * speed * gameModel.getSpeedModifier();

        coord.setX(coord.getX() + xSpeed);
        coord.setY(coord.getY() + ySpeed);

        for (Buyable obj : gameModel.getObjects()) {
            double distance = calcDistance(coord, obj.getCoord());
            if (distance < vision) {
                if (obj.getType() == CanBuy.POND && unknownPlace(obj.getCoord(), drinkingPlaces)) {
                    drinkingPlaces.add(obj.getCoord());
                } else if (obj.getType() == CanBuy.PLANT && !isCarnivore && unknownPlace(obj.getCoord(), eatingPlaces)) {
                    eatingPlaces.add(obj.getCoord());
                }
            }
        }

        if (hasArrived()) {
            Optional<Coordinate> nearbyDrinkSource = drinkingPlaces.stream()
                    .filter(c -> calcDistance(coord, c) < 65)
                    .findFirst();

            Optional<Coordinate> nearbyFoodSource = eatingPlaces.stream()
                    .filter(c -> calcDistance(coord, c) < 45)
                    .findFirst();

            if (nearbyDrinkSource.isPresent()) {
                Coordinate source = nearbyDrinkSource.get();
                boolean stillExists = gameModel.getPonds().stream()
                        .anyMatch(p -> calcDistance(p.getCoord(), source) < 1);

                if (!stillExists) {
                    drinkingPlaces.remove(source);
                    targetCoord = null;
                    return false;
                }

                if (!isDrinking && drinkingCooldown <= 0) {
                    startDrinking();
                }
            }

            if (nearbyFoodSource.isPresent()) {
                Coordinate source = nearbyFoodSource.get();
                boolean stillExists = gameModel.getPlants().stream()
                        .anyMatch(p -> calcDistance(p.getCoord(), source) < 1);

                if (!stillExists) {
                    eatingPlaces.remove(source);
                    targetCoord = null;
                    return false;
                }

                if (!isEating && eatingCooldown <= 0) {
                    startEating();
                }
            }
        }

        return hasArrived();
    }

    /**
     * Starts the drinking process, waits at the pond.
     */
    private void startDrinking() {
        isDrinking = true;
        gameModel.getPonds().stream().filter(p -> p.getCoord().distanceTo(coord, 60)).findFirst().ifPresent(Pond::drain);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            isDrinking = false;
            drinkingCooldown = 20;
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * Starts the herbivores' eating process, waits at the plant.
     */
    private void startEating() {
        isEating = true;
        gameModel.getPlants().stream().filter(p -> p.getCoord().distanceTo(coord, 45)).findFirst().ifPresent(Plant::consume);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            isEating = false;
            eatingCooldown = 30;
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * @return Whether the animal is close enough to its target coordinate
     */
    public boolean hasArrived() {
        return targetCoord != null && calcDistance(coord, targetCoord) < 20;
    }

    /**
     * Helper function, for visual arrival at the target coordinate
     * @param objectCenter the target coordinate
     * @param objectRadius the target's radius
     * @return The coordinate, where the animal has to stop.
     */
    public Coordinate calculateEdgeTarget(Coordinate objectCenter, double objectRadius) {
        double dx = objectCenter.getX() - coord.getX();
        double dy = objectCenter.getY() - coord.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) return objectCenter;
        double offsetX = objectCenter.getX() - (dx / distance) * objectRadius;
        double offsetY = objectCenter.getY() - (dy / distance) * objectRadius;
        return new Coordinate(offsetX, offsetY);
    }

    /**
     * Executes, when the Carnivore hunts down the prey
     * @param prey The Herbivore animal
     */
    public void huntDown(Animal prey) {
        double dx = prey.getCoord().getX() - this.coord.getX();
        double dy = prey.getCoord().getY() - this.coord.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;
        double xSpeed = (dx / dist) * speed * gameModel.getSpeedModifier();
        double ySpeed = (dy / dist) * speed * gameModel.getSpeedModifier();
        this.coord.setX(this.coord.getX() + xSpeed);
        this.coord.setY(this.coord.getY() + ySpeed);
    }

    /**
     * Places the animals around a leader in a circle if there's a place for them
     * @param leader the leader animal
     * @param index counter value, to see if the circle around the leader isn't full yet
     */
    public void followLeaderWithOffset(Animal leader, int index) {
        if (leader == null) return;
        double dx = this.coord.getX() - leader.coord.getX();
        double dy = this.coord.getY() - leader.coord.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        if(dist < this.vision && index < 9) {
            Coordinate coordLeader = leader.getCoord();
            double angle = Math.toRadians(index * 45);
            double radius = 70;
            double offsetX = Math.cos(angle) * radius;
            double offsetY = Math.sin(angle) * radius;

            targetCoord = new Coordinate(coordLeader.getX() + offsetX, coordLeader.getY() + offsetY);
            followingLeader = leader;
        }
        else{
            followingLeader = null;
        }

    }

    public double getVision() { return vision; }
    public void setVision(double vision) { this.vision = vision; }
    public boolean isCarnivore() { return isCarnivore; }
    public ArrayList<Animal> getTargets() { return targets; }
    public void addTarget(Animal animal) { targets.add(animal); }
    public void removeTarget(Animal animal) { targets.remove(animal); }
    public Coordinate getCoord() { return coord; }
    public CanBuy getType() { return type; }
    public double getPrice() { return price; }
    public boolean isEating() { return isEating; }
    public boolean isDrinking() { return isDrinking; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public boolean isChipped() { return isChipped; }
    public int getDrinkingCooldown() { return drinkingCooldown; }
    public int getEatingCooldown() { return eatingCooldown; }
    public void resetDrinkingForLeader() { this.drinkingCooldown = 0; }
    public void resetEatingForLeader() { this.eatingCooldown = 0; }
    public Coordinate getTargetCoord() { return targetCoord; }
    public void setTargetCoord(Coordinate targetCoord) { this.targetCoord = targetCoord; }
    public Animal getLeader() { return leader; }
    public void setLeader() { this.leader = this; }
    public boolean isTooOld() {
        return 2.4 * 5 - age < 0; }

    /**
     * The natural aging of the animals, it handles reproduction, drinking and eating timers.
     */
    public void aging() {
        this.age += 0.1;
        if (reproduceCooldown > 0 && isMature()) reproduceCooldown--;
        if (drinkingCooldown > 0) drinkingCooldown--;
        if (eatingCooldown > 0) eatingCooldown--;
    }

    public boolean sameCoord(Coordinate a, Coordinate b) {
        return a.getX() == b.getX() && a.getY() == b.getY();
    }

    /**
     * Determines whether a given coordinate has not been visited or is not present
     * within the list of known places.
     *
     * @param objCoord The coordinate to be checked.
     * @param places The list of known places represented as coordinates.
     * @return true if the coordinate is not present in the list of known places.
     */
    public boolean unknownPlace(Coordinate objCoord, ArrayList<Coordinate> places) {
        return !places.contains(objCoord);
    }

    @Override
    public void setView(BuyableView viewComponent) {
        this.view = (AnimalView) viewComponent;
    }

    public AnimalView getView() {
        return view;
    }

    public void setChipped(boolean b) {
        this.isChipped = b;
    }

    public void move() {}

    /**
     * Checks if the animal collided with an obstacle, altering its speed and vision
     */
    public void collidedObstacle() {
        boolean collided = false;
        Obstacle typeObstacle = null;
        for (Obstacle obstacle : gameModel.getObstacles()){
            if (isTouchingObstacle()){
                collided = true;
                typeObstacle = obstacle;
                break;
            }
        }
        if (collided) {
            if (typeObstacle instanceof Hill){
                this.setVision(200.0);
            }
            this.setSpeed(0.6);
        }
        else {
            this.setVision(100.0);
            this.setSpeed(1.0);
        }
    }

    private boolean isTouchingObstacle() {
        return false;
    }

    /**
     * generates a random target coordinate on the scene
     * @param width scene width
     * @param height scene height
     */
    public void createRandomTarget(double width, double height) {
        Random rand = new Random();
        double x = rand.nextDouble(width * 0.05, width * 0.95);
        double y = rand.nextDouble(height * 0.05, height * 0.95);
        this.targetCoord = new Coordinate(x, y);
    }

    public boolean isCaptured() {
        return captured;
    }

    public String getAge(){
        double numericAge = Double.parseDouble(String.valueOf(age));
        return String.format(Locale.US,"%.2f", numericAge);
    }

    public Animal getFollowingLeader() {
        return followingLeader;
    }

    public void setGameModel(GameModel gm) {
        this.gameModel = gm;
    }

    public ArrayList<Coordinate> getDrinkingPlaces() {
        return drinkingPlaces;
    }

    public ArrayList<Coordinate> getEatingPlaces() {
        return eatingPlaces;
    }

    public int getReproduceCooldown() {
        return reproduceCooldown;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }
}
