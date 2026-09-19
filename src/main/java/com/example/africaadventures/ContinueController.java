package com.example.africaadventures;

import com.example.gamemodel.GameModel;
import com.example.viewmodel.GameView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;

public class ContinueController {

    /**
     * Returns back to home page.
     * @param e
     * @throws IOException
     */
    @FXML
    protected void toHome(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScene.fxml"));
        Parent root = loader.load();
        Stage game = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene gameScene = new Scene(root);
        gameScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        game.setScene(gameScene);
        game.show();
    }

    /**
     * Loads a saved game from a file, handles exceptions.
     * @param e
     */
    @FXML
    protected void loadGame(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a saved game");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save Files (*.sav)", "*.sav"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile))) {
                GameModel loadedModel = (GameModel) in.readObject();
                loadedModel.reconnectReferences();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/africaadventures/GameScene.fxml"));
                Parent root = loader.load();
                GameView view = loader.getController();
                GameController controller = new GameController();

                Scene gameScene = new Scene(root);
                gameScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/africaadventures/style.css")).toExternalForm());
                stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(gameScene);
                stage.setFullScreen(true);
                stage.show();
                GameController.layoutToScreen(root);
                controller.prepareGame(loadedModel, view, loadedModel.getDifficulty());

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
