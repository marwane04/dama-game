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

        assertTrue(board.getPossibleMouvements(positionInitiale).contains(nouvelPosition), "Mouvement has to be possible");

        board.movePiece(positionInitiale, nouvelPosition);

        assertTrue(board.getPossibleMouvements(positionInitiale).isEmpty(), "The piece shouldn't be here");
    }

    @Test
    void testPossibleMouvementsForAnEmptySquare() {
        Position caseVide = new Position(3, 1);
        List<Position> mouvements = board.getPossibleMouvements(caseVide);
        assertTrue(mouvements.isEmpty(), "There should not be mouvement for an empty square");
    }
}
