package com.dama.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.dama.controller.GameController;

public class ControlPanel extends JPanel {
    private GameController controller;
    private final JButton newGameBtn = new JButton("New Game");
    private final JButton quitBtn    = new JButton("Quit");

    public ControlPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(140, 0));
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        styleButton(newGameBtn);
        styleButton(quitBtn);

        add(Box.createVerticalGlue());
        add(newGameBtn);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(quitBtn);
        add(Box.createVerticalGlue());

        newGameBtn.addActionListener(e -> { if (controller != null) controller.onNewGame(); });
        quitBtn.addActionListener(e    -> { if (controller != null) controller.onQuit(); });
    }

    private void styleButton(JButton btn) {
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(120, 40));
        btn.setBackground(new Color(80, 80, 80));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }
}
