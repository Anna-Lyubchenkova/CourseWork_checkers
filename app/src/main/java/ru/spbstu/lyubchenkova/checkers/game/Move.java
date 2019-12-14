package ru.spbstu.lyubchenkova.checkers.game;
import java.util.ArrayList;


/**
 *Класс используется для моделирования хода в игре.
 * Ход отслеживает каждую позицию доски, которая проходит, а также каждую позицию,
 * в которой находится противоположная фигура, которая захватывается при выполнении этого хода.
 */
public class Move {
    public ArrayList<Position> positions;
    public ArrayList<Position> capturePositions;
    private boolean kinged;

    public Move(Position pos) {
        init(pos.x, pos.y);
    }

    // copy constructor
    public Move(Move clone) {
        kinged = clone.kinged;
        positions = new ArrayList<>();
        for (Position position : clone.positions) {
            positions.add(position);
        }
        capturePositions = new ArrayList<>();
        for (Position capture : clone.capturePositions) {
            capturePositions.add(capture);
        }
    }

    private void init(int x, int y) {
        Position first = new Position(x, y);
        positions = new ArrayList<>();
        positions.add(first);
        kinged = false;
        capturePositions = new ArrayList<>();
    }

    public Move add(Position pos) {
        return add(pos.x, pos.y);
    }

    public Move add(int x, int y) {
        Position next = new Position(x, y);
        positions.add(next);

        // check if move results in a new king
        if (y == 0 || y == 7) {
            kinged = true;
        }
        return this;
    }

    public boolean isKinged() {
        return kinged;
    }

    /**
     * Добавляем позицию фигуры, которая захватывается при выполнении хода
     */
    public void addCapture(Position position) {
        capturePositions.add(position);
    }

    public Position start() {
        return positions.get(0);
    }

    public Position end() {
        return positions.get(positions.size() - 1);
    }

    public boolean equals(Move other){
        if(positions.size()!=other.positions.size())return false;
        for(int i = 0; i<positions.size(); i++){
            if(!positions.get(i).equals(other.positions.get(i)))return false;
        }
        return true;
    }

}