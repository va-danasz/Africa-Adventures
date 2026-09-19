package com.example.gamemodel;

import com.example.viewmodel.BuyableView;
import com.example.viewmodel.JeepView;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.example.gamemodel.Coordinate.calcDistance;

public class Jeep extends AbstractBuyable implements Serializable {
    private Coordinate coord;
    private final CanBuy type;
    private final double price;
    private int passengerNum = 0;
    private double speed = 0.9;
    private final double vision = 100;
    private transient GameModel gm;
    private transient Object returnLock = new Object();
    private boolean hasReturned = false;
    private transient JeepView view;
    private final Set<Animal> animalsSeen = new HashSet<>();
    private final TouristPipeline pipeline;

    public Jeep(Coordinate coord, double price, final TouristPipeline pipeline, GameModel gm) {
        this.type = CanBuy.JEEP;
        this.coord = coord;
        this.price = price;
        this.pipeline = pipeline;
        this.gm = gm;
    }

    public int getPassengerNum() {
        return passengerNum;
    }

    public Coordinate getCoord(Coordinate coord) {
        return coord;
    }

    public void setCoord(Coordinate coord) {
        this.coord = coord;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public double getVision() {
        return vision;
    }

    @Override
    public BuyableView getView() {
        return view;
    }

    @Override
    public void setView(BuyableView viewComponent) {
        this.view = (JeepView) viewComponent;
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

    /**
     * This method makes sure jeeps are only transporting tourist when they should:
     * at daylight, and we won't start the jeep while it hasn't returned yet
     * @param isOver current game state
     */
    public void beginDriving(AtomicBoolean isOver) {
        while (!isOver.get()) {
            if (gm.getIsNight().get()) {
                try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                continue;
            }

            drive();
            synchronized (returnLock) {
                while (!hasReturned) {
                    try { returnLock.wait(); } catch (InterruptedException e) { e.printStackTrace(); }
                }
                hasReturned = false;
            }
        }
    }

    /**
     * Changing between the jeep functionalities during driving:
     * choosing a random route in the graph,
     * when going back choosing the shortest path of size,
     * moving between coordinates
     */
    void drive(){
        Path currentPath = gm.getNewPath();
        int group = pipeline.tryToTake();
        gm.addVisitorCount(group);
        gm.changeMoney(group*gm.getTicketPrice());
        passengerNum = group;
        List<Integer> chosenPath = findRandomPath(currentPath.getAllPaths());

        animalsSeen.clear();
        for(Integer index : chosenPath){
            moveTo(currentPath.getReverseIndexMap().get(index));
        }


        if (coord.equals(gm.getExit())) {
            List<Integer> shortestPath = findShortestPath(currentPath.getAllPaths());
            calcSatisfaction();

            for (Integer index : shortestPath.reversed()) {
                moveTo(currentPath.getReverseIndexMap().get(index));
            }
        }

        synchronized (returnLock) {
            hasReturned = true;
            returnLock.notifyAll();
        }
    }

    /**
     * Moves the jeep towards its target coordinate
     * @param targetCoord the destination
     */
    void moveTo(Coordinate targetCoord) {
        if (targetCoord == null) {
            return;
        }

        Coordinate destination = new Coordinate(targetCoord.getX(), targetCoord.getY());
        while (true) {
            double dx = destination.getX() - this.coord.getX();
            double dy = destination.getY() - this.coord.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            checkForAnimals();
            if (dist < 4) {
                break;
            }

            double xSpeed = (dx / dist) * speed * gm.getSpeedModifier();
            double ySpeed = (dy / dist) * speed * gm.getSpeedModifier();

            this.coord.setX(this.coord.getX() + xSpeed);
            this.coord.setY(this.coord.getY() + ySpeed);

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        this.coord = destination;

    }

    /**
     * Checks if there are animals in the jeep's vision and store it in a Set
     */
    void checkForAnimals(){
        for (Animal obj : gm.getAnimals()) {
            double distance = calcDistance(coord, obj.getCoord());
            if (distance < vision) {
                animalsSeen.add(obj);
            }
        }
    }

    /**
     * @param paths All the paths
     * @return A randomly chosen path from the entrance to the exit
     */
    List<Integer> findRandomPath(Set<List<Integer>> paths) {
        if (paths == null || paths.isEmpty()) {
            return null;
        }

        List<List<Integer>> pathList = new ArrayList<>(paths);
        Random rand = new Random();
        return pathList.get(rand.nextInt(pathList.size()));
    }

    /**
     * @param paths All the paths
     * @return The shortest path leading from the exit to the entrance
     */
    List<Integer> findShortestPath(Set<List<Integer>> paths) {
        if (paths == null || paths.isEmpty()) {
            return null;
        }
        List<List<Integer>> pathList = new ArrayList<>(paths);
        List<Integer> shortestPath = null;
        int minSize = Integer.MAX_VALUE;
        for (List<Integer> path : pathList) {
            if (path.size() < minSize) {
                minSize = path.size();
                shortestPath = path;
            }
        }
        return shortestPath;
    }

    /**
     * Calculates the satisfaction with the number of seen animals, their type and the current ticket price
     */
    public void calcSatisfaction() {
        int countAnimals = animalsSeen.size();
        Set<String> animalTypes = new HashSet<>();
        for (Animal obj : animalsSeen) {
            animalTypes.add(obj.getClass().getSimpleName());
        }
        int countTypes = animalTypes.size();

        double animalWeight = 0.5;
        double typeWeight = 0.3;
        double priceWeight = 0.0002;

        double satisfaction = (countAnimals * animalWeight) + (countTypes * typeWeight) - (getPrice() * priceWeight);
        satisfaction = Math.max(0.0, satisfaction);
        gm.setSatisfactionMultiplier(satisfaction);

    }

    public void setGameModel(GameModel gm) {
        this.gm = gm;
    }

    public void setLock() {
        this.returnLock = new Object();
    }
}