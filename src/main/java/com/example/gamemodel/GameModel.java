package com.example.gamemodel;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameModel implements Serializable {
    private final ArrayList<Buyable> objects = new ArrayList<>();
    private final Set<Coordinate> dotCoords = new HashSet<>();
    private ArrayList<Animal> animals = new ArrayList<>();
    private final ArrayList<Animal> groupLeaders = new ArrayList<>();
    private final ArrayList<Plant> plants = new ArrayList<>();
    private final ArrayList<Ranger> rangers = new ArrayList<>();
    private final ArrayList<Ranger> markedRangers = new ArrayList<>();
    private final ArrayList<Poacher> poachers = new ArrayList<>();
    private final ArrayList<Pond> ponds = new ArrayList<>();
    private final ArrayList<Path> roads = new ArrayList<>();
    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private final ArrayList<Jeep> jeeps = new ArrayList<>();
    private final int[] goalList = new int[]{0, 0, 0, 0};
    private int visitorCount = 0;
    private Coordinate exit;
    private Difficulty currentDifficulty;
    private double currentSpeed = 1.0;
    private transient AtomicBoolean isNight;
    private double currentMoney = 4000;
    private int ticketPrice = 1000;
    private double width = 1000;
    private double height = 1000;
    private Path currentPath = null;
    private double satisfactionMultiplier = 1.0;
    private int timeCounter = 0;

    private final ArrayList<Animal> animalsToRemove = new ArrayList<>();
    private final ArrayList<Ranger> rangersToRemove = new ArrayList<>();
    private final ArrayList<Poacher> poachersToRemove = new ArrayList<>();
    private final ArrayList<Plant> plantsToRemove = new ArrayList<>();
    private final ArrayList<Pond> pondsToRemove = new ArrayList<>();

    public GameModel() {
    }

    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public ArrayList<Ranger> getRangersToRemove() {
        return rangersToRemove;
    }

    public ArrayList<Animal> getAnimalsToRemove() {
        return animalsToRemove;
    }

    public ArrayList<Poacher> getPoachersToRemove() {
        return poachersToRemove;
    }

    public ArrayList<Plant> getPlantsToRemove() {
        return plantsToRemove;
    }

    public ArrayList<Pond> getPondsToRemove() {
        return pondsToRemove;
    }

    public void setSize(double w, double h) {
        this.width = w;
        this.height = h;
    }

    public void setTimeCounter(int timeCounter) {
        this.timeCounter = timeCounter;
    }

    public int getTimeCounter() {
        return timeCounter;
    }

    public void setExit(Coordinate exit) {
        this.exit = exit;
    }

    public ArrayList<Buyable> getObjects() {
        return objects;
    }

    public Set<Coordinate> getDotCoords() {
        return dotCoords;
    }

    public Coordinate getExit() {
        return exit;
    }

    public ArrayList<Animal> getAnimals() {
        return animals;
    }

    public ArrayList<Plant> getPlants() {
        return plants;
    }

    public ArrayList<Ranger> getRangers() {
        return rangers;
    }

    public ArrayList<Poacher> getPoachers() {
        return poachers;
    }

    public ArrayList<Pond> getPonds() {
        return ponds;
    }

    public ArrayList<Path> getRoads() {
        return roads;
    }

    synchronized public void setSatisfactionMultiplier(double multiplier) {
        this.satisfactionMultiplier = multiplier;
    }

    synchronized public double getSatisfactionMultiplier() {
        return satisfactionMultiplier;
    }

    public double getSpeedModifier() {
        return currentSpeed;
    }

    public void setSpeedModifier(double speed) {
        this.currentSpeed = speed;
    }

    public void addVisitorCount(int visitor) {
        this.visitorCount += visitor;
    }

    /**
     * At the start of the game it generates some pond and plants
     *
     * @param rand   random number
     * @param width  bound's width
     * @param height bound's height
     * @return the generated object
     */
    public Buyable generateObject(Random rand, double width, double height) {
        Buyable item = null;
        boolean valid = false;

        int attempts = 0;
        while (!valid && attempts < 100) {
            attempts++;

            double coord_x = Math.round(rand.nextDouble(width * 0.1, width * 0.9));
            double coord_y = Math.round(rand.nextDouble(height * 0.1, height * 0.8));
            Coordinate coord = new Coordinate(coord_x, coord_y);
            int rand_num = rand.nextInt(4);

            item = switch (rand_num) {
                case 0 -> new Pond(coord, 1000.0, 10, this);
                case 1 -> new Grass(coord, 100.0, 3, this);
                case 2 -> new Bush(coord, 200.0, 3, this);
                case 3 -> new Tree(coord, 300.0, 3, this);
                default -> null;
            };

            valid = true;
            for (Buyable existing : objects) {
                double dx = item.getCoord().getX() - existing.getCoord().getX();
                double dy = item.getCoord().getY() - existing.getCoord().getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < item.getVision() + existing.getVision()) {
                    valid = false;
                    break;
                }
            }
        }

        if (valid) {
            switch (item.getType()) {
                case CanBuy.POND -> ponds.add((Pond) item);
                case CanBuy.PLANT -> plants.add((Plant) item);
            }
            objects.add(item);
        }

        return item;
    }

    /**
     * When animals reproduce, they create a new instance of the same type
     *
     * @param parent the parent animal
     * @return the new animal
     */
    public Animal createAnimal(Animal parent) {
        Coordinate leaderCoord = parent.getCoord();
        double offsetX = leaderCoord.getX() + Math.random() * 40 - 20;
        double offsetY = leaderCoord.getY() + Math.random() * 40 - 20;

        Animal offspring = null;
        Coordinate newCoord = new Coordinate(offsetX, offsetY);

        if (parent instanceof Antilope) {
            offspring = new Antilope(newCoord, parent.getPrice(), this);
        } else if (parent instanceof Cheetah) {
            offspring = new Cheetah(newCoord, parent.getPrice(), this);
        } else if (parent instanceof Lion) {
            offspring = new Lion(newCoord, parent.getPrice(), this);
        } else if (parent instanceof Zebra) {
            offspring = new Zebra(newCoord, parent.getPrice(), this);
        }

        if (offspring != null) {
            offspring.setSpeed(parent.getSpeed());
        }
        return offspring;
    }

    /**
     * @return the value corresponds to the current game state: win, lose, or none
     */
    public int gameOver() {
        int state = 0;
        if (timeCounter > 1) {
            if (animals.isEmpty() || currentMoney <= 0) state = 1;
            else if (win()) state = 2;
        }
        return state;
    }

    /**
     * Check all the conditions if the player meets all the conditions to win the game.
     */
    public boolean win() {
        final int multiplier = 24 * 30;
        int diffOrdinal = currentDifficulty.ordinal();
        int carnivoreCount = (int) animals.stream().filter(a -> a instanceof Carnivore).count();
        int herbivoreCount = (int) animals.stream().filter(a -> a instanceof Herbivore).count();

        if (visitorCount >= DifficultyGoals.getVisitor(diffOrdinal)) goalList[0]++;
        else goalList[0] = 0;
        if (currentMoney >= DifficultyGoals.getMoney(diffOrdinal)) goalList[1]++;
        else goalList[1] = 0;
        if (carnivoreCount >= DifficultyGoals.getCarnivore(diffOrdinal)) goalList[2]++;
        else goalList[2] = 0;
        if (herbivoreCount >= DifficultyGoals.getHerbivore(diffOrdinal)) goalList[3]++;
        else goalList[3] = 0;

        for (int singleGoals : goalList) {
            if (singleGoals < DifficultyGoals.getMonth(diffOrdinal) * multiplier) return false;
        }
        return true;
    }

    /**
     * Adds an animal to the game model
     *
     * @param a the animal's model
     */
    public void addAnimal(Animal a) {
        animals.add(a);
    }

    public void addRanger(Ranger r) {
        rangers.add(r);
    }

    /**
     * Mark a dead ranger to remove it
     *
     * @param ranger
     */
    public void markRanger(Ranger ranger) {
        markedRangers.add(ranger);
    }

    /**
     * removes the marked rnagers
     */
    public void removeDeadRangers() {
        rangers.removeAll(markedRangers);
        rangersToRemove.addAll(markedRangers);
        markedRangers.clear();
    }

    /**
     * removes the captured animal from the model
     *
     * @param capturedAnimal
     */
    public void removeAnimal(Animal capturedAnimal) {
        animals.remove(capturedAnimal);
        animalsToRemove.add(capturedAnimal);
    }

    /**
     * Sells the animal in the model
     *
     * @param animal the sold animal
     */
    public void sellAnimal(Animal animal) {
        animals.remove(animal);
        if (animal.getLeader() != null) {
            removeTheLeader(animal);
        }
        changeMoney(animal.getPrice() * 0.5);
    }

    public void changeMoney(double change) {
        currentMoney += change;
    }

    /**
     * Adds a chip to the selected animal
     *
     * @param animal
     */
    public void getChipped(Animal animal) {
        double chipCost = 500;
        if (!animal.isChipped()) {
            animal.setChipped(true);
            changeMoney(-chipCost);
        }
    }

    /**
     * The core update method, iterates through all the moving objects
     *
     * @param width
     * @param height
     */
    public void update(double width, double height) {
        int index = 0;
        for (Animal animal : animals) {
            if (animal.getTargetCoord() == null) {
                animal.createRandomTarget(width, height);
            }

            if (animal.migrate() && !animal.isCaptured()) {
                animal.createRandomTarget(width, height);
            }
            animal.move();
            animal.collidedObstacle();

            for (Animal conAnimals : animals) {
                if (conAnimals.getLeader() != null && conAnimals.getClass() == animal.getClass()) {
                    groupLeaders.add(conAnimals);
                }
            }

            if (animal.getLeader() == null && !animal.isCaptured()) {
                Animal leader = null;
                for (Animal typeLeader : groupLeaders) {
                    if (animal.getClass() == typeLeader.getClass()) leader = typeLeader;
                }
                if (leader != null && !leader.isCaptured()) {
                    index++;
                    animal.followLeaderWithOffset(leader, index);
                } else {
                    animal.setLeader();
                }
            }
        }

        for (Ranger ranger : rangers) {
            ranger.hunt();
        }
        removeDeadRangers();

        for (Poacher poacher : poachers) {
            poacher.setVisibility(poacher.isVisibleForRangers(rangers));
            if (!poacher.isEscaping()) {
                poacher.hunt(animals);
            } else {
                if (poacher.escape()) {
                    Animal capturedAnimal = poacher.getCapturedAnimal();
                    poacher.setCapturedAnimal(null);
                    removeAnimal(capturedAnimal);

                    poacher.setEscaping(false);
                    poacher.setCaptured(false);
                    poacher.setSpeed(0.5);
                }
            }
        }
    }

    public Path getNewPath() {
        return currentPath;
    }

    /**
     * Removes an animal from the game and cleanups.
     *
     * @param target The animal to be removed from the game. If null, the method returns immediately.
     */
    public void huntAnimal(Animal target) {
        if (target == null) return;

        if (target.getLeader() != null) {
            removeTheLeader(target);
        }

        animalsToRemove.add(target);
        animals.remove(target);
    }

    /**
     * The poacher captures the animal. Returns nothing if no animal is found to capture.
     *
     * @param animal  The animal that's captured.
     * @param poacher Who captures the animal.
     */
    public void captureAnimal(Animal animal, Poacher poacher) {
        if (animal == null) return;

        if (animal.getLeader() != null) {
            removeTheLeader(animal);
        }
        animal.setTargetCoord(poacher.getCoord());

        poacher.setSpeed(animal.getSpeed() + 0.05);
        animal.setCaptured(true);
        poacher.setCapturedAnimal(animal);
        poacher.setEscaping(true);
    }

    /**
     * Removes the specified leader from the group and reassigns new target locations
     * for the remaining animals in the game.
     *
     * @param leader The leader to be removed from the group.
     */
    private void removeTheLeader(Animal leader) {
        groupLeaders.removeIf(a -> a.equals(leader));
        for (Animal animal1 : animals) {
            animal1.createRandomTarget(width, height);
        }
    }

    /**
     * Removes the marked poachers from the model
     *
     * @param poacher
     */
    public void removePoacher(Poacher poacher) {
        poachersToRemove.add(poacher);
        poachers.remove(poacher);
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    /**
     * Adds a poacher to the model
     *
     * @param width
     * @param height
     * @return the newly spawned poacher
     */
    public Poacher poacherSpawnNow(double width, double height) {
        Random rand = new Random();
        double x = rand.nextDouble(width);
        double y = rand.nextDouble(height);
        Coordinate coordinatePoacher = new Coordinate(x, y);

        Poacher poach = new Poacher(coordinatePoacher, 100, 0.5, false, this, width, height);
        poachers.add(poach);
        return poach;
    }

    public int getJeepNumber() {
        return jeeps.size();
    }

    public AtomicBoolean getIsNight() {
        return isNight;
    }

    public void setIsNight(AtomicBoolean b) {
        isNight = b;
    }

    /**
     * Monthly pays the rangers' salary
     */
    public void payRangers() {
        double salaryCost = -Ranger.getSalary() * rangers.size();
        changeMoney(-salaryCost);
    }

    public int getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(int ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public double getCurrentMoney() {
        return currentMoney;
    }

    /**
     * Removes the object from the model
     *
     * @param pond
     */
    public void removePond(Pond pond) {
        pondsToRemove.add(pond);
        ponds.remove(pond);
    }

    /**
     * Removes the object from the model
     *
     * @param plant
     */
    public void removePlant(Plant plant) {
        plantsToRemove.add(plant);
        plants.remove(plant);
    }

    public ArrayList<Jeep> getJeeps() {
        return jeeps;
    }

    public void setNewPath(Path currentPath) {
        this.currentPath = currentPath;
    }

    public Difficulty getDifficulty() {
        return currentDifficulty;
    }

    public void setAnimals(ArrayList<Animal> animals) {
        this.animals = animals;
    }

    /**
     * After loading a saved game, this method reconnects the objects' game model to the loaded one
     */
    public void reconnectReferences() {
        for (Animal a : animals) {
            a.setGameModel(this);
        }
        for (Pond p : ponds) {
            p.setGameModel(this);
        }
        for (Poacher poacher : poachers) {
            poacher.setGameModel(this);
        }
        for (Ranger ranger : rangers) {
            ranger.setGameModel(this);
        }
        for (Jeep jeep : jeeps) {
            jeep.setGameModel(this);
        }
    }

}