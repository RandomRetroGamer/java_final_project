package com.example.sprites;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Triangle extends Polygon {
    private double vx = 0.0;
    private double vy = 0.0;

    private double acceleration = 0.12;
    private double friction = 0.98;
    private double maxSpeed = 5.0;
    private double targetAngle = 0.0;

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
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setFill(color);
    }

    public void moveDown() {
        targetAngle = 0.0;
        vy += acceleration;
    }

    public void moveLeft() {
        targetAngle = 90.0;
        vx -= acceleration;
    }

    public void moveUp() {
        targetAngle = 180.0;
        vy -= acceleration;
    }

    public void moveRight() {
        targetAngle = -90.0;
        vx += acceleration;
    }

    public void moveUpLeft(){
        targetAngle = 135.0;
        vy -= acceleration;
    }

    public void updateMovement() {

        double currentSpeed = Math.hypot(vx, vy);
        if (currentSpeed > maxSpeed) {
            vx = (vx / currentSpeed) * maxSpeed;
            vy = (vy / currentSpeed) * maxSpeed;
        }


        setTranslateX(getTranslateX() + vx);
        setTranslateY(getTranslateY() + vy);


        vx *= friction;
        vy *= friction;

        if (Math.abs(vx) < 0.01) vx = 0;
        if (Math.abs(vy) < 0.01) vy = 0;

    }

    public double getSpeed() {
        return Math.hypot(vx, vy);
    }



    public void updateRotation() {
        double current = getRotate();
        double diff = ((targetAngle - current + 540) % 360) - 180;

        if (Math.abs(diff) < 1.0) {
            setRotate(targetAngle);
        } else {
            setRotate(current + diff * 0.25);
        }
    }
}
