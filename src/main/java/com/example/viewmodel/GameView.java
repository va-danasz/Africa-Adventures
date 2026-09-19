package com.example.viewmodel;

import com.example.africaadventures.DraggableMaker;
import com.example.africaadventures.GameEndController;
import com.example.gamemodel.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import static com.example.gamemodel.Coordinate.calcDistance;

public class GameView {
    private Canvas fogCanvas;
    private GraphicsContext fogGraphics;
    @FXML
    private Slider slider;
    private Consumer<ActionEvent> onReturnHome;
    private Consumer<ActionEvent> onSpeedChange;
    private Consumer<ActionEvent> onSaveGame;
    private Consumer<String> onCategorySelect;
    private BiConsumer<ShopItem, Coordinate> onConfirmPurchase;
    @FXML
    private ChoiceBox<String> speedChoiceBox;
    @FXML
    private Button plantMenu;
    @FXML
    private Button animalMenu;
    @FXML
    private Button utilMenu;
    @FXML
    public Button okBtn;
    @FXML
    public Button noBtn;
    @FXML
    private VBox shopContainer, infoContainer;
    @FXML
    Label infoLabel1, infoLabel2, infoLabel3, infoLabel4, moneyLabel, timeLabel, ticketLabel;
    @FXML
    private Pane gamePane;
    private Shape currentlyHighlighted = null;
    private boolean shownDotMap = false;
    private ShopItem lastPlaced;
    private final HashMap<Coordinate, Circle> dotMap = new HashMap<>();
    private final ArrayList<Circle> targetedAnimalCircles = new ArrayList<>();
    private final DraggableMaker draggableMaker = new DraggableMaker();
    private Circle newCircle;
    private Gate entranceGate, exitGate;
    private Ranger previouslyClickedRanger = null;
    private Set<List<Integer>> allPaths = new HashSet<>();
    private Path lastPath = null;
    private Coordinate entryPoint, exitPoint;
    private Coordinate endPoint1 = null;

    public GameView() {
    }

    public Coordinate getExitPoint() {
        return exitPoint;
    }

    public void setEndPoint1(Coordinate endPoint1) {
        this.endPoint1 = endPoint1;
    }

    public void setPreviouslyClickedRanger(Ranger r) {
        this.previouslyClickedRanger = r;
    }

    public Ranger getPreviouslyClickedRanger() {
        return previouslyClickedRanger;
    }

    public void addNode(Node node) {
        gamePane.getChildren().add(node);
    }

    public Pane getGamePane() {
        return gamePane;
    }

    /**
     * Adds background to the game scene
     */
    public void initBackground() {
        Image img = new Image(Objects.requireNonNull(getClass().getResource("/com/example/africaadventures/images/ground.png")).toExternalForm());

        BackgroundImage backgroundImage = new BackgroundImage(
                img, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        gamePane.setBackground(new Background(backgroundImage));
    }

    /**
     * Initializes the night's "fog-of-war" effect
     */
    public void initFogCanvas() {
        fogCanvas = new Canvas(gamePane.getWidth(), gamePane.getHeight());
        fogCanvas.setMouseTransparent(true);
        gamePane.getChildren().add(fogCanvas);
        fogGraphics = fogCanvas.getGraphicsContext2D();
    }

    public double getWidth() {
        return gamePane.getWidth();
    }

    public double getHeight() {
        return gamePane.getHeight();
    }

    /**
     * Removes an object visually
     *
     * @param view
     */
    public void removeBuyableView(BuyableView view) {
        gamePane.getChildren().remove(view.getCircle());
        infoContainer.setVisible(false);
    }

    /**
     * Removes an object visually
     *
     * @param view
     */
    public void removePoacherView(PoacherView view) {
        if (view != null) gamePane.getChildren().remove(view.getCircle());
    }

    /**
     * Initializes the gates' appearance
     *
     * @param entranceGate
     * @param exitGate
     */
    public void initGates(Gate entranceGate, Gate exitGate) {
        this.entranceGate = entranceGate;
        this.exitGate = exitGate;
        gamePane.getChildren().add(entranceGate.getView().getRect());
        gamePane.getChildren().add(exitGate.getView().getRect());
    }

    /**
     * sets the timer's label to the given string value
     *
     * @param formattedTime the current in-game time formatted
     */
    public void setTimeLabel(String formattedTime) {
        Platform.runLater(() -> timeLabel.setText(formattedTime));
    }

    /**
     * Initializes the slider for the ticket slider
     *
     * @param modelPrice the base price of the tickets
     */
    public void initSlider(int modelPrice) {
        slider.setMin(1000);
        slider.setMax(5000);
        slider.setValue(modelPrice);
        slider.setMajorTickUnit(500);
        slider.setBlockIncrement(250);
        slider.setMinorTickCount(1);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);

        if (ticketLabel != null)
            ticketLabel.textProperty().bind(slider.valueProperty().asString("Ticket price: %.0f"));
    }

