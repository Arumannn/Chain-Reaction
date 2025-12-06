package com.silent.treatment.chainreaction.view;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleBackground {

    private static final Random random = new Random();

    private ParticleBackground() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Creates floating atom particles on the given pane.
     * The particles automatically adjust their respawn logic based on the pane's
     * size.
     */
    public static void attachTo(Pane pane) {
        List<Circle> atoms = new ArrayList<>();
        Color[] colors = { Color.RED, Color.CYAN, Color.LIME, Color.YELLOW };

        // Spawn particles (40 particles as per previous fix)
        for (int i = 0; i < 40; i++) {
            Circle c = new Circle(random.nextInt(5) + 3, colors[random.nextInt(4)]);
            c.setOpacity(0.4);
            // Randomize position initially over a large area (assuming Full HD max)
            c.setTranslateX(random.nextDouble() * 1920);
            c.setTranslateY(random.nextDouble() * 1080);
            pane.getChildren().add(c);
            atoms.add(c);
        }

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double height = pane.getHeight();
                double width = pane.getWidth();

                // Fallback if width/height is not ready yet
                if (height == 0)
                    height = 800;
                if (width == 0)
                    width = 1200;

                for (Circle c : atoms) {
                    c.setTranslateY(c.getTranslateY() - 0.7);

                    // Reset if out of bounds
                    if (c.getTranslateY() < -10) {
                        c.setTranslateY(height + 10);
                        c.setTranslateX(random.nextDouble() * width);
                    }
                }
            }
        }.start();
    }
}
