package com.dama.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBar extends JPanel {
    private final JLabel label = new JLabel("Welcome! Red goes first.");

    public StatusBar() {
        setBackground(new Color(30, 30, 30));
        setPreferredSize(new Dimension(0, 35));
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(label);
    }

    public void setMessage(String message) {
        label.setText(message);
    }
}
