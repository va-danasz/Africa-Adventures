package com.example.africaadventures;

import com.example.gamemodel.*;
import com.example.viewmodel.*;
import com.example.viewmodel.GameView;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import static com.example.gamemodel.Coordinate.calcDistance;

public class GameController {
    private GameModel model;
    private GameView view;
    private final boolean enableGameEnd = true;
    private final boolean enableDayLight = true;
    private ScheduledExecutorService executorDrink = null;
    private ScheduledExecutorService executorEat = null;
    private ScheduledExecutorService executorTimeCounter = null;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private Animal selectedAnimal;
    private AnimationTimer gameLoop;
    private final AtomicBoolean isOver = new AtomicBoolean(false);
    private final TouristPipeline touristPipeline = new TouristPipeline();
    private final Map<String, ShopCategory> categories = new HashMap<>();
    private boolean isSpawningPoachers = false;
    private Path currentPath = null;
    private Object currentSelectedObject = null;
    private ScheduledExecutorService updateExecutor = null;
    private Consumer<ArrayList<String>> infoUpdateCallback;

    public GameController() {
    }

    public GameModel getModel() {
        return model;
    }

    public GameView getView() {
        return view;
    }

    /**
     * Lays the game scene out at screen size right away. Some platforms (e.g. Linux) size a
     * fullscreen window asynchronously, so the map would still be 0x0 when the game starts.
     *
     * @param root the root of the game scene
     */
    public static void layoutToScreen(Parent root) {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        if (root instanceof Region region) {
            region.resize(bounds.getWidth(), bounds.getHeight());
        }
        root.applyCss();
        root.layout();
    }

    /**
     * Handles the game's initialization
     *
     * @param model
     * @param view
     * @param difficulty the current difficulty
     */
    public void initGame(GameModel model, GameView view, Difficulty difficulty) {
        prepareGame(model, view, difficulty);
        defaultGeneration(model);
    }

    /**
     * At every start, this method builds up the model and view, action listeners and other setups
     *
     * @param model
     * @param view
     * @param difficulty the current difficulty
     */
    public void prepareGame(GameModel model, GameView view, Difficulty difficulty) {
        this.model = model;
        this.view = view;
        model.setDifficulty(difficulty);
        model.setSize(view.getWidth(), view.getHeight());

        double gateSize = 100;
        double gatePos = 50;

        Gate entranceGate = new Gate(new Coordinate(gatePos, view.getHeight() - (gatePos + gateSize)), gateSize, true);
        Gate exitGate = new Gate(new Coordinate(getView().getWidth() - (gatePos + gateSize), view.getHeight() - (gatePos + gateSize)), gateSize, false);

        initShopLogic();
        view.getGamePane().setFocusTraversable(true);
        view.initBackground();
        view.initFogCanvas();
        view.initSlider(model.getTicketPrice());

        view.getSlider().valueProperty().addListener((obs, oldVal, newVal) -> {
            model.setTicketPrice(newVal.intValue());
        });

        view.setOnSaveGame(this::handleSave);
        view.setOnReturnHome(this::navigateHome);
        view.setOnSpeedChange(e -> restartTimeCounterExecutor());
        view.setOnConfirmPurchase(this::finalizePurchase);
        view.getGamePane().setOnMouseClicked(e -> view.getGamePane().requestFocus());

        restartTimeCounterExecutor();
        setupDrinkScheduler();
        setupEatScheduler();
        setupJeepThread();
        setupKeyboardControls();
        setupMouseClickBlocker();
        startGameLoop();

        view.getGamePane().requestFocus();
        reconnectViews(model, view, entranceGate, exitGate);
        resetJeeps();
    }

    /**
     * Restarts the jeeps' executors
     */
    private void resetJeeps() {
        for (Jeep jeep : model.getJeeps()) {
            jeep.setLock();
            executor.execute(() -> jeep.beginDriving(isOver));
        }
    }

