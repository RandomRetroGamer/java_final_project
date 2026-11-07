package com.example.sprites;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Triangle extends Polygon implements specs {
    

    

    public Triangle(){
        this.getPoints().addAll(
            0.0, 0.0,
            50.0, 100.0,
            100.0, 0.0
        );
        this.setFill(Color.BLUE);
    }


    public Triangle(double x, double y, Color color) {
        this();
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setFill(color);
    }
}