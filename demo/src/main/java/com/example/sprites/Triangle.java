package com.example.sprites;
/*
 * this is the player class that will be used in app.java
 * we have somethings to go over, the triangle class is connected to the
 * spec class which has the default speed and acceleration of the triangle,
 * my plan is to make this a wave / rouge lite game where the players has to 
 * upgrade to keep up with the difficulty of the game.
 */
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Triangle extends Polygon {
    private double vx = 0.0;
    private double vy = 0.0;

    private double worldX = 0.0;
    private double worldY = 0.0;

    private double friction = 0.98;
    private double targetAngle = 0.0;

    // direction keys held this frame (-1, 0, or 1 on each axis after combining)
    private int inputX = 0;
    private int inputY = 0;

    specs s = new specs();
    upgrades u = new upgrades();
    private double acceleration = s.user_accerlation * u.default_speed_multiplier;
    private double maxSpeed = s.user_speed;

    public Triangle() {
        this.getPoints().addAll(
            0.0, 0.0,
            25.0, 50.0,
            50.0, 0.0
        );
        this.setFill(Color.BLUE);
    }

    public Triangle(double x, double y, Color color) {
        this();
        worldX = x;
        worldY = y;
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setFill(color);
    }

    public void moveDown() {
        inputY += 1;
        vy += acceleration;
    }

    public void moveLeft() {
        inputX -= 1;
        vx -= acceleration;
    }

    public void moveUp() {
        inputY -= 1;
        vy -= acceleration;
    }

    public void moveRight() {
        inputX += 1;
        vx += acceleration;
    }

    public void updateMovement() {

        double currentSpeed = Math.hypot(vx, vy);
        if (currentSpeed > maxSpeed) {
            vx = (vx / currentSpeed) * maxSpeed;
            vy = (vy / currentSpeed) * maxSpeed;
        }

        worldX += vx;
        worldY += vy;
        setTranslateX(worldX);
        setTranslateY(worldY);

        vx *= friction;
        vy *= friction;

        if (Math.abs(vx) < 0.01) vx = 0;
        if (Math.abs(vy) < 0.01) vy = 0;
    }

    public double getSpeed() {
        return Math.hypot(vx, vy);
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getWorldX() {
        return worldX;
    }

    public double getWorldY() {
        return worldY;
    }

    public void updateRotation() {
        // Build the target angle from the combined input direction.
        // Tip points down at 0 degrees: down=0, left=90, up=180, right=-90,
        // so diagonals like up-left come out as 135 automatically.
        // If no keys are held (or opposing keys cancel), keep the last angle.
        if (inputX != 0 || inputY != 0) {
            targetAngle = Math.toDegrees(Math.atan2(-inputX, inputY));
        }
        inputX = 0;
        inputY = 0;

        double current = getRotate();
        double diff = ((targetAngle - current + 540) % 360) - 180;

        if (Math.abs(diff) < 1.0) {
            setRotate(targetAngle);
        } else {
            setRotate(current + diff * 0.25);
        }
    }
}