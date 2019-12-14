package ru.spbstu.lyubchenkova.checkers.game;

import java.io.Serializable;

/**
 * Класс моделирует шашку. Она может быть белой, черной или дамкой.
 */
public class Piece implements Serializable {
    private int color;
    private boolean isKing;
    private int summaryID;

    // summary IDs
    public final int EMPTY = 0;
    public final int BLACK = 1;
    public final int WHITE = 2;
    public final int BLACK_KING = 3;
    public final int WHITE_KING = 4;

    /**
     * Конструктор шашки с параметрами цвета и значением дамки
     */
    Piece(int color, boolean king) {
        this.color = color;
        this.isKing = king;

        summaryID = color;

        if (isKing())
            summaryID += 2;
    }

    /**
     * Вернём цвет шашки
     */
    public int getColor() {
        return color;
    }

    public int getSummaryID() {
        return summaryID;
    }

    /**
     * Вернём дамка или нет
     */
    public boolean isKing() {
        return isKing;
    }

    /**
     * Делаем дамкой
     */
    void makeKing() {
        isKing = true;
        summaryID = color + 2;
    }
}
