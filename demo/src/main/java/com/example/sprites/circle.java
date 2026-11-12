package com.example.sprites;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Circle {

    private enum State { WANDER, CHASE, STRAFE, RETREAT }


    private static final double DETECT_RANGE = 700;
    private static final double SHOOT_RANGE = 550;
    private static final double PREFERRED_DIST = 320;
    private static final double TOO_CLOSE = 200;
    private static final int FIRE_COOLDOWN = 220;    
    private static final double AIM_SPREAD = 0.06;   

    private double x, y, vx, vy;
    private final double radius = 18;
    private final double accel = 0.12;
    private final double maxSpeed = 2.2;
    private final double friction = 0.96;

    private State state = State.WANDER;
    private double wanderX, wanderY;
    private int strafeDir = 1;
    private int strafeTimer = 0;
    private int fireTimer;
    private int health = 3;

    private final Random rng = new Random();
    private final List<Bullet> bullets = new ArrayList<>();

    public Circle(double x, double y) {
        this.x = x;
        this.y = y;
        this.fireTimer = 30 + rng.nextInt(FIRE_COOLDOWN);
        pickWanderTarget();
    }

  
    public void update(double px, double py, double pvx, double pvy, List<Circle> others) {
        double dx = px - x, dy = py - y;
        double dist = Math.hypot(dx, dy);

        chooseState(dist);

        double dirX = 0, dirY = 0;
        double nx = dist > 0 ? dx / dist : 0;
        double ny = dist > 0 ? dy / dist : 0;

        switch (state) {
            case WANDER -> {
                double wx = wanderX - x, wy = wanderY - y;
                double wd = Math.hypot(wx, wy);
                if (wd < 40) pickWanderTarget();
                else { dirX = wx / wd; dirY = wy / wd; }
            }
            case CHASE -> { dirX = nx; dirY = ny; }
            case RETREAT -> { dirX = -nx; dirY = -ny; }
            case STRAFE -> {
                if (--strafeTimer <= 0) {
                    strafeTimer = 90 + rng.nextInt(120);
                    if (rng.nextDouble() < 0.5) strafeDir = -strafeDir;
                }
  
                double radial = (dist - PREFERRED_DIST) / PREFERRED_DIST;
                dirX = -ny * strafeDir + nx * radial;
                dirY =  nx * strafeDir + ny * radial;
            }
        }


        for (Circle o : others) {
            if (o == this) continue;
            double sx = x - o.x, sy = y - o.y;
            double sd = Math.hypot(sx, sy);
            if (sd > 0 && sd < radius * 4) {
                dirX += (sx / sd) * 0.8;
                dirY += (sy / sd) * 0.8;
            }
        }

        double len = Math.hypot(dirX, dirY);
        if (len > 0) {
            vx += (dirX / len) * accel;
            vy += (dirY / len) * accel;
        }

        double speed = Math.hypot(vx, vy);
        if (speed > maxSpeed) {
            vx = vx / speed * maxSpeed;
            vy = vy / speed * maxSpeed;
        }
        x += vx;
        y += vy;
        vx *= friction;
        vy *= friction;

        // shooting
        if (fireTimer > 0) fireTimer--;
        if (state != State.WANDER && dist < SHOOT_RANGE && fireTimer <= 0) {
            shoot(px, py, pvx, pvy);
            fireTimer = FIRE_COOLDOWN + rng.nextInt(20);
        }

        updateBullets();
    }

    private void chooseState(double dist) {
        if (dist > DETECT_RANGE) state = State.WANDER;
        else if (dist > PREFERRED_DIST + 80) state = State.CHASE;
        else if (dist < TOO_CLOSE) state = State.RETREAT;
        else state = State.STRAFE;
    }

    private void pickWanderTarget() {
        double angle = rng.nextDouble() * Math.PI * 2;
        double d = 150 + rng.nextDouble() * 300;
        wanderX = x + Math.cos(angle) * d;
        wanderY = y + Math.sin(angle) * d;
    }


    private void shoot(double px, double py, double pvx, double pvy) {
        double bulletSpeed = Bullet.SPEED;
        double dx = px - x, dy = py - y;

        double a = pvx * pvx + pvy * pvy - bulletSpeed * bulletSpeed;
        double b = 2 * (dx * pvx + dy * pvy);
        double c = dx * dx + dy * dy;
        double t = 0;

        if (Math.abs(a) < 1e-6) {
            if (Math.abs(b) > 1e-6) t = Math.max(0, -c / b);
        } else {
            double disc = b * b - 4 * a * c;
            if (disc >= 0) {
                double sq = Math.sqrt(disc);
                double t1 = (-b - sq) / (2 * a);
                double t2 = (-b + sq) / (2 * a);
                t = Math.min(t1 > 0 ? t1 : Double.MAX_VALUE, t2 > 0 ? t2 : Double.MAX_VALUE);
                if (t == Double.MAX_VALUE) t = 0;
            }
        }

        double aimX = px + pvx * t;
        double aimY = py + pvy * t;
        double angle = Math.atan2(aimY - y, aimX - x) + (rng.nextDouble() - 0.5) * 2 * AIM_SPREAD;

        bullets.add(new Bullet(x, y, Math.cos(angle) * bulletSpeed, Math.sin(angle) * bulletSpeed));
    }

    private void updateBullets() {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.update();
            if (b.life <= 0) it.remove();
        }
    }


    public int checkBulletHits(double px, double py, double playerRadius) {
        int hits = 0;
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            if (Math.hypot(b.x - px, b.y - py) < playerRadius + Bullet.RADIUS) {
                hits++;
                it.remove();
            }
        }
        return hits;
    }



    public void draw(GraphicsContext gc, double cameraX, double cameraY) {
        double sx = x - cameraX;
        double sy = y - cameraY;


        gc.setFill(Color.ORANGERED);
        for (Bullet b : bullets) {
            gc.fillOval(b.x - cameraX - Bullet.RADIUS, b.y - cameraY - Bullet.RADIUS,
                    Bullet.RADIUS * 2, Bullet.RADIUS * 2);
        }

  
        boolean charging = fireTimer < 10 && state != State.WANDER;
        gc.setFill(charging ? Color.WHITE : Color.CRIMSON);
        gc.fillOval(sx - radius, sy - radius, radius * 2, radius * 2);
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.strokeOval(sx - radius, sy - radius, radius * 2, radius * 2);
    }


    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public boolean isDead() { return health <= 0; }
    public void damage(int amount) { health -= amount; }


    private static final class Bullet {
        static final double SPEED = 6.0;
        static final double RADIUS = 4.0;

        double x, y, vx, vy;
        int life = 280;  

        Bullet(double x, double y, double vx, double vy) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        }

        void update() {
            x += vx;
            y += vy;
            life--;
        }
    }
}