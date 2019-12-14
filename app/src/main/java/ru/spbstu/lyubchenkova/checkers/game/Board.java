package ru.spbstu.lyubchenkova.checkers.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Класс используется для моделирования состояния доски 8x8 для игры в шашки.
 * Каждый квадрат либо содержит часть, либо равен нулю, если он пуст.
 * При выполнении перемещения текущее состояние доски может быть изменено.
 */
public class Board implements Parcelable, Serializable {
    private Piece[][] board;

    /**
     * Создание стандартного поля
     */
    Board() {
        board = new Piece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int side = (y < 3) ? CheckersGame.BLACK : (y > 4) ? CheckersGame.WHITE : 0;
                boolean validSquare = this.isGameSquare(x, y);
                if (side != CheckersGame.NONE && validSquare) {
                    board[x][y] = new Piece(side, false);
                } else {
                    board[x][y] = null;
                }
            }
        }
    }

    /**
     * Воссоздание доски из двух массивов с информацией о позиции каждой шашки
     */
    Board(int[][] positions) {
        //this.checkersGame = checkersGame;
        board = new Piece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (positions[x][y] > CheckersGame.NONE) {
                    int side = positions[x][y] % CheckersGame.KINGED;
                    boolean kinged = positions[x][y] > CheckersGame.KINGED;
                    board[x][y] = new Piece(side, kinged);
                } else {
                    board[x][y] = null;
                }
            }
        }
    }

     /**
      * Generates a list of moves for a certain position which include at least one capture
      * Генерация списка ходов со "съедением"
      */
    private ArrayList<Move> getCaptures(Position start)
    {
        ArrayList<Move> base = new ArrayList<>();
        Piece piece = getPiece(start);
        int color = piece.getColor();
        boolean isKing = piece.isKing();

        // добавляем прыжок в каждом направлении
        Position[] directions = getDirections(color, isKing);
        for (Position dir : directions) {
            // если шашка дамка, то диапазон поиска увеличивается на 1 в каждом направлении
            if (isKing) {
                for (int i = 0; i < 8; i++) {
                    Position target = start.plus(dir);
                    for (int j = 0; j < i; j++) {
                        target = target.plus(dir);
                    }
                    Position dest = target.plus(dir);
                    Piece targetPiece = getPiece(target);
                    Piece destPiece = getPiece(dest);

                    // если 2 шашки стоят подряд или если доски нет дальше или если 1 цвет у шашек
                    // то поиск в этом направлении останавливается
                    if(!isGameSquare(target) || (targetPiece != null && (destPiece != null||targetPiece.getColor()==color))) {
                        break;
                    }

                    // смотрим на пустое место после предполагаемой для съедения шашки
                    else if (isGameSquare(dest) && destPiece == null &&
                            targetPiece != null &&
                            targetPiece.getColor() != color) {
                        Move newMove = new Move(start);
                        newMove.add(dest);
                        newMove.addCapture(target);
                        base.add(newMove);
                    }

                }
            }
            else {
                Position target = start.plus(dir);
                Position dest = target.plus(dir);
                Piece targetPiece = getPiece(target);
                Piece destPiece = getPiece(dest);
                // смотрим на пустое место после предполагаемой для съедения шашки
                if (isGameSquare(dest) && destPiece == null &&
                        targetPiece != null &&
                        targetPiece.getColor() != color) {
                    Move newMove = new Move(start);
                    newMove.add(dest);
                    newMove.addCapture(target);
                    base.add(newMove);
                }
            }
        }

        // ищем самый длинный ход
        return getCaptures(start, base);
    }

     /**
      * Пытаюсь добавить в существующий список новые шашки, которые можно съесть
      */
    private ArrayList<Move> getCaptures(Position start, ArrayList<Move> expand)
    {
        ArrayList<Move> finalCaptures = new ArrayList<>();
        ArrayList<Move> furtherCaptures = new ArrayList<>();

        Piece piece = getPiece(start);
        int color = piece.getColor();
        boolean isKing = piece.isKing();

        // пытаемся сделать более длинный ход из существующих
        for (Move move : expand) {
            Position[] directions = getDirections(color, isKing || move.isKinged());
            Position current = move.end();
            boolean continues = false;
            for (Position dir : directions)
            {
                Position target = current.plus(dir);
                Position dest = target.plus(dir);
                Piece targetPiece = getPiece(target);
                Piece destPiece = getPiece(dest);

                // смотрим на пустое место после предполагаемой для съедения шашки
                if (isGameSquare(dest) && destPiece == null &&
                        targetPiece != null &&
                        targetPiece.getColor() != color) {
                    // проверяем, что притивник не был захвачен при этом ходе
                    boolean valid = true;
                    for (Position captured : move.capturePositions) {
                        if (captured.equals(target)) {
                            valid = false;
                            break;
                        }
                    }
                    // помечаем шашку, подходящую для съедения
                    if (valid) {
                        Move newMove = new Move(move);
                        newMove.add(dest);
                        newMove.addCapture(target);
                        furtherCaptures.add(newMove);
                        continues = true;
                    }
                }
            }

            // Если нет альтернатив, то добавляем ход
            if (!continues) {
                finalCaptures.add(move);
            }
        }

        if (furtherCaptures.size() > 0) {
            furtherCaptures = getCaptures(start, furtherCaptures);
        }
        finalCaptures.addAll(furtherCaptures);

        return finalCaptures;
    }

    /**
     * Возвращаем направления движения шашки по параметрам
     */
    private Position[] getDirections(int color, boolean king) {
        if (king) {
            return new Position[]{new Position(-1, 1), new Position(1, 1),
                    new Position(-1, -1), new Position(1, -1)};
        } else if (color == CheckersGame.BLACK) {
            return new Position[]{new Position(-1, 1), new Position(1, 1)};
        } else if (color == CheckersGame.WHITE) {
            return new Position[]{new Position(-1, -1), new Position(1, -1)};
        } else {
            return new Position[]{};
        }
    }

     /**
      * Генерируем список всех возможных ходов для данной шашки
      */
    private ArrayList<Move> getMoves(Position start)
    {
        Piece piece = getPiece(start);

        ArrayList<Move> immediateMoves = new ArrayList<>();

        // проверяем соседние позиции
        Position[] neighbors = getDirections(piece.getColor(), piece.isKing());
        for (Position pos : neighbors) {
            // проверяем каждый квадрат свободен ли он для хода
            if (piece.isKing()) {
                for (int i = 0; i < 8; i++) {
                    Position dest = start.plus(pos);
                    for (int j = 0; j < i; j++) {
                        dest = dest.plus(pos);
                    }
                    Piece destPiece = getPiece(dest);

                    // добавить квадрат, если он в поле и другой шашки там нет
                    if (isGameSquare(dest) && destPiece == null) {
                        Move newMove = new Move(start);
                        newMove.add(dest);
                        immediateMoves.add(newMove);
                    }
                    else {
                        break;
                    }
                }
            }
            else {
                Position dest = start.plus(pos);
                Piece destPiece = getPiece(dest);

                if (isGameSquare(dest) && destPiece == null) {
                    Move newMove = new Move(start);
                    newMove.add(dest);
                    immediateMoves.add(newMove);
                }
            }
        }

        ArrayList<Move> captures = getCaptures(start);
        immediateMoves.addAll(captures);
        return immediateMoves;
    }

     /**
      * Генерируем список возможных ходов для игрока
      */
    Move[] getMoves(int currentPlayer) {
        ArrayList<Move> finalMoves;
        ArrayList<Move> potentialMoves = new ArrayList<>();

        // добавляем ход для каждой нужной фигуры
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = getPiece(x, y);
                if (piece != null && piece.getColor() == currentPlayer) {
                    Position start = new Position(x, y);
                    potentialMoves.addAll(
                            getMoves(start)
                    );
                }
            }
        }
        finalMoves = potentialMoves;

        boolean areCaptures = false;
        for (Move sequence : potentialMoves) {
            if (sequence.capturePositions.size() > 0) {
                areCaptures = true;
                break;
            }
        }
        if (areCaptures) {
            finalMoves = new ArrayList<>();
            for (Move sequence : potentialMoves) {
                if (sequence.capturePositions.size() > 0) {
                    finalMoves.add(sequence);
                }
            }
        }
        //возвращаем выбор как последовательность подходящих позиций
        return finalMoves.toArray(new Move[finalMoves.size()]);
    }

    /**
     * Возвращаем позицию данной шашки на доске
     */
    public Piece getPiece(int x, int y) {
        return (isGameSquare(x, y) ? board[x][y] : null);
    }

    /**
     * Возвращаем позицию данной шашки на доске
     */
    public Piece getPiece(Position pos) {
        return getPiece(pos.x, pos.y);
    }

    // ищем шашку на доске
    public Position getPosition(Piece piece) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (getPiece(x, y) == piece) {
                    return new Position(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Проверяем находятся ли координаты на игровом поле
     *
     */
    public boolean isGameSquare(int x, int y) {
        return (x >= 0 && y >= 0 && x < 8 && y < 8 && (x + y) % 2 > 0);
    }

    /**
     * Проверяем находятся ли координаты на игровом поле
     */
    private boolean isGameSquare(Position pos) {
        return isGameSquare(pos.x, pos.y);
    }

     /**
      * Делаем ход, удаляя все захваченные шашки
      */
    void makeMove(Move move) {
        Position start = move.start();
        Position end = move.end();
        Piece piece = getPiece(start);

        // очищаем посещенные позиции
        for (Position pos : move.positions) {
            board[pos.x][pos.y] = null;
        }
        // очищаем съеденные позиции
        for (Position cap : move.capturePositions) {
            board[cap.x][cap.y] = null;
        }
        // помещаем шашку в конечную позицию
        board[end.x][end.y] = piece;
        // проверяем стала ли шашка дамкой, и если нужно делаем её дамкой
        if (move.isKinged()) {
            piece.makeKing();
        }
    }

    /**
     *Сохраняем доску как двумерный массив(для восстановления)
     */
    int[][] saveBoard() {
        int[][] result = new int[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (board[x][y] != null) {
                    Piece piece = board[x][y];
                    result[x][y] = piece.getColor();
                    if (piece.isKing()) {
                        result[x][y] += CheckersGame.KINGED;
                    }
                } else {
                    result[x][y] = CheckersGame.NONE;
                }
            }
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == null)
                    dest.writeInt(0);
                else
                    dest.writeInt(board[i][j].getSummaryID());
            }
        }

    }
    public static final Parcelable.Creator<Board> CREATOR
            = new Parcelable.Creator<Board>() {
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        public Board[] newArray(int size) {
            return new Board[size];
        }
    };

    /** воссоздаём объект из parcel */
    private Board(Parcel in) {
        board = new Piece[8][8];
        int cellID = 0;

        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                cellID = in.readInt();
                switch (cellID) {
                    case 1: board[i][j] = new Piece(1, false); break;
                    case 2: board[i][j] = new Piece(2, false); break;
                    case 3: board[i][j] = new Piece(1, true); break;
                    case 4: board[i][j] = new Piece(2, true); break;
                    default:
                }
            }
        }
    }

    /*
     **Подсчитывает количество шашек с нужным ID
     */
    public int getPieceCount(int ID){
        int counter=0;
        for(Piece[] row:board){
            for (Piece p:row){
                if(p!=null&&p.getSummaryID()==ID)counter++;
            }
        }
        return counter;
    }

    /*
     *узнаем ID шашки по координатам
     */
    public int getPieceID(int x, int y){
        if(isGameSquare(x, y)){
            if(board[x][y]==null)return 0;
            return board[x][y].getSummaryID();
        }
        return -1;
    }
}