    public Slider getSlider() {
        return slider;
    }

    public void setOnReturnHome(Consumer<ActionEvent> handler) {
        this.onReturnHome = handler;
    }

    public void setOnSpeedChange(Consumer<ActionEvent> handler) {
        this.onSpeedChange = handler;
    }

    public void setOnCategorySelect(Consumer<String> handler) {
        this.onCategorySelect = handler;
    }

    public void setOnConfirmPurchase(BiConsumer<ShopItem, Coordinate> handler) {
        this.onConfirmPurchase = handler;
    }

    public void setOnSaveGame(Consumer<ActionEvent> handler) {
        this.onSaveGame = handler;
    }

    /**
     * Returns back to home page.
     *
     * @param e
     * @throws IOException
     */
    @FXML
    protected void toHome(ActionEvent e) throws IOException {
        if (onReturnHome != null) {
            onReturnHome.accept(e);
        }
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/africaadventures/MainScene.fxml"));
        Parent root = loader.load();

        Scene gameScene = new Scene(root);
        gameScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/africaadventures/style.css")).toExternalForm());

        stage.setScene(gameScene);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * crates a save from the current model state
     *
     * @param e
     */
    @FXML
    protected void saveGame(ActionEvent e) {
        if (!shownDotMap) {
            if (onSaveGame != null) {
                onSaveGame.accept(e);
            }
        }
    }

    /**
     * It can finalize the purchase of an object
     */
    @FXML
    private void purchaseConfirm() {
        clearAfterShop();
        if (!lastPlaced.getName().equals("Road") && !lastPlaced.getName().equals("Jeep")) {
            for (Circle dot : dotMap.values()) {
                if ((newCircle).getBoundsInParent().intersects(dot.getBoundsInParent()) && dot.getFill().equals(Color.RED)) {
                    return;
                }
            }
        }
        if (onConfirmPurchase != null) {
            onConfirmPurchase.accept(lastPlaced, draggableMaker.getMouseLocation());
        }
    }

    /**
     * Deletes the draggable object, canceling the current purchase
     */
    @FXML
    private void purchaseCancel() {
        if (lastPath != null) removeLastPath(lastPath);
        clearAfterShop();
    }

    /**
     * Sets the visual elements' to the correct state
     */
    public void clearAfterShop() {
        gamePane.getChildren().remove(newCircle);
        okBtn.setVisible(false);
        noBtn.setVisible(false);
        shopContainer.setVisible(false);
        gamePane.getChildren().removeAll(getDotMap().values());
        setShownDotMap(false);
        lastPath = null;
    }

    /**
     * Initializes the shop and the other elements, with the categories and the ok/cancel buttons, labels
     *
     * @param baseMoney the starting money
     */
    public void initShopUI(int baseMoney) {
        speedChoiceBox.getItems().addAll("Slow", "Normal", "Fast");
        speedChoiceBox.getSelectionModel().select("Slow");
        speedChoiceBox.setOnAction(e -> {
            if (onSpeedChange != null) onSpeedChange.accept(e);
        });

        moneyLabel.setText("Money: " + baseMoney);

        plantMenu.setOnAction(e -> {
            gamePane.getChildren().removeAll(getDotMap().values());
            if (onCategorySelect != null) onCategorySelect.accept("Plants");
        });

        animalMenu.setOnAction(e -> {
            gamePane.getChildren().removeAll(getDotMap().values());
            if (onCategorySelect != null) onCategorySelect.accept("Animals");
        });

        utilMenu.setOnAction(e -> {
            gamePane.getChildren().removeAll(getDotMap().values());
            if (onCategorySelect != null) onCategorySelect.accept("Utilities");
        });

        okBtn.setVisible(false);
        noBtn.setVisible(false);
    }

    public ChoiceBox<String> getSpeedChoiceBox() {
        return speedChoiceBox;
    }

    /**
     * After selecting a category it's elements appear
     *
     * @param category the selected category
     */
    public void showCategory(ShopCategory category) {
        shopContainer.getChildren().clear();
        shopContainer.getChildren().add(category.getView().getCategoryBox());
        draggableMaker.setShopActive(true);
        shopContainer.setVisible(true);
        infoContainer.setVisible(false);
    }

    /**
     * Turns on/off the shop, visually changing the appearance of the sidebar
     *
     * @param e
     */
    public void showShop(ActionEvent e) {
        clearTargetedAnimalStrokes();
        if (currentlyHighlighted != null) {
            currentlyHighlighted.setEffect(null);
        }
        draggableMaker.setShopActive(false);
        plantMenu.setVisible(!plantMenu.isVisible());
        animalMenu.setVisible(!animalMenu.isVisible());
        utilMenu.setVisible(!utilMenu.isVisible());
        infoContainer.setVisible(false);
        shopContainer.setVisible(false);

        if (!plantMenu.isVisible()) {
            gamePane.getChildren().removeAll(getDotMap().values());
            setShownDotMap(false);
            okBtn.setVisible(false);
            noBtn.setVisible(false);
            if (lastPlaced != null) gamePane.getChildren().remove(lastPlaced);
        }
    }

    public HashMap<Coordinate, Circle> getDotMap() {
        return dotMap;
    }

    /**
     * After clicking away from a ranger, clears the strokes
     */
    public void clearTargetedAnimalStrokes() {
        for (Circle c : targetedAnimalCircles) {
            c.setStroke(null);
        }
        targetedAnimalCircles.clear();
    }

    public void setShownDotMap(boolean shownDotMap) {
        this.shownDotMap = shownDotMap;
    }

    /**
     * Creates the draggable object for shopping
     *
     * @param item
     */
    public void handlePurchaseInit(ShopItem item) {
        double startX = 50, startY = 50;
        okBtn.setVisible(true);
        noBtn.setVisible(true);

        if (newCircle != null) {
            gamePane.getChildren().remove(newCircle);
            newCircle = null;
        }

        lastPlaced = item;
        if (item.getName().equals("Road") || item.getName().equals("Jeep")) {
            return;
        }

        newCircle = new Circle(20);
        newCircle.setCenterX(startX);
        newCircle.setCenterY(startY);
        newCircle.setFill(Color.WHITE);
        draggableMaker.makeDraggable(newCircle);
        draggableMaker.setDefaultMouseLocation();
        gamePane.getChildren().add(newCircle);
    }

    public boolean getShownDotMap() {
        return shownDotMap;
    }

    /**
     * Creating a HashMap of coordinates and circles to visualize possible places to set down objects.
     *
     * @param width
     * @param height
     * @param objects
     * @return
     */
    public HashMap<Coordinate, Circle> createPoints(double width, double height, List<Buyable> objects) {
        ArrayList<CanBuy> allowDotMaps = new ArrayList<>(Arrays.asList(CanBuy.ANIMAL, CanBuy.PATH, CanBuy.JEEP, CanBuy.RANGER));
        int size = 25;
        setShownDotMap(true);
        ArrayList<Coordinate> points = new ArrayList<>();
        dotMap.clear();
        double dotRadius = 5.0;
        for (double i = dotRadius; i < width; i += size) {
            for (double j = dotRadius; j < height; j += size) {
                Coordinate coord = new Coordinate(i, j);
                points.add(coord);

                Circle dot = new Circle(i, j, dotRadius, Color.GREEN);
                dotMap.put(coord, dot);
            }
        }

        for (Buyable obj : objects) {
            if (!allowDotMaps.contains(obj.getType())) {
                for (Coordinate coord : points) {
                    double dx = coord.getX() - obj.getView().getCircle().getCenterX();
                    double dy = coord.getY() - obj.getView().getCircle().getCenterY();
                    double distanceSquared = dx * dx + dy * dy;

                    if (Math.sqrt(distanceSquared) < obj.getView().getCircle().getRadius() + size && obj.getView().getCircle().getFill() != Color.AZURE || collideWithRectangle(coord, entranceGate) || collideWithRectangle(coord, exitGate)) {
                        if (dotMap.get(coord) != null) {
                            dotMap.get(coord).setFill(Color.RED);
                        }
                    }
                }
            }
        }
        entryPoint = findPoint(entranceGate, dotMap);

        dotMap.get(entryPoint).setFill(Color.ORANGE);
        exitPoint = findPoint(exitGate, dotMap);
        dotMap.get(exitPoint).setFill(Color.ORANGE);
        return dotMap;
    }

    /**
     * This method sets the starting and end coordinate for the entry and exit gates, as well as the roads.
     *
     * @param gate
     * @param dotMap
     * @return
     */
    private Coordinate findPoint(Gate gate, HashMap<Coordinate, Circle> dotMap) {
        Coordinate closestCoord = null;
        double minDistance = Double.MAX_VALUE;

        for (Coordinate dotCoord : dotMap.keySet()) {
            double dx = dotCoord.getX() - gate.getView().getRect().getX();
            double dy = dotCoord.getY() - gate.getView().getRect().getY();
            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared < minDistance) {
                minDistance = distanceSquared;
                closestCoord = new Coordinate(dotCoord.getX(), dotCoord.getY());
            }
        }
        return closestCoord;
    }

