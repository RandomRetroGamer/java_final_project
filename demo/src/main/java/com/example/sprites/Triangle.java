package com.example.sprites;
/*
 * this is the player class that will be used in app.java
 * we have somethings to go over, the triangle class is connected to the
 * spec class which has the default speed and acceleration of the triangle,
 *
 * Units: vx, vy, and getSpeed() are in pixels per second.
 * updateMovement(dt) and updateRotation(dt) must be called once per frame,
 * after the movement keys are read, and before the next frame.
 */
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Triangle extends Polygon {

    private static final double FPS = 60.0;

    private double vx = 0.0;
    private double vy = 0.0;

    private double worldX = 0.0;
    private double worldY = 0.0;

    private final double friction = 0.98;     // retained per 1/60 s
    private double targetAngle = 0.0;

    // direction keys held this frame (-1, 0, or 1 on each axis after combining)
    private int inputX = 0;
    private int inputY = 0;

    specs s = new specs();
    upgrades u = new upgrades();

    // px/s^2 and px/s
    private final double acceleration = s.user_accerlation * u.default_speed_multiplier * FPS * FPS;
    private final double maxSpeed = s.user_speed * FPS;

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

    // The move methods only record which keys are held. The actual
    // acceleration is applied in updateMovement(dt).
    public void moveDown()  { inputY += 1; }
    public void moveLeft()  { inputX -= 1; }
    public void moveUp()    { inputY -= 1; }
    public void moveRight() { inputX += 1; }

    public void updateMovement(double dt) {
        vx += inputX * acceleration * dt;
        vy += inputY * acceleration * dt;

        double currentSpeed = Math.hypot(vx, vy);
        if (currentSpeed > maxSpeed) {
            vx = (vx / currentSpeed) * maxSpeed;
            vy = (vy / currentSpeed) * maxSpeed;
        }

        worldX += vx * dt;
        worldY += vy * dt;
        setTranslateX(worldX);
        setTranslateY(worldY);

        double damping = Math.pow(friction, dt * FPS);
        vx *= damping;
        vy *= damping;

        // threshold in px/s (was 0.01 px per frame)
        if (Math.abs(vx) < 0.6) vx = 0;
        if (Math.abs(vy) < 0.6) vy = 0;
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

    // Must run after updateMovement(dt) in the same frame, because it consumes
    // the input flags that the move methods set.
    public void updateRotation(double dt) {
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
            // was a fixed 25% step per frame
            double t = 1 - Math.pow(0.75, dt * FPS);
            setRotate(current + diff * t);
        }
    }
}
