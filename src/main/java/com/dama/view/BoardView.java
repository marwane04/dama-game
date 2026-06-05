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
    private static final int TILE_SIZE  = 80;

    private static final Color DARK_TILE         = Color.web("#5C3317");
    private static final Color LIGHT_TILE        = Color.web("#F0D9B5");
    private static final Color SELECTED_TILE     = Color.web("#FFD700BB");
    private static final Color MOVE_DOT_COLOR    = Color.web("#50C878CC");
    private static final Color CAPTURE_DOT_COLOR = Color.web("#E94560CC");
    private static final Color RED_PIECE         = Color.web("#C0392B");
    private static final Color RED_PIECE_EDGE    = Color.web("#922B21");
    private static final Color BLACK_PIECE       = Color.web("#1A1A2E");
    private static final Color BLACK_PIECE_EDGE  = Color.web("#0D0D1A");
    private static final Color KING_COLOR        = Color.web("#FFD700");

    private final Board model;
    private GameController controller;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final Set<String> highlights = new HashSet<>();

    private final StackPane[][] tiles = new StackPane[BOARD_SIZE][BOARD_SIZE];

    public BoardView(Board model) {
        this.model = model;
        setHgap(0);
        setVgap(0);
        setMinSize(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        setMaxSize(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        setPrefSize(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        buildBoard();
    }

    public void setController(GameController c) { this.controller = c; }

    // ── Public API ────────────────────────────────────────────────

    public void refresh() {
        for (int row = 0; row < BOARD_SIZE; row++)
            for (int col = 0; col < BOARD_SIZE; col++)
                redrawTile(row, col);
    }

    public void setHighlights(int[][] squares) {
        highlights.clear();
        if (squares != null)
            for (int[] sq : squares)
                highlights.add(sq[0] + "," + sq[1]);
        refresh();
    }

    public void setSelectedPiece(int row, int col) {
        selectedRow = row;
        selectedCol = col;
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
                tile.setMinSize(TILE_SIZE, TILE_SIZE);
                tile.setMaxSize(TILE_SIZE, TILE_SIZE);
                tile.setPrefSize(TILE_SIZE, TILE_SIZE);
                tile.setClip(new Rectangle(TILE_SIZE, TILE_SIZE));
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

        // ── Background ──
        Rectangle bg = new Rectangle(TILE_SIZE, TILE_SIZE);
        bg.setFill((row + col) % 2 != 0 ? DARK_TILE : LIGHT_TILE);
        bg.setMouseTransparent(true);
        tile.getChildren().add(bg);

        // ── Selected piece highlight ──
        if (row == selectedRow && col == selectedCol) {
            Rectangle sel = new Rectangle(TILE_SIZE, TILE_SIZE);
            sel.setFill(SELECTED_TILE);
            sel.setMouseTransparent(true);
            tile.getChildren().add(sel);

            Rectangle border = new Rectangle(TILE_SIZE, TILE_SIZE);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.web("#FFD700"));
            border.setStrokeWidth(3);
            border.setMouseTransparent(true);
            tile.getChildren().add(border);
        }

        // ── Valid move / capture dot ──
        String key = row + "," + col;
        if (highlights.contains(key)) {
            Piece here = model.getPiece(row, col);
            boolean isCapture = here != null;
            double dotRadius = isCapture ? 34 : 14;
            Color  dotColor  = isCapture ? CAPTURE_DOT_COLOR : MOVE_DOT_COLOR;

            Circle dot = new Circle(dotRadius);
            dot.setFill(dotColor);
            dot.setMouseTransparent(true);
            tile.getChildren().add(dot);
        }

        // ── Piece ──
        Piece piece = model.getPiece(row, col);
        if (piece != null) {
            tile.getChildren().add(buildPiece(piece));
        }
    }

    private StackPane buildPiece(Piece piece) {
        StackPane container = new StackPane();
        container.setMouseTransparent(true);
        container.setMinSize(TILE_SIZE, TILE_SIZE);
        container.setMaxSize(TILE_SIZE, TILE_SIZE);

        double radius = (TILE_SIZE / 2.0) - 10;

        Circle shadow = new Circle(radius);
        shadow.setFill(Color.rgb(0, 0, 0, 0.35));
        shadow.setTranslateX(3);
        shadow.setTranslateY(4);

        Circle body = new Circle(radius);
        body.setFill(piece.getColor() == com.dama.model.Color.RED ? RED_PIECE : BLACK_PIECE);

        InnerShadow inner = new InnerShadow();
        inner.setColor(Color.rgb(255, 255, 255, 0.25));
        inner.setRadius(10);
        inner.setOffsetX(-3);
        inner.setOffsetY(-3);
        body.setEffect(inner);

        Circle edge = new Circle(radius);
        edge.setFill(Color.TRANSPARENT);
        edge.setStroke(piece.getColor() == com.dama.model.Color.RED ? RED_PIECE_EDGE : BLACK_PIECE_EDGE);
        edge.setStrokeWidth(2.5);

        container.getChildren().addAll(shadow, body, edge);

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
        if (controller != null) controller.onSquareSelected(row, col);
    }
}