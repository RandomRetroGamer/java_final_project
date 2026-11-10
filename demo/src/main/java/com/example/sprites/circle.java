package com.example.sprites;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class circle {
    private enum State { WANDER, CHASE, STRAFE, RETREAT }

    private double detect_range = 700.0;
    private double shoot_range = 500.0;
    private double prefered_distance = 320.0;

    private double too_close = 200.0;
    private double fire_cooldown = 80.0;
    private double aim_spread = 0.06;

    private double x, y, vx, vy;
    private double radius = 18.0;
    private double accel = 0.12;
    private double max_speed = 3.0;
    private double friction = 0.98;

    private State state = State.WANDER;
    private double wanderX, wanderY;

    private double strafeDirection = 1.0;
    private double strafeTimer = 0.0;
    private double fireTimer;

    private int health = 3;

    private final Random rng = new Random();

    private final List<Bullet> bullets = new ArrayList<>();

    

}
