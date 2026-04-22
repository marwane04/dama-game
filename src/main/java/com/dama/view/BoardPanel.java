package com.dama.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import com.dama.controller.GameController;
import com.dama.model.BoardModel;
import com.dama.model.PieceType;

public class BoardPanel extends JPanel {
    private static final int TILE_SIZE  = 80;
    private static final int BOARD_SIZE = 8;

    // Colors
    private static final Color DARK_SQUARE    = new Color(101, 67, 33);   // brown
    private static final Color LIGHT_SQUARE   = new Color(240, 217, 181); // cream
    private static final Color HIGHLIGHT      = new Color(50, 205, 50, 160); // semi-transparent green
    private static final Color RED_PIECE      = new Color(200, 30, 30);
    private static final Color BLACK_PIECE    = new Color(30, 30, 30);
    private static final Color KING_CROWN     = new Color(255, 215, 0);   // gold
    private static final Color SELECTED_RING  = new Color(255, 255, 0);   // yellow border

    private final BoardModel model;
    private GameController controller;

    // Currently selected square
    private int selectedRow = -1;
    private int selectedCol = -1;

    // Valid move destinations to highlight
    private final Set<String> highlightedSquares = new HashSet<>();

    public BoardPanel(BoardModel model) {
        this.model = model;
        setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / TILE_SIZE;
                int row = e.getY() / TILE_SIZE;
                if (isInBounds(row, col) && controller != null) {
                    selectedRow = row;
                    selectedCol = col;
                    controller.onSquareSelected(row, col);
                    repaint();
                }
            }
        });
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public void setHighlights(int[][] squares) {
        highlightedSquares.clear();
        if (squares != null) {
            for (int[] sq : squares) {
                highlightedSquares.add(sq[0] + "," + sq[1]);
            }
        }
    }

    public void clearHighlights() {
        highlightedSquares.clear();
        selectedRow = -1;
        selectedCol = -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Smooth rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(g2);
        drawHighlights(g2);
        drawPieces(g2);
        drawSelectedRing(g2);
    }

    private void drawBoard(Graphics2D g2) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isDark = (row + col) % 2 != 0;
                g2.setColor(isDark ? DARK_SQUARE : LIGHT_SQUARE);
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawHighlights(Graphics2D g2) {
        g2.setColor(HIGHLIGHT);
        for (String key : highlightedSquares) {
            String[] parts = key.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            g2.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawPieces(Graphics2D g2) {
        int padding = 10;
        int diameter = TILE_SIZE - 2 * padding;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                PieceType piece = model.getPieceAt(row, col);
                if (piece == PieceType.EMPTY) continue;

                int x = col * TILE_SIZE + padding;
                int y = row * TILE_SIZE + padding;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(x + 3, y + 3, diameter, diameter);

                // Piece body
                boolean isRed = (piece == PieceType.RED || piece == PieceType.RED_KING);
                g2.setColor(isRed ? RED_PIECE : BLACK_PIECE);
                g2.fillOval(x, y, diameter, diameter);

                // Piece border
                g2.setColor(isRed ? RED_PIECE.darker() : BLACK_PIECE.brighter());
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(x, y, diameter, diameter);

                // King crown indicator
                boolean isKing = (piece == PieceType.RED_KING || piece == PieceType.BLACK_KING);
                if (isKing) {
                    g2.setColor(KING_CROWN);
                    g2.setFont(new Font("Serif", Font.BOLD, 22));
                    FontMetrics fm = g2.getFontMetrics();
                    String crown = "♛";
                    int tx = x + (diameter - fm.stringWidth(crown)) / 2;
                    int ty = y + (diameter + fm.getAscent()) / 2 - 4;
                    g2.drawString(crown, tx, ty);
                }
            }
        }
    }

    private void drawSelectedRing(Graphics2D g2) {
        if (selectedRow < 0 || selectedCol < 0) return;
        int padding = 10;
        int diameter = TILE_SIZE - 2 * padding;
        g2.setColor(SELECTED_RING);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(
            selectedCol * TILE_SIZE + padding,
            selectedRow * TILE_SIZE + padding,
            diameter, diameter
        );
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
}
