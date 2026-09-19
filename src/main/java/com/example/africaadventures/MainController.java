package com.example.africaadventures;

import com.example.gamemodel.Difficulty;
import com.example.gamemodel.DifficultyGoals;
import com.example.gamemodel.GameModel;
import com.example.viewmodel.GameView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class MainController {

    private final Difficulty[] difficultyOptions = Difficulty.values();
    private Difficulty currentDifficulty = Difficulty.EASY;
    private Stage game;
    private Scene gameScene;
    private Parent root;
    @FXML private Label difficultyLabel, goalVisitor, goalMoney, goalCarnivore, goalHerbivore, goalMonth;

    /**
     * Move to difficulty page after pressing "START"
     * @param e
     * @throws IOException
     */
    @FXML
    protected void start(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DifficultyScene.fxml"));
        loadScene(e, loader);
    }

    private void loadScene(ActionEvent e, FXMLLoader loader) throws IOException {
        root = loader.load();
        game = (Stage) ((Node)e.getSource()).getScene().getWindow();
        gameScene = new Scene(root);
        gameScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        game.setScene(gameScene);
        game.show();
    }

    /**
     * Closes the application.
     * @param e
     */
    @FXML
    protected void exit(ActionEvent e)
    {
        System.out.println("Closing...");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Move to continue page
     * @param e
     * @throws IOException
     */
    @FXML
    protected void toContinue(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ContinueScene.fxml"));
        loadScene(e, loader);
    }

    /**
     * Returns back to home page.
     * @param e
     * @throws IOException
     */
    @FXML
    protected void toHome(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScene.fxml"));
        loadScene(e, loader);
    }

    /**
     * After choosing the difficulty starts the game
     * @param e
     * @throws IOException
     */
    @FXML
    protected void toGame(ActionEvent e) throws IOException {
        GameModel model = new GameModel();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/africaadventures/GameScene.fxml"));
        root = loader.load();
        GameView view = loader.getController();
        GameController controller = new GameController();

        game = (Stage) ((Node) e.getSource()).getScene().getWindow();
        gameScene = new Scene(root);
        gameScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/africaadventures/style.css")).toExternalForm());
        game.setScene(gameScene);
        game.setFullScreen(true);
        game.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        game.setFullScreenExitHint("");

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            game.setFullScreen(true);
            game.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
            game.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
            game.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        }

        game.show();
        GameController.layoutToScreen(root);
        controller.initGame(model, view, currentDifficulty) ;
    }

    /**
     * Cyclically moves to the next difficulty option.
     */
    @FXML
    protected void nextDifficulty() {
        int n = (currentDifficulty.ordinal() + 1)%3;
        difficultyLabel.setText((difficultyOptions[n].toString()));
        currentDifficulty = Difficulty.values()[n];
        setGoalLabels(n);
    }

    /**
     * Cyclically moves to the previous difficulty option.
     */
    @FXML
    protected void prevDifficulty() {
        int n = (currentDifficulty.ordinal()+2)%3;
        difficultyLabel.setText((difficultyOptions[n].toString()));
        currentDifficulty = Difficulty.values()[n];
        setGoalLabels(n);
    }

    /**
     * Initializes the labels for difficulty page.
     * @param n The current difficulty's ordinal
     */
    private void setGoalLabels(int n){
        goalMonth.setText("Month: " + DifficultyGoals.getMonth(n));
        goalVisitor.setText("Visitor: " + DifficultyGoals.getVisitor(n));
        goalMoney.setText("Money: " + DifficultyGoals.getMoney(n));
        goalCarnivore.setText("Carnivore: " + DifficultyGoals.getCarnivore(n));
        goalHerbivore.setText("Herbivore: " + DifficultyGoals.getHerbivore(n));
    }
}