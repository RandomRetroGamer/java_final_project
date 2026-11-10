package com.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {
    private final List<Circle> mobs = new ArrayList<>();
    
    window win = new window();
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final Star[] stars = createStars();
    private static final double STAR_PARALLAX = 0.35;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(win.window_width, win.window_height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Triangle triangle = new Triangle(
                (win.window_width - 100) / 4.0,
                (win.window_height - 100) / 4.0,
                Color.LIGHTBLUE);

        Text text = new Text(20, 40, String.format("Speed: %.2f", triangle.getSpeed()));
        text.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 14));

        text.setFill(Color.WHITE);
        text.setStroke(Color.LIGHTGRAY);
        
        text.setStrokeWidth(0.5);

        Pane root = new Pane(canvas, triangle, text);
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
                render(gc, triangle, now);
                text.setText(String.format("Speed: %.2f", triangle.getSpeed()));
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

        triangle.setTranslateX(win.window_width / 2.0 - 25.0);
        triangle.setTranslateY(win.window_height / 2.0 - 25.0);
    }

    private void render(GraphicsContext gc, Triangle triangle, long now) {
        gc.setFill(Color.rgb(3, 5, 20));
        gc.fillRect(0, 0, win.window_width, win.window_height);

        double cameraX = triangle.getWorldX() + 25.0 - win.window_width / 2.0;
        double cameraY = triangle.getWorldY() + 25.0 - win.window_height / 2.0;
        double time = now / 1_000_000_000.0;

        for (Star star : stars) {

            double x = wrap(star.x - cameraX * STAR_PARALLAX, win.window_width);
            double y = wrap(star.y - cameraY * STAR_PARALLAX, win.window_height);

            double twinkle = 0.7 + 0.3 * Math.sin(time * star.twinkleSpeed + star.phase);
            int brightness = (int) (star.brightness * twinkle);

            gc.setFill(Color.rgb(brightness, brightness, Math.min(255, brightness + 35)));
            gc.fillOval(x, y, star.radius * 2.0, star.radius * 2.0);
        }
    }

    private Star[] createStars() {
        Random random = new Random(42);
        Star[] result = new Star[220];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Star(

                    random.nextDouble() * win.window_width,
                    random.nextDouble() * win.window_height,
                    0.6 + random.nextDouble() * 1.4,

                    110 + random.nextInt(146),
                    0.5 + random.nextDouble() * 2.0,

                    random.nextDouble() * Math.PI * 2.0);
        }
        return result;
    }

    private double wrap(double value, double size) {
        return (value % size + size) % size;
    }

    private static final class Star {
        private final double x;
        private final double y;

        private final double radius;
        private final int brightness;

        private final double twinkleSpeed;
        private final double phase;

        private Star(double x, double y, double radius, int brightness, double twinkleSpeed, double phase) {
            this.x = x;
            this.y = y;

            this.radius = radius;
            this.brightness = brightness;

            this.twinkleSpeed = twinkleSpeed;
            this.phase = phase;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