    private boolean collideWithRectangle(Coordinate coord, Gate rect) {
        return dotMap.get(coord).getBoundsInParent().intersects(rect.getView().getRect().getBoundsInParent());
    }

    public void shopMenuAction(List<Buyable> objects) {
        gamePane.getChildren().addAll(createPoints(gamePane.getWidth(), gamePane.getHeight(), objects).values());
    }

    public void refreshMoney(double currentMoney) {
        moneyLabel.setText("Money: " + currentMoney);
    }

    public Gate getEntranceGate() {
        return entranceGate;
    }

    public void setLastPlacedNull() {
        lastPlaced = null;
        newCircle = null;
    }

    /**
     * sets the container's infos
     *
     * @param infos        the list of the infos
     * @param sourceCircle the clicked object
     * @param isTargeting  whether a ranger is in targeting mode
     */
    public void setInfoContainer(ArrayList<String> infos, Shape sourceCircle, boolean isTargeting) {
        infoContainer.getChildren().clear();
        infoContainer.setVisible(true);
        shopContainer.setVisible(false);
        for (String info : infos) {
            Label label = new Label(info);
            infoContainer.getChildren().add(label);
        }

        setShadows(sourceCircle, isTargeting);
    }

    /**
     * Adds a light shadow to the selected object
     *
     * @param sourceCircle the clicked object
     * @param isTargeting  whether a ranger is in targeting mode
     */
    public void setShadows(Shape sourceCircle, boolean isTargeting) {
        DropShadow infoHighlightEffect = new DropShadow(20, Color.BLACK);
        DropShadow rangerTargetingEffect = new DropShadow(40, Color.RED);
        if (currentlyHighlighted != null) {
            currentlyHighlighted.setEffect(null);
        }

        if (sourceCircle != null) {
            sourceCircle.setEffect(isTargeting ? rangerTargetingEffect : infoHighlightEffect);
            currentlyHighlighted = sourceCircle;

            if (!isTargeting) {
                clearTargetedAnimalStrokes();
            }
        }
    }

