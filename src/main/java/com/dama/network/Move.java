package com.dama.network;

import com.dama.model.Position;

public class Move {
    private final Position initialPosition;
    private final Position targetPosition;

    public Move(Position initialPosition, Position targetPosition) {
        this.initialPosition = initialPosition;
        this.targetPosition = targetPosition;
    }

    @Override
    public String toString() {
        return initialPosition.getX() + " " + initialPosition.getY() + " , " + targetPosition.getX() + " " + targetPosition.getY();
    }

    /**
     * Parses a move from the format produced by toString(): "x y , x y".
     */
    public static Move stringToMove(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Move string is null");
        }

        String[] parts = string.trim().split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid move format: " + string);
        }

        int[] coords = new int[4];
        int index = 0;
        for (String part : parts) {
            String[] tokens = part.trim().split("\\s+");
            if (tokens.length != 2) {
                throw new IllegalArgumentException("Invalid move format: " + string);
            }
            coords[index++] = Integer.parseInt(tokens[0]);
            coords[index++] = Integer.parseInt(tokens[1]);
        }

        Position initial = new Position(coords[0], coords[1]);
        Position target = new Position(coords[2], coords[3]);

        return new Move(initial, target);
    }
}
