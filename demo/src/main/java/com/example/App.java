package com.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * JavaFX for final project java.
 */

public class App extends Application {

    private static Scene scene;
    window win = new window();
    
    @Override
    public void start(Stage stage) {
        
        Canvas canvas = new Canvas(win.window_width, win.window_height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        
        StackPane root = new StackPane(canvas);
        scene = new Scene(root, win.window_width, win.window_height);

        stage.setScene(scene);
        stage.setResizable(false); 
        stage.show();


        new AnimationTimer() {
            @Override
            public void handle(long now) {
                render(gc);
            }
        }.start();
    }

    private void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, win.window_width, win.window_height);
        
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}
