package com.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.example.sprites.Circle;
import com.example.sprites.Triangle;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {

    window win = new window();
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final Star[] stars = createStars();
    private final List<Circle> mobs = new ArrayList<>();
    private final List<PlayerBullet> playerBullets = new ArrayList<>();
    private Circle target;
    private boolean shootRequested;
    private double playerFireCooldown;
    private double zoom = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.5;
    private static final double GRID_SPACING = 120.0;
    private static final double STAR_PARALLAX = 0.35;
    private static final int INITIAL_CIRCLE_COUNT = 10;
    private static final int PLAYER_MAX_HEALTH = 100;
    private static final int CIRCLE_DAMAGE = 5;
    private static final double PLAYER_DAMAGE_COOLDOWN = 0.75;
		private static final double FIRE_COOLDOWN = 0.2;        
    private int playerHealth = PLAYER_MAX_HEALTH;
    private double playerDamageCooldown;
    private boolean gameOver;
		private long lastFrameNanos = -1;
		private static final double MAX_DT = 0.05; 

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

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (activeKeys.add(event.getCode())) {
                if (event.getCode() == KeyCode.E) cycleTarget(triangle, 1);
                if (event.getCode() == KeyCode.Q) cycleTarget(triangle, -1);
                if (event.getCode() == KeyCode.SPACE) shootRequested = true;
                if (event.getCode() == KeyCode.R && gameOver) restartGame(triangle);
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> activeKeys.remove(event.getCode()));
        scene.addEventFilter(ScrollEvent.SCROLL, event -> {
            zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM,
                    zoom * Math.pow(1.1, event.getDeltaY() / 40.0)));
            event.consume();
        });

        stage.setScene(scene);
        stage.setResizable(false);

        stage.show();
        root.requestFocus();

        spawnMobs(triangle);
        target = findNearestTarget(triangle);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
							  double dt = lastFrameNanos < 0 ? 0 : Math.min((now - lastFrameNanos) / 1_000_000_000.0, MAX_DT);
								lastFrameNanos = now;

                if (!gameOver) {
 										updatePlayer(triangle, dt);
										updateMobs(triangle, dt);
										ensureTarget(triangle);
										firePlayerBullet(triangle, dt);
										updatePlayerBullets(dt);
                }

                render(gc, triangle, now);
                if (gameOver) {
                    text.setText("Ship destroyed - press R to restart");
                    text.setFill(Color.ORANGERED);
                } else {
                    text.setText(String.format(
                            "HP: %d/%d   Speed: %.2f   Target: %d/%d   Zoom: %.0f%%   Q/E: target   Space: shoot   Scroll: zoom",
                            playerHealth, PLAYER_MAX_HEALTH, triangle.getSpeed(),
                            target == null ? 0 : mobs.indexOf(target) + 1, mobs.size(), zoom * 100));
                    text.setFill(Color.WHITE);
                }
            }
        }.start();
    }

    private void spawnMobs(Triangle triangle) {
        Random spawnRng = new Random();
        for (int i = 0; i < INITIAL_CIRCLE_COUNT; i++) {
            double angle = spawnRng.nextDouble() * Math.PI * 2;
            double distance = 230 + spawnRng.nextDouble() * 80;
            mobs.add(new Circle(
                    triangle.getWorldX() + 25 + Math.cos(angle) * distance,
                    triangle.getWorldY() + 25 + Math.sin(angle) * distance));
        }
    }

    private void restartGame(Triangle triangle) {
        playerHealth = PLAYER_MAX_HEALTH;
        playerDamageCooldown = 0;
        playerFireCooldown = 0;
        shootRequested = false;
        gameOver = false;
        playerBullets.clear();
        mobs.clear();
        spawnMobs(triangle);
        target = findNearestTarget(triangle);
    }

		private void updatePlayer(Triangle triangle, double dt) {
				if (activeKeys.contains(KeyCode.W)) triangle.moveUp();
				if (activeKeys.contains(KeyCode.S)) triangle.moveDown();
				if (activeKeys.contains(KeyCode.A)) triangle.moveLeft();
				if (activeKeys.contains(KeyCode.D)) triangle.moveRight();

				triangle.updateMovement(dt);
				triangle.updateRotation(dt);

				triangle.setTranslateX(win.window_width / 2.0 - 25.0);
				triangle.setTranslateY(win.window_height / 2.0 - 25.0);
				triangle.setScaleX(zoom);
				triangle.setScaleY(zoom);

				// blink period of 8 frames at 60 fps, now in seconds
				boolean blinkOff = playerDamageCooldown > 0 && ((int) (playerDamageCooldown * 60) % 8) < 4;
				triangle.setOpacity(blinkOff ? 0.4 : 1.0);
		}

		private void updateMobs(Triangle triangle, double dt) {
				if (playerDamageCooldown > 0) {
						playerDamageCooldown = Math.max(0, playerDamageCooldown - dt);
				}
				double px = triangle.getWorldX() + 25.0;
				double py = triangle.getWorldY() + 25.0;

				for (Circle mob : mobs) {
						mob.update(px, py, triangle.getVx(), triangle.getVy(), dt, mobs);
						int hits = mob.checkBulletHits(px, py, 20);
						if (hits > 0 && playerDamageCooldown == 0) {
								playerHealth = Math.max(0, playerHealth - CIRCLE_DAMAGE);
								playerDamageCooldown = PLAYER_DAMAGE_COOLDOWN;
								gameOver = playerHealth == 0;
						}
				}
				mobs.removeIf(Circle::isDead);
		}

    private void cycleTarget(Triangle triangle, int direction) {
        if (mobs.isEmpty()) {
            target = null;
            return;
        }

        int currentIndex = mobs.indexOf(target);
        if (currentIndex < 0) {
            target = findNearestTarget(triangle);
            return;
        }

        int nextIndex = (currentIndex + direction + mobs.size()) % mobs.size();
        target = mobs.get(nextIndex);
    }

    private void ensureTarget(Triangle triangle) {
        if (target == null || !mobs.contains(target)) {
            target = findNearestTarget(triangle);
        }
    }

    private Circle findNearestTarget(Triangle triangle) {
        double px = triangle.getWorldX() + 25.0;
        double py = triangle.getWorldY() + 25.0;
        Circle nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Circle mob : mobs) {
            double distance = Math.hypot(mob.getX() - px, mob.getY() - py);
            if (distance < nearestDistance) {
                nearest = mob;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private void firePlayerBullet(Triangle triangle, double dt) {
			if (gameOver) return;
			if (playerFireCooldown > 0) playerFireCooldown = Math.max(0, playerFireCooldown - dt);
			if (!shootRequested && (!activeKeys.contains(KeyCode.SPACE) || playerFireCooldown > 0)) return;
			shootRequested = false;
			if (target == null || target.isDead()) return;

				double startX = triangle.getWorldX() + 25.0;
				double startY = triangle.getWorldY() + 25.0;
				double dx = target.getX() - startX;
				double dy = target.getY() - startY;
				double distance = Math.hypot(dx, dy);
				if (distance == 0) return;

				double speed = PlayerBullet.SPEED;
				playerBullets.add(new PlayerBullet(startX, startY, target,
								dx / distance * speed, dy / distance * speed));
				playerFireCooldown = FIRE_COOLDOWN;
		}

    private void updatePlayerBullets(double dt) {
				Iterator<PlayerBullet> bullets = playerBullets.iterator();
				while (bullets.hasNext()) {
						PlayerBullet bullet = bullets.next();
						if (bullet.target.isDead() || !mobs.contains(bullet.target)) {
								bullets.remove();
								continue;
						}

						double dx = bullet.target.getX() - bullet.x;
						double dy = bullet.target.getY() - bullet.y;
						double distance = Math.hypot(dx, dy);
						if (distance > 0) {
								bullet.vx = dx / distance * PlayerBullet.SPEED;
								bullet.vy = dy / distance * PlayerBullet.SPEED;
						}
						bullet.x += bullet.vx * dt;
						bullet.y += bullet.vy * dt;
						bullet.life -= dt;

						if (Math.hypot(bullet.x - bullet.target.getX(), bullet.y - bullet.target.getY())
										<= bullet.target.getRadius() + PlayerBullet.RADIUS) {
								bullet.target.damage(1);
								bullets.remove();
						} else if (bullet.life <= 0) {
								bullets.remove();
						}
				}
		}

    private void render(GraphicsContext gc, Triangle triangle, long now) {
        gc.setFill(Color.rgb(3, 5, 20));
        gc.fillRect(0, 0, win.window_width, win.window_height);

        gc.save();
        gc.translate(win.window_width / 2.0, win.window_height / 2.0);
        gc.scale(zoom, zoom);
        gc.translate(-win.window_width / 2.0, -win.window_height / 2.0);

        double cameraX = triangle.getWorldX() + 25.0 - win.window_width / 2.0;
        double cameraY = triangle.getWorldY() + 25.0 - win.window_height / 2.0;
        double time = now / 1_000_000_000.0;

        drawStars(gc, cameraX, cameraY, time);
        drawGrid(gc, triangle);

        for (Circle mob : mobs) {
            mob.draw(gc, cameraX, cameraY, mob == target);
        }

        gc.setFill(Color.LIMEGREEN);
        for (PlayerBullet bullet : playerBullets) {
            gc.fillOval(bullet.x - cameraX - PlayerBullet.RADIUS,
                    bullet.y - cameraY - PlayerBullet.RADIUS,
                    PlayerBullet.RADIUS * 2, PlayerBullet.RADIUS * 2);
        }
        gc.restore();

        drawHealthBar(gc);
    }

    private void drawHealthBar(GraphicsContext gc) {
        double x = 20;
        double y = 55;
        double width = 200;
        double height = 14;
        double healthRatio = playerHealth / (double) PLAYER_MAX_HEALTH;

        gc.setFill(Color.rgb(25, 34, 48, 0.95));
        gc.fillRoundRect(x, y, width, height, 6, 6);
        gc.setFill(healthRatio > 0.5 ? Color.LIMEGREEN
                : healthRatio > 0.25 ? Color.GOLD : Color.ORANGERED);
        gc.fillRoundRect(x, y, width * healthRatio, height, 6, 6);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, width, height, 6, 6);
    }

    private void drawStars(GraphicsContext gc, double cameraX, double cameraY, double time) {
        double halfVisibleWidth = win.window_width / (2.0 * zoom);
        double halfVisibleHeight = win.window_height / (2.0 * zoom);
        double left = win.window_width / 2.0 - halfVisibleWidth;
        double right = win.window_width / 2.0 + halfVisibleWidth;
        double top = win.window_height / 2.0 - halfVisibleHeight;
        double bottom = win.window_height / 2.0 + halfVisibleHeight;

        for (Star star : stars) {
            double baseX = wrap(star.x - cameraX * STAR_PARALLAX, win.window_width);
            double baseY = wrap(star.y - cameraY * STAR_PARALLAX, win.window_height);
            int firstTileX = (int) Math.floor((left - baseX) / win.window_width);
            int lastTileX = (int) Math.ceil((right - baseX) / win.window_width);
            int firstTileY = (int) Math.floor((top - baseY) / win.window_height);
            int lastTileY = (int) Math.ceil((bottom - baseY) / win.window_height);

            double twinkle = 0.7 + 0.3 * Math.sin(time * star.twinkleSpeed + star.phase);
            int brightness = (int) (star.brightness * twinkle);
            gc.setFill(Color.rgb(brightness, brightness, Math.min(255, brightness + 35)));

            for (int tileX = firstTileX; tileX <= lastTileX; tileX++) {
                for (int tileY = firstTileY; tileY <= lastTileY; tileY++) {
                    double x = baseX + tileX * win.window_width;
                    double y = baseY + tileY * win.window_height;
                    gc.fillOval(x, y, star.radius * 2.0, star.radius * 2.0);
                }
            }
        }
    }

    private void drawGrid(GraphicsContext gc, Triangle triangle) {
        double playerX = triangle.getWorldX() + 25.0;
        double playerY = triangle.getWorldY() + 25.0;
        double visibleHalfWidth = win.window_width / (2.0 * zoom);
        double visibleHalfHeight = win.window_height / (2.0 * zoom);
        double leftWorld = playerX - visibleHalfWidth;
        double rightWorld = playerX + visibleHalfWidth;
        double topWorld = playerY - visibleHalfHeight;
        double bottomWorld = playerY + visibleHalfHeight;

        gc.setStroke(Color.rgb(28, 42, 72, 0.45));
        gc.setLineWidth(1.0 / zoom);

        double firstX = Math.floor(leftWorld / GRID_SPACING) * GRID_SPACING;
        for (double worldX = firstX; worldX <= rightWorld; worldX += GRID_SPACING) {
            double screenX = worldX - (playerX - win.window_width / 2.0);
            gc.strokeLine(screenX, 0, screenX, win.window_height);
        }

        double firstY = Math.floor(topWorld / GRID_SPACING) * GRID_SPACING;
        for (double worldY = firstY; worldY <= bottomWorld; worldY += GRID_SPACING) {
            double screenY = worldY - (playerY - win.window_height / 2.0);
            gc.strokeLine(0, screenY, win.window_width, screenY);
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

    private static final class PlayerBullet {
				private static final double SPEED = 720.0;   
        private static final double RADIUS = 6.0;

        private double x;
        private double y;
        private double vx;
        private double vy;
        private final Circle target;
        private double life = 3.0f;

        private PlayerBullet(double x, double y, Circle target, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.target = target;
            this.vx = vx;
            this.vy = vy;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
