package com.example;

import java.util.HashSet;
import java.util.Set;

import com.example.sprites.Triangle;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {

    window win = new window();
    private final Set<KeyCode> activeKeys = new HashSet<>();

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(win.window_width, win.window_height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Triangle triangle = new Triangle(
                (win.window_width - 100) / 4.0,
                (win.window_height - 100) / 4.0,
                Color.BLUE);

        Pane root = new Pane(canvas, triangle);
        root.setPrefSize(win.window_width, win.window_height);
        root.setFocusTraversable(true);
        Scene scene = new Scene(root, win.window_width, win.window_height);


        scene.setOnKeyPressed(event -> activeKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        root.requestFocus();

   
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePlayer(triangle);
                render(gc);
            }
        }.start();
    }

      private void updatePlayer(Triangle triangle) {
        if (activeKeys.contains(KeyCode.W)) triangle.moveUp();
        if (activeKeys.contains(KeyCode.S)) triangle.moveDown();
        if (activeKeys.contains(KeyCode.A)) triangle.moveLeft();
        if (activeKeys.contains(KeyCode.D)) triangle.moveRight();

        triangle.updateMovement();
        triangle.updateRotation();


        double maxX = win.window_width - triangle.getBoundsInLocal().getWidth();
        double maxY = win.window_height - triangle.getBoundsInLocal().getHeight();
        triangle.setTranslateX(Math.max(0, Math.min(maxX, triangle.getTranslateX())));
        triangle.setTranslateY(Math.max(0, Math.min(maxY, triangle.getTranslateY())));

        
    }

    private void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, win.window_width, win.window_height);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