    /**
     * Reconnects the model to the corresponding views after loading a saved file
     *
     * @param model
     * @param view
     * @param entrance entrance gate
     * @param exit     exit gate
     */
    public void reconnectViews(GameModel model, GameView view, Gate entrance, Gate exit) {
        for (Animal a : model.getAnimals()) {
            BuyableView v = BuyableViewFactory.createView(a);
            a.setView(v);
            v.setOnClickAction(e -> handleBuyableClick(a, v));
            view.addNode(v.getCircle());
        }

        for (Ranger r : model.getRangers()) {
            BuyableView v = BuyableViewFactory.createView(r);
            r.setView(v);
            v.setOnClickAction(e -> handleBuyableClick(r, v));
            view.addNode(v.getCircle());
        }

        for (Plant p : model.getPlants()) {
            BuyableView v = BuyableViewFactory.createView(p);
            p.setView(v);
            v.setOnClickAction(e -> handleBuyableClick(p, v));
            view.addNode(v.getCircle());
        }

        for (Pond pond : model.getPonds()) {
            BuyableView v = BuyableViewFactory.createView(pond);
            pond.setView(v);
            v.setOnClickAction(e -> handleBuyableClick(pond, v));
            view.addNode(v.getCircle());
        }

        for (Jeep jeep : model.getJeeps()) {
            JeepView v = new JeepView(jeep);
            jeep.setView(v);
            v.setOnClickAction(e -> handleBuyableClick(jeep, v));
            view.addNode(v.getCircle());
        }

        for (Poacher p : model.getPoachers()) {
            PoacherView v = new PoacherView(p);
            p.setView(v);
            view.addNode(v.getCircle());
        }

        for (Path p : model.getRoads()) {
            for (RoadPiece r : p.getPath()) {
                RoadPieceView v = new RoadPieceView(r);
                r.setView(v);
                view.addNode(v.getRectangle());
            }
        }

        entrance.setView(new GateView(entrance));
        exit.setView(new GateView(exit));
        view.initGates(entrance, exit);

        entrance.getView().getRect().setOnMouseClicked(e -> handleGateClick(entrance));
        exit.getView().getRect().setOnMouseClicked(e -> handleGateClick(exit));
    }

    /**
     * Upon starting a new game generates some starting objects
     *
     * @param model
     */
    private void defaultGeneration(GameModel model) {
        List<Animal> starters = List.of(
                new Antilope(new Coordinate(100, 100), 600, model),
                new Antilope(new Coordinate(50, 50), 600, model),
                new Antilope(new Coordinate(50, 100), 600, model)
        );

        starters.forEach(a -> {
            model.addAnimal(a);
            BuyableView viewComponent = BuyableViewFactory.createView(a);
            a.setView(viewComponent);
            viewComponent.setOnClickAction(e -> handleBuyableClick(a, viewComponent));
            view.addNode(a.getView().getCircle());
        });

        List<Buyable> generated = generateMap(view.getWidth(), view.getHeight());
        for (Buyable obj : generated) {
            BuyableView viewComponent = BuyableViewFactory.createView(obj);
            obj.setView(viewComponent);
            viewComponent.setOnClickAction(e -> handleBuyableClick(obj, viewComponent));
            view.addNode(viewComponent.getCircle());
        }

        model.setIsNight(new AtomicBoolean(false));
    }

