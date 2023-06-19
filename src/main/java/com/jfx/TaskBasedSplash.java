package com.jfx;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;

/**
 * Example of displaying a splash page for a standalone JavaFX application
 */
@RequiredArgsConstructor
public class TaskBasedSplash {

    public static final String SPLASH_IMAGE =
            "/img/splash.png";

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;

    private final Runnable completionHandler;
    private final Task<?> task;
    private static final int SPLASH_WIDTH = 676;
    private static final int SPLASH_HEIGHT = 227;
    private Stage initStage;

    public void init() {
        initStage = new Stage();
        ImageView splash = new ImageView(new Image(
                SPLASH_IMAGE
        ));
        splash.setFitWidth(215);
        splash.setFitHeight(215);
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        progressText = new Label("启动中 . . .");
        progressText.setTextFill(Color.WHITE);
        splashLayout = new VBox();
//        splashLayout.setPrefWidth(SPLASH_WIDTH);
//        splashLayout.setPrefHeight(SPLASH_HEIGHT);
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                        "-fx-background-color: rgba(94,188,255,0.58); " +
                        "-fx-border-width:5; " +
                        "-fx-border-color: " +
                        "linear-gradient(" +
                        "to bottom, " +
                        "chocolate, " +
                        "derive(chocolate, 50%)" +
                        ");"
        );
        splashLayout.setEffect(new DropShadow());
        showSplash(
                initStage,
                task,
                completionHandler
        );
    }


    private void showSplash(
            final Stage initStage,
            Task<?> task,
            Runnable completionHandler
    ) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().set(-1);
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.8), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> {
                    initStage.hide();
                    completionHandler.run();
                });
                fadeSplash.play();
            }
        });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
//        initStage.setMaxWidth(SPLASH_WIDTH);
//        initStage.setMaxHeight(SPLASH_HEIGHT);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - (double) SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - (double) (SPLASH_HEIGHT + 260) / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public Stage getInitStage() {
        return initStage;
    }
}