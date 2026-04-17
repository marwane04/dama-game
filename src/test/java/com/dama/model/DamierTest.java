package com.dama.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DamierTest {

    private Damier damier;

    @BeforeEach
    void setUp() {
        damier = new Damier();
    }

    @Test
    void testInitialisationEtat() {
        assertEquals(EtatJeu.EN_PROGRES, damier.getEtat());
    }

    @Test
    void testDeplacerPiece() {
        Position positionInitiale = new Position(2, 0); // Assuming 2,1 is a Noir piece
        Position nouvelPosition = new Position(3, 1);

        assertTrue(damier.mouvementsPossibles(positionInitiale).contains(nouvelPosition), "Mouvement devrait etre possible");

        damier.deplacerPiece(positionInitiale, nouvelPosition);

        assertTrue(damier.mouvementsPossibles(positionInitiale).isEmpty(), "La piece ne devrait plus etre la");
    }

    @Test
    void testMouvementsPossiblesCaseVide() {
        Position caseVide = new Position(3, 1); // Ligne 3 est vide a l'initialisation
        List<Position> mouvements = damier.mouvementsPossibles(caseVide);
        assertTrue(mouvements.isEmpty(), "Il ne devrait pas y avoir de mouvements pour une case vide");
    }
}
