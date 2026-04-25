package com.dama.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void testInitialState() {
        assertEquals(GameState.IN_PROGRESS, board.getState());
    }

    @Test
    void testMovePiece() {
        Position positionInitiale = new Position(2, 0);
        Position nouvelPosition = new Position(3, 1);

        assertTrue(board.getPossibleMovements(positionInitiale).contains(nouvelPosition), "Mouvement has to be possible");

        board.movePiece(positionInitiale, nouvelPosition);

        assertTrue(board.getPossibleMovements(positionInitiale).isEmpty(), "The piece shouldn't be here");
    }

    @Test
    void testPossibleMovementsForAnEmptySquare() {
        Position caseVide = new Position(3, 1);
        List<Position> movements = board.getPossibleMovements(caseVide);
        assertTrue(movements.isEmpty(), "There should not be mouvement for an empty square");
    }

    @Test
    void testCaptureMoveHasPriorityForPiece() {
        board.movePiece(new Position(5, 3), new Position(4, 2));
        board.movePiece(new Position(4, 2), new Position(3, 3));

        Position blackPiece = new Position(2, 2);
        Position captureLanding = new Position(4, 4);

        List<Position> movements = board.getPossibleMovements(blackPiece);

        assertEquals(1, movements.size(), "Only capture should be returned when available");
        assertTrue(movements.contains(captureLanding), "Capture landing square should be returned");
    }

    @Test
    void testCaptureMoveIsForcedGlobally() {
        board.movePiece(new Position(5, 3), new Position(4, 2));
        board.movePiece(new Position(4, 2), new Position(3, 3));

        List<Position> movements = board.getPossibleMovements(new Position(2, 0));

        assertTrue(movements.isEmpty(), "Non-capturing piece should have no moves when a capture exists");
    }
}
