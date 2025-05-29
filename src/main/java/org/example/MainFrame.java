package org.example;

import javax.swing.*;

public class MainFrame extends JFrame {
    private Labyrinth labyrinth;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;

    public MainFrame(Labyrinth labyrinth ) {
        this.labyrinth = labyrinth;
        this.setSize(WIDTH, HEIGHT);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.add(this.labyrinth);
        this.setVisible(true);
    }
}
