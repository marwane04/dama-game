package com.dama.view;

import com.dama.controller.GameController;
import com.dama.model.Board;
import com.dama.model.Piece;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashSet;
import java.util.Set;

public class BoardView extends GridPane {

    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 80;

    // Colors — dark wood aesthetic
    private static final Color DARK_TILE = Color.web("#5C3317");
    private static final Color LIGHT_TILE = Color.web("#F0D9B5");
    private static final Color HIGHLIGHT = Color.web("#50C87880"); // green, semi-transparent
    private static final Color SELECTED_TILE = Color.web("#FFD70080"); // gold, semi-transparent
    private static final Color RED_PIECE = Color.web("#C0392B");
    private static final Color RED_PIECE_EDGE = Color.web("#922B21");
    private static final Color BLACK_PIECE = Color.web("#1A1A2E");
    private static final Color BLACK_PIECE_EDGE = Color.web("#0D0D1A");
    private static final Color KING_COLOR = Color.web("#FFD700");

    private final Board model;
    private GameController controller;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final Set<String> highlights = new HashSet<>();

    // Grid of tile StackPanes for easy redraw
    private final StackPane[][] tiles = new StackPane[BOARD_SIZE][BOARD_SIZE];

    public BoardView(Board model) {
        this.model = model;
        setHgap(0);
        setVgap(0);
        buildBoard();
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    // ── Public API ────────────────────────────────────────────────

    public void refresh() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                redrawTile(row, col);
            }
        }
    }

    public void setHighlights(int[][] squares) {
        highlights.clear();
        if (squares != null) {
            for (int[] sq : squares) {
                highlights.add(sq[0] + "," + sq[1]);
            }
        }
        refresh();
    }

    public void clearHighlights() {
        highlights.clear();
        selectedRow = -1;
        selectedCol = -1;
        refresh();
    }

    // ── Private drawing ───────────────────────────────────────────

    private void buildBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane tile = new StackPane();
                tile.setPrefSize(TILE_SIZE, TILE_SIZE);
                tiles[row][col] = tile;

                final int r = row, c = col;
                tile.setOnMouseClicked(e -> onTileClicked(r, c));

                redrawTile(row, col);
                add(tile, col, row);
            }
        }
    }

    private void redrawTile(int row, int col) {
        StackPane tile = tiles[row][col];
        tile.getChildren().clear();

        // ── Background square ──
        Rectangle bg = new Rectangle(TILE_SIZE, TILE_SIZE);
        boolean isDark = (row + col) % 2 != 0;
        bg.setFill(isDark ? DARK_TILE : LIGHT_TILE);
        tile.getChildren().add(bg);

        // ── Highlight overlay ──
        String key = row + "," + col;
        if (highlights.contains(key)) {
            Rectangle overlay = new Rectangle(TILE_SIZE, TILE_SIZE);
            overlay.setFill(HIGHLIGHT);
            tile.getChildren().add(overlay);
        }

        // ── Selected overlay ──
        if (row == selectedRow && col == selectedCol) {
            Rectangle overlay = new Rectangle(TILE_SIZE, TILE_SIZE);
            overlay.setFill(SELECTED_TILE);
            tile.getChildren().add(overlay);
        }

        // ── Piece ──
        Piece piece = model.getPiece(row, col);
        if (piece != null) {
            tile.getChildren().add(buildPiece(piece));
        }
    }

    private StackPane buildPiece(Piece piece) {
        StackPane container = new StackPane();
        container.setMouseTransparent(true); // clicks pass through to tile


        double radius = (TILE_SIZE / 2.0) - 10;

        // Shadow
        Circle shadow = new Circle(radius);
        shadow.setFill(Color.rgb(0, 0, 0, 0.35));
        shadow.setTranslateX(3);
        shadow.setTranslateY(4);

        // Piece body
        Circle body = new Circle(radius);
        body.setFill(piece.getColor() == com.dama.model.Color.RED ? RED_PIECE : BLACK_PIECE);

        // Inner highlight (3D feel)
        InnerShadow inner = new InnerShadow();
        inner.setColor(Color.rgb(255, 255, 255, 0.25));
        inner.setRadius(10);
        inner.setOffsetX(-3);
        inner.setOffsetY(-3);
        body.setEffect(inner);

        // Edge ring
        Circle edge = new Circle(radius);
        edge.setFill(Color.TRANSPARENT);
        edge.setStroke(piece.getColor() == com.dama.model.Color.RED ? RED_PIECE_EDGE : BLACK_PIECE_EDGE);
        edge.setStrokeWidth(2.5);

        container.getChildren().addAll(shadow, body, edge);

        // King crown
        if (piece.isKing()) {
            Text crown = new Text("♛");
            crown.setFont(Font.font("Serif", FontWeight.BOLD, 22));
            crown.setFill(KING_COLOR);

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FFD700"));
            glow.setRadius(6);
            crown.setEffect(glow);

            container.getChildren().add(crown);
        }

        return container;
    }

    private void onTileClicked(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        refresh();
        if (controller != null) {
            controller.onSquareSelected(row, col);
        }
    }
}