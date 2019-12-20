package ru.spbstu.lyubchenkova.checkers;

import org.junit.Before;
import org.junit.Test;

import ru.spbstu.lyubchenkova.checkers.game.CheckersGame;
import ru.spbstu.lyubchenkova.checkers.game.GameType;
import ru.spbstu.lyubchenkova.checkers.game.Move;
import ru.spbstu.lyubchenkova.checkers.game.Piece;
import ru.spbstu.lyubchenkova.checkers.game.Position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestCheckers {
    CheckersGame game;

    @Before
    public void init() {
        this.game = new CheckersGame(GameType.Human,0);
    }

    @Test
    public void checkMove(){
        Piece piece1=game.getBoard().getPiece(1, 2);
        game.makeMove(new Move(new Position(1,2)).add(new Position(2,3)));
        Piece piece2=game.getBoard().getPiece(2, 3);
        assertEquals(piece1, piece2);
    }

    @Test
    public void checkCapture(){
        assertEquals(game.whoseTurn() , CheckersGame.WHITE);
        game.makeMove(new Move(new Position(1,2)).add(new Position(2,3)));
        assertEquals(game.whoseTurn() , CheckersGame.BLACK);
        game.makeMove(new Move(new Position(4,5)).add(new Position(3,4)));
        assertEquals(game.whoseTurn() , CheckersGame.WHITE);
        assertEquals(1, game.getMoves().length);
        Move move = game.getMoves()[0];
        assertEquals(1, move.capturePositions.size());
        Piece willBeCaptured = game.getBoard().getPiece(3,4);
        assertNotNull(willBeCaptured);
        game.makeMove(move);
        assertNull(game.getBoard().getPiece(3,4));
    }
}
