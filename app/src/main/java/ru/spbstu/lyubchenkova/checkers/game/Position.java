package ru.spbstu.lyubchenkova.checkers.game;

/**
 * Класс моделирует позицию шашки по квадратам на доске. Координаты от 0 до 7.
 */
public class Position {
    // x and y coordinates of position
    final int x;
    final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Position) {
            Position otherPosition = (Position)other;
            return (x == otherPosition.x && y == otherPosition.y);
        } else {
            return false;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    Position plus(Position to) {
        return new Position(x + to.x, y + to.y);
    }

    public boolean equals(Position other){
        return x==other.x && y==other.y;
    }
}
