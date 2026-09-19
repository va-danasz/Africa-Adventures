package com.example.africaadventures;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage mainStage) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("MainScene.fxml")));
        Scene mainScene = new Scene(root);
        mainScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());

        mainStage.setTitle("Africa Adventures");
        mainStage.setWidth(1280);
        mainStage.setHeight(720);

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/icon.png")));
        mainStage.getIcons().add(icon);
        mainStage.setScene(mainScene);
        mainStage.setResizable(false);
        mainStage.show();

        mainStage.setOnCloseRequest(event -> {
            System.out.println("Closing...");
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}