    /**
     * Saves the current game state, handle exceptions
     *
     * @param event
     */
    public void handleSave(ActionEvent event) {
        if (model == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Your Game");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Save Files (*.sav)", "*.sav")
        );
        fileChooser.setInitialFileName("safari_save.sav");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            if (!selectedFile.getName().toLowerCase().endsWith(".sav")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".sav");
            }

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(selectedFile))) {
                out.writeObject(model);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * At the start of a new game it generates some random objetcs
     *
     * @param width
     * @param height
     * @return the generated objects
     */
    public ArrayList<Buyable> generateMap(double width, double height) {
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            Buyable obj = model.generateObject(rand, width, height);
            BuyableView viewComponent = BuyableViewFactory.createView(obj);
            obj.setView(viewComponent);
        }
        return model.getObjects();
    }

    /**
     * stops the game when returning home
     *
     * @param event
     */
    private void navigateHome(ActionEvent event) {
        if (executorTimeCounter != null) {
            executorTimeCounter.shutdownNow();
        }
        gameLoop.stop();
    }

    /**
     * Restart the time counter. It changes the ages of the animals
     */
    private void restartTimeCounterExecutor() {
        if (executorTimeCounter != null && !executorTimeCounter.isShutdown()) {
            executorTimeCounter.shutdownNow();
        }
        updateGameSpeed(Math.pow((view.getSpeedChoiceBox().getSelectionModel().getSelectedIndex() + 1), 2));

        executorTimeCounter = Executors.newScheduledThreadPool(1);
        executorTimeCounter.scheduleAtFixedRate(() -> {
            if (executorTimeCounter.isShutdown()) {
                return;
            }

            int state = model.gameOver();
            if (enableGameEnd && state > 0) {
                gameOver(state > 1);
                isOver.set(true);
            }

            List<Animal> newOffsprings = new ArrayList<>();
            for (Animal a : model.getAnimals()) {
                a.aging();
                if (a.getLeader() != null) {
                    for (Animal b : model.getAnimals()) {
                        if (b.getFollowingLeader() != null && b.getFollowingLeader().equals(a)) {
                            if (b.reproduce(a)) {
                                Animal offspring = model.createAnimal(a);
                                if (offspring != null) {
                                    newOffsprings.add(offspring);
                                }
                            }
                        }
                    }
                }
                if (a.isTooOld()) {
                    model.removeAnimal(a);
                }
            }
            for (Animal offspring : newOffsprings) {
                Platform.runLater(() -> {
                    model.getAnimals().add(offspring);
                    BuyableView viewComponent = BuyableViewFactory.createView(offspring);
                    offspring.setView(viewComponent);
                    viewComponent.setOnClickAction(e -> handleBuyableClick(offspring, viewComponent));
                    view.addNode(offspring.getView().getCircle());
                });
            }

            int prevMonth = (calcTime()[2]);
            model.setTimeCounter(model.getTimeCounter() + 1);

            int hours = calcTime()[0];
            int days = calcTime()[1];
            int months = calcTime()[2];

            model.setIsNight(new AtomicBoolean(enableDayLight && (hours <= 6 || hours >= 20) && (days > 0)));

            if (prevMonth != months && !model.getRangers().isEmpty()) {
                model.payRangers();
            }

            String formattedTime = String.format("Time: M%02d, D%02d, H%02d", months, days, hours);
            view.setTimeLabel(formattedTime);
        }, 0, (long) (10 / model.getSpeedModifier()), TimeUnit.SECONDS);
    }

    /**
     * From the time counter calculates the days and months
     *
     * @return the array of the formatted time
     */
    public int[] calcTime() {
        int hours = model.getTimeCounter() % 24;
        int days = (model.getTimeCounter() / 24) % 30;
        int months = (model.getTimeCounter() / (24 * 30));

        return new int[]{hours, days, months};
    }

    /**
     * Ends the game, stops the game loop, and transitions to the GameEnd Scene
     * with appropriate messaging based on whether the player won or lost.
     *
     * @param won Indicates whether the player has won the game.
     *            True: The game congrats the player
     *            False: The game says that the player lost the game.
     */
    public void gameOver(boolean won) {
        shutdownExecutor(executor);
        shutdownExecutor(executorDrink);
        shutdownExecutor(executorEat);
        shutdownExecutor(executorTimeCounter);
        if (gameLoop != null) gameLoop.stop();

        view.gameOver(won, model.getTimeCounter(), model.getDifficulty());
    }

    /**
     * Shuts down the given executor
     *
     * @param exec
     */
    private void shutdownExecutor(ExecutorService exec) {
        if (exec == null || exec.isShutdown()) return;

        exec.shutdown();
        try {
            if (!exec.awaitTermination(1, TimeUnit.SECONDS)) {
                exec.shutdownNow();
            }
        } catch (InterruptedException e) {
            exec.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * starts the jeeps
     */
    private void setupJeepThread() {
        new Thread(() -> {
            while (model.getJeepNumber() <= 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            startJeeps();
        }).start();
    }

    /**
     * Setting up the BlockingQueue and Executor system for the relationship between jeeps and tourists.
     */
    private void startJeeps() {
        if (model.getJeepNumber() > 0) {
            Tourist tourist = new Tourist(model);
            TouristDistributor distributor = new TouristDistributor();

            executor.execute(() -> tourist.startSpawning(isOver));
            executor.execute(() -> distributor.startDistributing(isOver,  tourist, touristPipeline));

            new Thread(() -> {
                try {
                    while (!isOver.get()) {
                        Thread.sleep(500);
                    }
                    executor.shutdown();
                    executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    public void poacherInvisible(Poacher poacher) {
        poacher.getView().getCircle().setVisible(poacher.isVisibility());
    }

    /**
     * handles poacher spawning regularly
     */
    private void poacherSpawning() {
        if (isSpawningPoachers) {
            return;
        }

        isSpawningPoachers = true;
        PauseTransition delay = new PauseTransition(Duration.seconds(50));
        delay.setOnFinished(e -> {
            Poacher p = model.poacherSpawnNow(view.getWidth(), view.getHeight());
            PoacherView poacherView = new PoacherView(p);
            p.setView(poacherView);
            view.addNode(poacherView.getCircle());
            poacherView.setOnClickAction(mouseEvent -> handlePoacherClick(p, poacherView));
            isSpawningPoachers = false;
        });
        delay.play();
    }

    public void updateGameSpeed(double newSpeed) {
        model.setSpeedModifier(newSpeed);
    }

    private void setupDrinkScheduler() {
        if (executorDrink == null) {
            executorDrink = Executors.newScheduledThreadPool(1);
            executorDrink.scheduleAtFixedRate(this::handleAnimalDrinking, 2, makeRandomPeriods(), TimeUnit.SECONDS);
        }
    }

    private void setupEatScheduler() {
        if (executorEat == null) {
            executorEat = Executors.newScheduledThreadPool(1);
            executorEat.scheduleAtFixedRate(this::handleAnimalEating, 3, makeRandomPeriods(), TimeUnit.SECONDS);
        }
    }

    /**
     * Using ScheduledExecutor to arrange drinking routines and find the closest water source.
     */
    public void handleAnimalDrinking() {
        for (Animal animal : model.getAnimals()) {
            if (!animal.isCaptured()) {
                executorDrink.submit(() -> {
                    synchronized (animal) {
                        if (animal.getLeader() == animal) {
                            boolean anyFollowerThirsty = model.getAnimals().stream()
                                    .filter(a -> a.getLeader() == animal && !a.isCaptured())
                                    .anyMatch(a -> a.getDrinkingCooldown() <= 0);

                            if (anyFollowerThirsty && animal.getDrinkingCooldown() > 0) {
                                animal.resetDrinkingForLeader();
                            }
                        }
                        if (!animal.getDrinkingPlaces().isEmpty() && !animal.isDrinking() && animal.getDrinkingCooldown() <= 0) {
                            Coordinate closestWater = null;
                            double minDistance = Double.MAX_VALUE;

                            for (Coordinate waterSource : animal.getDrinkingPlaces()) {
                                boolean exists = model.getPonds().stream().anyMatch(p -> animal.sameCoord(p.getCoord(), waterSource));
                                if (!exists) continue;
                                double dist = calcDistance(animal.getCoord(), waterSource);
                                if (dist < minDistance) {
                                    minDistance = dist;
                                    closestWater = waterSource;
                                }
                            }

                            if (closestWater != null) {
                                Coordinate edgeCoord = animal.calculateEdgeTarget(closestWater, 40);
                                animal.setTargetCoord(edgeCoord);
                            }
                        }
                        ;
                    }
                });

            }
        }
    }

    /**
     * Using ScheduledExecutor to arrange eating routines and find the closest source.
     */
    private void handleAnimalEating() {
        for (Animal animal : model.getAnimals()) {
            if (!animal.isCaptured()) {
                executorEat.submit(() -> {
                    synchronized (animal) {
                        if (animal.isCarnivore()) {
                            for (Animal prey : model.getAnimals()) {
                                if (!prey.equals(animal) && calcDistance(animal.getCoord(), prey.getCoord()) < animal.getVision() && !prey.isCarnivore()) {
                                    animal.addTarget(prey);
                                }
                            }

                            if (!animal.getTargets().isEmpty()) {
                                final Animal closestTarget;

                                double minDistance = Double.MAX_VALUE;
                                Animal tempTarget = null;

                                for (Animal prey : animal.getTargets()) {
                                    double dist = calcDistance(animal.getCoord(), prey.getCoord());
                                    if (dist < minDistance) {
                                        minDistance = dist;
                                        tempTarget = prey;
                                    }
                                }

                                closestTarget = tempTarget;

                                if (closestTarget != null) {
                                    double temp = animal.getSpeed();
                                    animal.setSpeed(1.2);

                                    AnimationTimer huntingTimer = new AnimationTimer() {
                                        @Override
                                        public void handle(long now) {
                                            animal.huntDown(closestTarget);

                                            if (animal.getCoord().distanceTo(closestTarget.getCoord(), 2 * 20)) {
                                                model.huntAnimal(closestTarget);
                                                animal.removeTarget(closestTarget);
                                                animal.setSpeed(temp);
                                                this.stop();
                                            }
                                        }
                                    };
                                    huntingTimer.start();
                                }
                            }
                        } else {
                            if (animal.getLeader() == animal) {
                                boolean anyFollowerHungry = model.getAnimals().stream()
                                        .filter(a -> a.getLeader() == animal && !a.isCaptured())
                                        .anyMatch(a -> a.getEatingCooldown() <= 0);

                                if (anyFollowerHungry && animal.getEatingCooldown() > 0) {
                                    animal.resetEatingForLeader();
                                }
                            }
                            if (!animal.getEatingPlaces().isEmpty() && !animal.isEating() && animal.getEatingCooldown() <= 0) {
                                Coordinate closestFood = null;
                                double minDistance = Double.MAX_VALUE;

                                for (Coordinate foodSource : animal.getEatingPlaces()) {
                                    boolean exists = model.getPlants().stream().anyMatch(p -> animal.sameCoord(p.getCoord(), foodSource));
                                    if (!exists) continue;

                                    double dist = calcDistance(animal.getCoord(), foodSource);
                                    if (dist < minDistance) {
                                        minDistance = dist;
                                        closestFood = foodSource;
                                    }
                                }

                                if (closestFood != null) {
                                    Coordinate edgeCoord = animal.calculateEdgeTarget(closestFood, 20);
                                    animal.setTargetCoord(edgeCoord);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Handles animal selling and chip purchase
     */
    private void setupKeyboardControls() {
        view.getGamePane().setOnKeyPressed(event -> {
            if (currentSelectedObject instanceof Animal) {
                switch (event.getCode()) {
                    case S -> {
                        model.sellAnimal(selectedAnimal);
                        view.removeBuyableView(selectedAnimal.getView());
                        currentSelectedObject = null;
                        stopContinuousUpdates();
                    }
                    case C -> {
                        if (selectedAnimal != null) {
                            model.getChipped(selectedAnimal);
                        }
                    }
                }
            }
        });
    }

    /**
     * Blocks the clicking through the black canvas representing the night
     */
    private void setupMouseClickBlocker() {
        view.getGamePane().setOnMousePressed(event -> {
            if (!model.getIsNight().get()) return;
            event.consume();
        });
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    controllerUpdate();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Updates the model and the view
     *
     * @throws InterruptedException
     */
    public void controllerUpdate() throws InterruptedException {
        model.update(view.getWidth(), view.getHeight());

        for (Animal a : model.getAnimals()) {
            a.getView().updatePosition();
        }

        for (Ranger r : model.getRangers()) {
            r.getView().updatePosition();
        }

        for (Poacher p : model.getPoachers()) {
            poacherInvisible(p);
            p.getView().updatePosition();
        }

        for (Jeep j : model.getJeeps()) {
            ((JeepView) j.getView()).updatePosition();
        }

        poacherSpawning();
        removeObjects();
        view.refreshMoney(model.getCurrentMoney());
        updateFog();
    }

    /**
     * Updates the fog-of-war effect that represents the day and night cycle
     */
    private void updateFog() {
        if (!model.getIsNight().get()) {
            view.clearFogCanvas();
            for (Animal animal : model.getAnimals()) {
                view.setDayLight(animal, true);
            }
            return;
        }

        view.initNightFog();
        double step = 4;
        for (Ranger ranger : model.getRangers()) {
            view.clearFog(ranger, step);
        }
        for (Plant plant : model.getPlants()) {
            view.clearFog(plant, step);
        }
        for (Pond pond : model.getPonds()) {
            view.clearFog(pond, step);
        }
        for (Jeep jeep : model.getJeeps()) {
            view.clearFog(jeep, step);
        }

        for (Path path : model.getRoads()) {
            double radius = path.getVision();
            for (RoadPiece rp : path.getPath()) {
                Coordinate a = rp.getEndPoint1();
                Coordinate b = rp.getEndPoint2();

                double stepSize = 16;
                double distance = calcDistance(a, b);
                int steps = (int) (distance / stepSize);
                if (steps == 0) steps = 1;

                for (int i = 0; i <= steps; i++) {
                    double t = i / (double) steps;
                    double x = a.getX() * (1 - t) + b.getX() * t;
                    double y = a.getY() * (1 - t) + b.getY() * t;
                    view.clearCanvasFog(radius, x, y, step);
                }
            }
        }

        for (Animal animal : model.getAnimals()) {
            view.animalFog(animal, step, model.getIsNight(), isInAnyVision(animal.getCoord()));
        }
    }

    /**
     * Removes the objects from the scene, which are removed from the model
     */
    private void removeObjects() {
        for (Animal a : model.getAnimalsToRemove()) {
            if (a.equals(currentSelectedObject)) {
                stopContinuousUpdates();
            }
            view.removeBuyableView(a.getView());
        }
        model.getAnimalsToRemove().clear();
        for (Ranger r : model.getRangersToRemove()) {
            if (r.equals(currentSelectedObject)) {
                stopContinuousUpdates();
            }
            view.removeBuyableView(r.getView());
        }
        model.getRangersToRemove().clear();
        for (Poacher p : model.getPoachersToRemove()) {
            if (p.equals(currentSelectedObject)) {
                stopContinuousUpdates();
            }
            view.removePoacherView(p.getView());
        }
        model.getPoachersToRemove().clear();
        for (Plant p : model.getPlantsToRemove()) {
            if (p.equals(currentSelectedObject)) {
                stopContinuousUpdates();
            }
            view.removeBuyableView(p.getView());
        }
        model.getPlantsToRemove().clear();
        for (Pond p : model.getPondsToRemove()) {
            if (p.equals(currentSelectedObject)) {
                stopContinuousUpdates();
            }
            view.removeBuyableView(p.getView());
        }
        model.getPondsToRemove().clear();
    }

    /**
     * Creates random periods for the drink, and eat for the animals.
     */
    private int makeRandomPeriods() {
        Random randPeriod = new Random();
        return randPeriod.nextInt(12 - 5 + 1) + 5;
    }

    /**
     * initializes shop, sets the prices
     */
    public void initShopLogic() {
        categories.put("Plants", new ShopCategory("Plants", new ArrayList<>(List.of(
                new ShopItem("Bush", 200),
                new ShopItem("Grass", 100),
                new ShopItem("Tree", 300)
        ))));

        categories.put("Animals", new ShopCategory("Animals", new ArrayList<>(List.of(
                new ShopItem("Antilope", 600),
                new ShopItem("Cheetah", 800),
                new ShopItem("Lion", 1200),
                new ShopItem("Zebra", 700)
        ))));

        categories.put("Utilities", new ShopCategory("Utilities", new ArrayList<>(List.of(
                new ShopItem("Road", 250),
                new ShopItem("Jeep", 500),
                new ShopItem("Ranger", 1500),
                new ShopItem("Pond", 1000)
        ))));

        view.setOnSpeedChange(e -> {
            String selectedSpeed = view.getSpeedChoiceBox().getSelectionModel().getSelectedItem();
            double multiplier = switch (selectedSpeed) {
                case "Normal" -> 4.0;
                case "Fast" -> 9.0;
                default -> 1.0;
            };
            model.setSpeedModifier(multiplier);
        });

        view.setOnCategorySelect(categoryName -> {
            if (!model.getIsNight().get()) {
                stopContinuousUpdates();
                ShopCategory category = categories.get(categoryName);
                category.setView(new ShopCategoryView(category));
                category.getView().setOnItemSelected(this::handleItemPurchase);
                view.showCategory(category);
                view.shopMenuAction(model.getObjects());
            }
        });

        int baseMoney = 10000;
        view.initShopUI(baseMoney);
    }

    /**
     * Start of the purchase method
     *
     * @param item bought item
     */
    public void handleItemPurchase(ShopItem item) {
        view.handlePurchaseInit(item);

        if (item.getName().equals("Road")) {
            model.getDotCoords().clear();
            model.getDotCoords().addAll(view.getDotMap().keySet());
            initRoadPlacing();
        }
    }

    /**
     * initializes the purchase of the path placing
     */
    private void initRoadPlacing() {
        Path newSegment = new Path(250);

        if (currentPath != null) {
            newSegment.copyGraphFrom(currentPath);
        }

        newSegment = view.initRoadView(newSegment);
        currentPath = newSegment;
    }

    /**
     * Finalizes the purchase of the bought item
     *
     * @param item the bought item
     * @param draggableCoord the draggable object
     */
    private void finalizePurchase(ShopItem item, Coordinate draggableCoord) {
        Buyable bought = chooseType(item, draggableCoord);
        if (bought == null) {
            if (item.getName().equals("Road")) view.removeLastPath(currentPath);
            return;
        }
        addPurchased(bought);

        model.changeMoney(-item.getPrice());
        view.setLastPlacedNull();
    }

    /**
     * After purchase adds the bought object to the model
     *
     * @param bought
     */
    private void addPurchased(Buyable bought) {
        BuyableView viewComponent;
        switch (bought.getType()) {
            case CanBuy.ANIMAL:
                Animal newAnimal = (Animal) bought;
                model.getAnimals().add(newAnimal);
                viewComponent = BuyableViewFactory.createView(newAnimal);
                newAnimal.setView(viewComponent);
                viewComponent.setOnClickAction(e -> handleBuyableClick(newAnimal, viewComponent));
                view.addNode(newAnimal.getView().getCircle());
                break;
            case CanBuy.PLANT:
                Plant newPlant = (Plant) bought;
                model.getPlants().add(newPlant);
                model.getObjects().add(newPlant);
                viewComponent = BuyableViewFactory.createView(newPlant);
                newPlant.setView(viewComponent);
                viewComponent.setOnClickAction(e -> handleBuyableClick(newPlant, viewComponent));
                view.addNode(newPlant.getView().getCircle());
                break;
            case CanBuy.POND:
                Pond newPond = (Pond) bought;
                model.getPonds().add(newPond);
                model.getObjects().add(newPond);
                viewComponent = BuyableViewFactory.createView(newPond);
                newPond.setView(viewComponent);
                viewComponent.setOnClickAction(e -> handleBuyableClick(newPond, viewComponent));
                view.addNode(newPond.getView().getCircle());
                break;
            case CanBuy.PATH:
                model.getRoads().add(currentPath);
                model.setNewPath(currentPath);
                break;
            case CanBuy.RANGER:
                Ranger newRanger = (Ranger) bought;
                model.getRangers().add(newRanger);
                viewComponent = BuyableViewFactory.createView(newRanger);
                newRanger.setView(viewComponent);
                viewComponent.setOnClickAction(e -> handleBuyableClick(newRanger, viewComponent));
                view.addNode(newRanger.getView().getCircle());
                break;
            case CanBuy.JEEP:
                Jeep newJeep = (Jeep) bought;
                addJeep(newJeep);
                viewComponent = BuyableViewFactory.createView(newJeep);
                newJeep.setView(viewComponent);
                viewComponent.setOnClickAction(e -> handleBuyableClick(newJeep, viewComponent));
                view.addNode(newJeep.getView().getCircle());
                break;
        }
    }

    /**
     * Helps to decide what is the bought object
     *
     * @param item           the bought item
     * @param draggableCoord the draggable object's current coordinate
     * @return the corresponding bought object
     */
    private Buyable chooseType(ShopItem item, Coordinate draggableCoord) {
        Buyable bought;
        switch (item.getName()) {
            case "Antilope":
                bought = new Antilope(draggableCoord, 600.0, model);
                break;
            case "Lion":
                bought = new Lion(draggableCoord, 1200.0, model);
                break;
            case "Cheetah":
                bought = new Cheetah(draggableCoord, 800.0, model);
                break;
            case "Zebra":
                bought = new Zebra(draggableCoord, 700.0, model);
                break;
            case "Bush":
                bought = new Bush(draggableCoord, 200.0, 3, model);
                break;
            case "Tree":
                bought = new Tree(draggableCoord, 300.0, 3, model);
                break;
            case "Grass":
                bought = new Grass(draggableCoord, 100.0, 3, model);
                break;
            case "Pond":
                bought = new Pond(draggableCoord, 1000.0, 10, model);
                break;
            case "Road":
                bought = currentPath;
                view.setEndPoint1(null);
                break;

            case "Jeep": {
                Coordinate ent = new Coordinate(view.getEntranceGate().getCoord().getX(), view.getEntranceGate().getCoord().getY());
                model.setExit(view.getExitPoint());
                bought = (model.getRoads().isEmpty()) ? null : new Jeep(ent, 500, touristPipeline, model);
                break;
            }
            case "Ranger":
                bought = new Ranger(draggableCoord, 1500.0, model, view.getWidth(), view.getHeight());
                break;
            default:
                bought = null;
        }

        return bought;
    }

    /**
     * Adds a jeep to the model, tries to start it
     *
     * @param jeep
     */
    public void addJeep(Jeep jeep) {
        model.getJeeps().add(jeep);
        if (!executor.isShutdown()) {
            executor.execute(() -> jeep.beginDriving(isOver));
        }
    }

    /**
     * Generates infos for the infoContainer.
     *
     * @param selectedObject The selected object.
     */
    public ArrayList<String> generateInfoForObject(Object selectedObject) {
        ArrayList<String> infos = new ArrayList<>();
        if (selectedObject instanceof Animal animal) {
            infos.add("Type: " + animal.getClass().getSimpleName());
            infos.add("Price: " + animal.getPrice());
            infos.add("Chipped: " + animal.isChipped());
            infos.add("Age: " + animal.getAge());
        } else if (selectedObject instanceof Ranger ranger) {
            infos.add("Type: " + ranger.getClass().getSimpleName());
            infos.add("Price: " + ranger.getPrice());
            infos.add("Targets: " + ranger.getTargets().size());
            infos.add("HP: " + ranger.getHp());
        } else if (selectedObject instanceof Jeep jeep) {
            infos.add("Type: " + jeep.getClass().getSimpleName());
            infos.add("Price: " + jeep.getPrice());
            infos.add("Satisfaction: " + model.getSatisfactionMultiplier());
            infos.add("Passengers: " + jeep.getPassengerNum());
        } else if (selectedObject instanceof Plant plant) {
            infos.add("Type: " + plant.getClass().getSimpleName());
            infos.add("Price: " + plant.getPrice());
            infos.add("Position: \nX:" + plant.getCoord().getX() + " Y:" + plant.getCoord().getY());
            infos.add("Quantity: " + plant.getQuantity());
        } else if (selectedObject instanceof Gate gate) {
            infos.add("Type: Gate");
            infos.add("Ticket: " + model.getTicketPrice());
            infos.add("X: " + gate.getCoord().getX());
            infos.add("Y: " + gate.getCoord().getY());
        } else if (selectedObject instanceof Pond pond) {
            infos.add("Type: " + pond.getClass().getSimpleName());
            infos.add("Price: " + pond.getPrice());
            infos.add("Position: \nX:" + pond.getCoord().getX() + " Y:" + pond.getCoord().getY());
            infos.add("Quantity: " + pond.getCapacity());
        }
        return infos;
    }

    /**
     * Updates continuously the selectedObject infos.
     *
     * @param selectedObject The selected object.
     * @param updateCallback
     */
    public void startContinuousUpdates(Object selectedObject, Consumer<ArrayList<String>> updateCallback) {
        stopContinuousUpdates();
        this.currentSelectedObject = selectedObject;
        this.infoUpdateCallback = updateCallback;

        updateExecutor = Executors.newSingleThreadScheduledExecutor();
        updateExecutor.scheduleAtFixedRate(() -> {
            ArrayList<String> info = generateInfoForObject(currentSelectedObject);
            Platform.runLater(() -> infoUpdateCallback.accept(info));
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the update continuously the selectedObject infos.
     */
    public void stopContinuousUpdates() {
        if (updateExecutor != null && !updateExecutor.isShutdown()) {
            updateExecutor.shutdownNow();
        }
        updateExecutor = null;
        currentSelectedObject = null;
        infoUpdateCallback = null;
    }

    /**
     * Handles clicking on a buyable object
     *
     * @param buyable       the object's model
     * @param viewComponent the object's view
     */
    private void handleBuyableClick(Buyable buyable, BuyableView viewComponent) {
        if (view.getShownDotMap()) return;

        boolean huntAnimal = false;
        Ranger prevRanger = view.getPreviouslyClickedRanger();

        if (buyable instanceof Animal animal) {

            if (prevRanger != null && prevRanger.isInTargetingMode()) {
                prevRanger.putAnimalNotAlreadyHunting(animal);
                view.showPrey(animal.getView().getCircle());
                huntAnimal = true;
            } else {
                view.setPreviouslyClickedRanger(null);
                selectedAnimal = animal;
            }
        }
        if (buyable instanceof Plant || buyable instanceof Pond) {

            view.setPreviouslyClickedRanger(null);
            selectedAnimal = null;
        }
        if (buyable instanceof Ranger rangerClicked) {
            Ranger ranger = model.getRangers().stream()
                    .filter(r -> r.equals(rangerClicked))
                    .findFirst()
                    .orElse(rangerClicked);

            for (Animal animal : ranger.getTargets()) {
                view.showPrey(animal.getView().getCircle());
            }
            for (Poacher p : ranger.getPoacherTargets()) {
                view.showPrey(p.getView().getCircle());
            }

            boolean isSameRanger = (view.getPreviouslyClickedRanger() == ranger);
            boolean targetingNow;


            if (!isSameRanger) {
                ranger.setTargetingMode(false);
                targetingNow = false;
                view.setPreviouslyClickedRanger(ranger);
            } else {
                targetingNow = !ranger.isInTargetingMode();
                ranger.setTargetingMode(targetingNow);
                view.setPreviouslyClickedRanger(targetingNow ? ranger : null);
            }

            view.setShadows(ranger.getView().getCircle(), targetingNow);
            selectedAnimal = null;
            startContinuousUpdates(buyable, infoLines ->
                    view.setInfoContainer(infoLines, ranger.getView().getCircle(), targetingNow)
            );
            return;
        }

        if (buyable instanceof Jeep) {

            view.setPreviouslyClickedRanger(null);
            selectedAnimal = null;
        }

        if (!huntAnimal) {
            startContinuousUpdates(buyable, infoLines ->
                    view.setInfoContainer(infoLines, viewComponent.getCircle(), false)
            );
        }
    }

    /**
     * Handles clicking on a gate
     *
     * @param gate the gate's model
     */
    private void handleGateClick(Gate gate) {
        if (view.getShownDotMap()) return;

        selectedAnimal = null;

        view.setPreviouslyClickedRanger(null);
        startContinuousUpdates(gate, infoLines -> {
            view.setInfoContainer(infoLines, gate.getView().getRect(), false);
            view.showSlider();
        });
    }

    /**
     * Handles clicking on a poacher
     *
     * @param p           the poacher's model
     * @param poacherView the poacher's view
     */
    private void handlePoacherClick(Poacher p, PoacherView poacherView) {
        Ranger targetingRanger = model.getRangers().stream().filter(Ranger::isInTargetingMode).findFirst().orElse(null);
        if (targetingRanger != null) {
            if (!targetingRanger.getPoacherTargets().contains(p)) {
                targetingRanger.getPoacherTargets().add(p);
            }

            view.showPrey(poacherView.getCircle());
        } else {

            selectedAnimal = null;
            view.setPreviouslyClickedRanger(null);
            startContinuousUpdates(p, infoLines ->
                    view.setInfoContainer(infoLines, poacherView.getCircle(), false)
            );
        }
    }

    /**
     * @param c the given coordinate
     * @return whether the given coordinate is in vision of any bought object
     */
    private boolean isInAnyVision(Coordinate c) {
        for (Ranger ranger : model.getRangers()) {
            if (inVision(c, ranger)) return true;
        }
        for (Plant plant : model.getPlants()) {
            if (inVision(c, plant)) return true;
        }
        for (Pond pond : model.getPonds()) {
            if (inVision(c, pond)) return true;
        }
        for (Animal animal : model.getAnimals()) {
            if (animal.isChipped() && inVision(c, animal)) return true;
        }
        return false;
    }

    /**
     * @param c   the given coordinate
     * @param obj the object, which is being checked
     * @return whether the given coordinate is in the vision of the object
     */
    private boolean inVision(Coordinate c, Buyable obj) {
        double dx = obj.getCoord().getX() - c.getX();
        double dy = obj.getCoord().getY() - c.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= obj.getVision();
    }
}