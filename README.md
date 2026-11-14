# java_final_project

## Requirements

- Java 25 (JDK), with JavaFX 25.0.5 supplied by Maven.
- Maven 3.9 or newer.

## Build and run

From the `demo` directory:

```shell
mvn clean test-compile
mvn test
mvn javafx:run
```

The JavaFX application needs a graphical desktop session to run.

Use **W**, **A**, **S**, and **D** to move the triangle up, left, down, and right.
The nearest enemy is targeted when the game starts. Use **Q** and **E** to cycle
targets, then press or hold **Space** to shoot at the selected enemy. The selected
enemy is marked with a green reticle.
Use the mouse scroll wheel to zoom the map and player in or out.
The player starts with 100 health. Circle enemies deal 5 damage per hit, and
the player gets brief invulnerability after taking damage. Defeat the swarm or
restart with **R** after the ship is destroyed.
eq