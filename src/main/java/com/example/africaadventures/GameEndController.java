package com.example.africaadventures;

import com.example.gamemodel.Difficulty;
import com.example.gamemodel.GameModel;
import com.example.viewmodel.GameView;
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

public class GameEndController {
    @FXML
    private Label infoLabel;
    private Stage game;
    private Scene gameScene;
    private Parent root;
    private Difficulty prevDifficulty;

    public void setInfo(String text) {
        infoLabel.setText(text);
    }

    /**
     * Returns back to home page.
     * @param e
     * @throws IOException
     */
    @FXML
    protected void toHome(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/africaadventures/MainScene.fxml"));
        root = loader.load();
        game = (Stage) ((Node) e.getSource()).getScene().getWindow();
        gameScene = new Scene(root);
        gameScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/africaadventures/style.css")).toExternalForm());
        game.setWidth(1280);
        game.setHeight(720);

        game.setResizable(false);
        game.setScene(gameScene);
        game.show();
    }

    /**
     * Adds the option to play again without leaving to the home page. Uses the same difficulty option as previously.
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
        controller.initGame(model, view, prevDifficulty);
    }

    public void setDifficulty(Difficulty currentDifficulty) {
        prevDifficulty = currentDifficulty;
    }
}