    /**
     * Selecting a ranger shows its targets
     *
     * @param circle
     */
    public void showPrey(Circle circle) {
        if (!targetedAnimalCircles.contains(circle)) {
            circle.setStroke(Color.ORANGE);
            circle.setStrokeWidth(3);
            targetedAnimalCircles.add(circle);
        }
    }

    /**
     * Handles the purchase of roads
     *
     * @param newPath
     * @return
     */
    public Path initRoadView(Path newPath) {
        if (endPoint1 == null) {
            endPoint1 = new Coordinate();
        }
        for (Map.Entry<Coordinate, Circle> entry : dotMap.entrySet()) {
            Circle dot = entry.getValue();
            dot.setOnMouseClicked(event -> {

                if (dot.getFill() != Color.RED) {

                    if (endPoint1.getX() == 0 && endPoint1.getY() == 0) {
                        endPoint1 = entry.getKey();

                        dot.setFill(Color.BLACK);
                        newPath.addVertex(endPoint1);
                    }

                    HashMap<Coordinate, Circle> availableDots = new HashMap<>();
                    double maxDistance = 500;

                    for (Map.Entry<Coordinate, Circle> entry2 : dotMap.entrySet()) {
                        Circle possibleDot = entry2.getValue();
                        Coordinate coord2 = entry2.getKey();

                        if (possibleDot.getFill() != Color.RED && calcDistance(endPoint1, coord2) < maxDistance) {
                            availableDots.put(coord2, possibleDot);
                        }
                    }

                    if (!availableDots.isEmpty()) {
                        for (Map.Entry<Coordinate, Circle> entry3 : availableDots.entrySet()) {
                            Circle dot2 = entry3.getValue();
                            dot2.setOnMouseClicked(event2 -> {
                                Coordinate selectedCoord = entry3.getKey();
                                Coordinate endPoint2 = new Coordinate(selectedCoord.getX(), selectedCoord.getY());
                                dot2.setFill(Color.BLACK);

                                RoadPiece rp = new RoadPiece(250, new Coordinate(endPoint1.getX(), endPoint1.getY()),
                                        new Coordinate(endPoint2.getX(), endPoint2.getY()));
                                RoadPieceView view = new RoadPieceView(rp);
                                rp.setView(view);
                                addNode(view.getRectangle());
                                newPath.addRoadPiece(rp);
                                newPath.addVertex(endPoint2);
                                newPath.addEdge(endPoint1, endPoint2);
                                newPath.getVertexCount();
                                endPoint1 = new Coordinate(endPoint2.getX(), endPoint2.getY());

                                if (newPath.isConnected(entryPoint, exitPoint)) {
                                    int entryIndex = newPath.getIndex(entryPoint);
                                    int exitIndex = newPath.getIndex(exitPoint);

                                    if (entryIndex != -1 && exitIndex != -1) {
                                        newPath.initAdjList();
                                        allPaths = newPath.printAllPaths(entryIndex, exitIndex);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

        lastPath = newPath;
        return newPath;
    }

    /**
     * removes the last placed path upon canceling the purchase
     *
     * @param path
     */
    public void removeLastPath(Path path) {
        for (RoadPiece rp : path.getPath()) {
            gamePane.getChildren().remove(((RoadPieceView) (rp.getView())).getRectangle());
        }
    }

    public void clearFogCanvas() {
        fogGraphics.clearRect(0, 0, fogCanvas.getWidth(), fogCanvas.getHeight());
    }

    public void setDayLight(Animal animal, boolean visible) {
        animal.getView().getCircle().setVisible(visible);
        animal.getView().getCircle().setDisable(!visible);
    }

    public void initNightFog() {
        fogGraphics.setFill(Color.rgb(0, 0, 0, 0.3));
        fogGraphics.fillRect(0, 0, fogCanvas.getWidth(), fogCanvas.getHeight());

        fogGraphics.setFill(Color.rgb(0, 0, 0, 0.0));
        fogGraphics.setStroke(Color.WHITE);
    }

    public void clearFog(Buyable obj, double step) {
        double radius = obj.getVision();
        double x = obj.getCoord().getX();
        double y = obj.getCoord().getY();
        clearCanvasFog(radius, x, y, step);
    }

    /**
     * Cuts out a "hole" from the fog's black canvas
     *
     * @param radius the hole's radius
     * @param x      hole's coordinate
     * @param y      hole's coordinate
     * @param step   the fineness of the circle
     */
    public void clearCanvasFog(double radius, double x, double y, double step) {
        for (double dx = -radius; dx <= radius; dx += step) {
            for (double dy = -radius; dy <= radius; dy += step) {
                if (dx * dx + dy * dy <= radius * radius) {
                    fogGraphics.clearRect(x + dx, y + dy, step, step);
                }
            }
        }
    }


    public void animalFog(Animal animal, double step, AtomicBoolean isNight, boolean isInVision) {
        if (animal.isChipped()) clearFog(animal, step);
        else {
            if (isNight.get()) {
                setDayLight(animal, isInVision);
            } else {
                setDayLight(animal, true);
            }
        }
    }

    /**
     * Redirects the player to the gameEndScene, changes its content according to losing or winning
     *
     * @param won               the player has won or lost
     * @param timeCounter       current in-game time
     * @param currentDifficulty the difficulty of the game
     */
    public void gameOver(boolean won, int timeCounter, Difficulty currentDifficulty) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/africaadventures/GameEndScene.fxml"));
                Parent endRoot = loader.load();
                GameEndController endController = loader.getController();
                endController.setDifficulty(currentDifficulty);
                if (won) endController.setInfo("Congrats, you won!");
                else
                    endController.setInfo("Game over, your safari operated for " + timeCounter + " hours\nGood luck next time!");

                Scene endScene = new Scene(endRoot);
                endScene.getStylesheets().add(getClass().getResource("/com/example/africaadventures/style.css").toExternalForm());

                Stage stage = (Stage) gamePane.getScene().getWindow();
                stage.setScene(endScene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Turns the slider's visibility on/off
     */
    public void showSlider() {
        if (!infoContainer.getChildren().contains(slider)) {
            infoContainer.getChildren().add(slider);
        }
        if (!infoContainer.getChildren().contains(ticketLabel)) {
            infoContainer.getChildren().add(ticketLabel);
        }
        slider.setVisible(true);
        ticketLabel.setVisible(true);
    }
}