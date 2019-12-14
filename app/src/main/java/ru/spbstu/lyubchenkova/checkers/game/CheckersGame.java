package ru.spbstu.lyubchenkova.checkers.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

import ru.spbstu.lyubchenkova.checkers.R;


/**
 * Класс, где моделируется игра в шашки, здесь информация
 * о игровом поле, текущем игроке, типе игры и захваченных шашках
 */
public class CheckersGame implements Parcelable, Serializable {
    public int searchDepth;

    static final int NONE = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    static final int KINGED = 3;

    // состояние игры
    private Board gameBoard;
    private int turn;
    private ArrayList<Piece> capturedBlackPieces;
    private ArrayList<Piece> capturedWhitePieces;
    private boolean isFinished;
    private GameType gameType;

    // Иконки шашек
    private int blackNormalIconId = R.drawable.ic_piece_black;
    private int blackKingIconId = R.drawable.ic_piece_black_queen;
    private int whiteNormalIconId = R.drawable.ic_piece_white;
    private int whiteKingIconId = R.drawable.ic_piece_white_queen;

    /**
     * Конструктор, где создаётся игра. Белые ходят первыми
     */
    public CheckersGame(GameType gameType,int depth) {
        gameBoard = new Board();
        turn = CheckersGame.WHITE;
        capturedBlackPieces = new ArrayList<>();
        capturedWhitePieces = new ArrayList<>();
        isFinished = false;
        this.gameType = gameType;
        searchDepth=depth;
    }

    public GameType getGameType() {
        return gameType;
    }

    public boolean isGameFinished() {
        return isFinished;
    }

    public void setGameFinished(boolean gameFinished) {
        isFinished = gameFinished;
    }

    /**
     * Смена хода
     */
    private void advanceTurn() {
        if (turn == CheckersGame.WHITE) {
            turn = CheckersGame.BLACK;
        } else {
            turn = CheckersGame.WHITE;
        }
    }

    /**
     * Возвращаем доску текущей игры
     */
    public Board getBoard() {
        return this.gameBoard;
    }

    public ArrayList<Piece> getCapturedBlackPieces() {
        return capturedBlackPieces;
    }

    public ArrayList<Piece> getCapturedWhitePieces() {
        return capturedWhitePieces;
    }

    private ArrayList<Piece> getCapturedPiecesForMove(Move move) {
        ArrayList<Piece> pieces = new ArrayList<>();

        for (Position p: move.capturePositions)
            pieces.add(getBoard().getPiece(p));

        return pieces;
    }

    /**
     * Возвращаем самый длинный ход для пары начальных-конечных позиций
     */
    public Move getLongestMove(Position start, Position end) {
        Move longest = null;
        Move[] moveset = getMoves();
        for (Move move : moveset) {
            if (move.start().equals(start) && move.end().equals(end)) {
                if (longest == null ||
                        longest.capturePositions.size() < move.capturePositions.size())
                    longest = move;
            }
        }
        return longest;
    }

    /**
     * Генерируем массив доступных ходов для текущего игрока
     */
    public Move[] getMoves() {
        return gameBoard.getMoves(turn);
    }

    /**
     * Возвращаем массив доступных ходов для заданного игрока
     */
    public Move[] getMoves(int turn) {
        return gameBoard.getMoves(turn);
    }

    /**
     * Выполняем ход на доске и передаём ход другому
     */
    public void makeMove(Move move) {
        if (whoseTurn() == BLACK)
            capturedWhitePieces.addAll(getCapturedPiecesForMove(move));
        else
            capturedBlackPieces.addAll(getCapturedPiecesForMove(move));
        gameBoard.makeMove(move);
        advanceTurn();
    }

    /**
     * Возвращаем ID текущего игрока
     */
    public int whoseTurn() {
        return turn;
    }

    public int getBlackNormalIconId() {
        return blackNormalIconId;
    }

    public int getBlackKingIconId() {
        return blackKingIconId;
    }

    public int getWhiteNormalIconId() {
        return whiteNormalIconId;
    }

    public int getWhiteKingIconId() {
        return whiteKingIconId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeParcelable(gameBoard, 0);
        dest.writeInt(whoseTurn());
    }

    public static final Parcelable.Creator<CheckersGame> CREATOR
            = new Parcelable.Creator<CheckersGame>() {
        public CheckersGame createFromParcel(Parcel in) {
            return new CheckersGame(in);
        }

        public CheckersGame[] newArray(int size) {
            return new CheckersGame[size];
        }
    };

    /** воссоздаём объект из parcel */
    private CheckersGame(Parcel in) {

        gameBoard = in.readParcelable(Board.class.getClassLoader());
        turn = in.readInt();
    }

    /**
     * Копируем конструктор для создания новых игр
     */
    public CheckersGame(CheckersGame checkersGame){
        this.gameBoard=new Board(checkersGame.gameBoard.saveBoard());
        this.turn=checkersGame.turn;
        this.capturedBlackPieces=new ArrayList<Piece>(checkersGame.capturedBlackPieces);
        this.capturedWhitePieces=new ArrayList<Piece>(checkersGame.capturedWhitePieces);
        this.isFinished=checkersGame.isFinished;
        this.gameType=checkersGame.gameType;
        this.searchDepth=checkersGame.searchDepth;
    }
}
