package com.dama.model;

import java.util.ArrayList;
import java.util.List;

public class Damier {
    private final Piece[][] pieces = new Piece[8][8];
    private EtatJeu etat;

    public Damier() {
        this.etat = EtatJeu.EN_PROGRES;
        initDamier();
    }

    public EtatJeu getEtat() {
        return etat;
    }

    public void faireGagnerNoir() {
        this.etat = EtatJeu.NOIR_GAGNE;
    }

    public void faireGagnerRouge() {
        this.etat = EtatJeu.ROUGE_GAGNE;
    }

    public void faireJeuNul() {
        this.etat = EtatJeu.NUL;
    }

    private void initDamier() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (Position.estPositionJouable(i, j))
                    this.pieces[i][j] = new Piece(Couleur.NOIR, new Position(i, j));
            }
        }

        for (int i = 5; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((Position.estPositionJouable(i, j)))
                    this.pieces[i][j] = new Piece(Couleur.ROUGE, new Position(i, j));
            }
        }
    }

    public void deplacerPiece(Position positionInitiale, Position nouvelPosition) {
        if (mouvementsPossibles(positionInitiale).contains(nouvelPosition)) {
            this.pieces[nouvelPosition.getX()][nouvelPosition.getY()] = this.pieces[positionInitiale.getX()][positionInitiale.getY()];
            this.pieces[positionInitiale.getX()][positionInitiale.getY()] = null;
        }

    }

    public List<Position> mouvementsPossibles(Position position) {
        Piece piece = this.pieces[position.getX()][position.getY()];

        if (!position.estPositionJouable() || piece == null) return new ArrayList<>();

        List<Position> listeMouvenents = new ArrayList<>();

        if (Position.estPositionJouable(position.getX() + 1, position.getY() + 1) &&
                (this.pieces[position.getX() + 1][position.getY() + 1] == null ||
                this.pieces[position.getX() + 1][position.getY() + 1].getCouleur() != piece.getCouleur())) {
            listeMouvenents.add(new Position(position.getX() + 1, position.getY() + 1));
        }

        if (Position.estPositionJouable(position.getX() + 1, position.getY() - 1) &&
                (this.pieces[position.getX() + 1][position.getY() - 1] == null ||
                this.pieces[position.getX() + 1][position.getY() - 1].getCouleur() != piece.getCouleur())) {
            listeMouvenents.add(new Position(position.getX() + 1, position.getY() - 1));
        }

        return listeMouvenents;
    }


}
