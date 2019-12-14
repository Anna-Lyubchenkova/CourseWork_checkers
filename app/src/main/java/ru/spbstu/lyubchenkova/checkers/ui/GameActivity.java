package ru.spbstu.lyubchenkova.checkers.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import ru.spbstu.lyubchenkova.checkers.App;
import ru.spbstu.lyubchenkova.checkers.R;
import ru.spbstu.lyubchenkova.checkers.database.ScoreDao;
import ru.spbstu.lyubchenkova.checkers.database.ScoreEntry;
import ru.spbstu.lyubchenkova.checkers.game.Board;
import ru.spbstu.lyubchenkova.checkers.game.CheckersGame;
import ru.spbstu.lyubchenkova.checkers.game.GameType;
import ru.spbstu.lyubchenkova.checkers.game.Move;
import ru.spbstu.lyubchenkova.checkers.game.Piece;
import ru.spbstu.lyubchenkova.checkers.game.Position;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class GameActivity extends AppCompatActivity {
    private Handler mHandler;
    SharedPreferences mSharedPreferences;
    ScoreDao database;

    String firstPlayerName;
    String secondPlayerName;

    private CheckersGame game;
    private CheckersLayout checkersView;
    private TextView currentPlayerText;
    private LinearLayout capturedBlackPiecesUI;
    private LinearLayout capturedWhitePiecesUI;
    Dialog dialog;
    boolean actionInProgress;
    int maxDepth;
    int startOwnPieces, startOwnKings, startEnemyPieces, startEnemyKings;


    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        setTheme();

        mHandler = new Handler();
        database = App.getInstance().getDatabase().scoreDao();

        game = loadFile();
        actionInProgress = false;
        setContentView(R.layout.activity_game);

        RelativeLayout content = findViewById((R.id.content_layout));

        if (saved == null) {
            if ((game == null || getIntent().getExtras() != null)) {
                Bundle extras = getIntent().getExtras();
                GameType gameType = GameType.valueOf(extras.getString("gameType", GameType.Bot.name()));
                game = new CheckersGame(gameType, extras.getInt("level"));
            }
        }
        maxDepth = game.searchDepth;

        //  else game = saved.getParcelable("gameController");

        // generate new layout for the board
        checkersView = new CheckersLayout(game, this);
        checkersView.refresh();

        // макеты, которые содержат все элементы, отображаемые в игре
        LinearLayout mainContentLayout = findViewById(R.id.main_content);
        LinearLayout sideContentLayout = findViewById(R.id.side_content);

        ConstraintLayout playersNamesLayout = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.players_names_layout, null);

        TextView firstPlayerText = playersNamesLayout.findViewById(R.id.first_player_name_text);
        TextView secondPlayerText = playersNamesLayout.findViewById(R.id.second_player_name_text);

        //test
        firstPlayerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWinDialog();
            }
        });

        firstPlayerName = mSharedPreferences.getString("first_player_name", "");
        secondPlayerName = mSharedPreferences.getString("second_player_name", "");
        if (game.getGameType() == GameType.Bot) {
            firstPlayerName = firstPlayerName.equals("") ? getResources().getString(R.string.first_player) : firstPlayerName;
            secondPlayerName = getResources().getString(R.string.game_type_bot);
        } else {
            firstPlayerName = firstPlayerName.equals("") ? getResources().getString(R.string.first_player) : firstPlayerName;
            secondPlayerName = secondPlayerName.equals("") ? getResources().getString(R.string.second_player) : secondPlayerName;
        }

        firstPlayerText.setText(firstPlayerName);
        secondPlayerText.setText(secondPlayerName);

        mainContentLayout.addView(playersNamesLayout);
        mainContentLayout.addView(checkersView);

        // Текст показывающий чей сейчас ход
        currentPlayerText = new TextView(this);
        currentPlayerText.setTextSize(24);
        currentPlayerText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sideContentLayout.addView(currentPlayerText);

        // layout для захваченных шашек
        capturedBlackPiecesUI = new LinearLayout(this);
        capturedBlackPiecesUI.setOrientation(LinearLayout.HORIZONTAL);

        capturedWhitePiecesUI = new LinearLayout(this);
        capturedWhitePiecesUI.setOrientation(LinearLayout.HORIZONTAL);

        sideContentLayout.addView(capturedBlackPiecesUI);
        sideContentLayout.addView(capturedWhitePiecesUI);


        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener);

    }

    private void setTheme() {
        mSharedPreferences = getDefaultSharedPreferences(this);
        int theme = mSharedPreferences.getInt("theme", 1);
        if (theme == 1) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDark);
        }
        Log.v("theme", "theme is " + theme);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        game = null;

        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener);
    }

    private ImageView generatePieceImage(int id) {
        ImageView image = new ImageView(this);


        int pixels = getResources().getConfiguration().orientation != ORIENTATION_LANDSCAPE ?
                Resources.getSystem().getDisplayMetrics().widthPixels / 12 :
                Resources.getSystem().getDisplayMetrics().widthPixels / 12 / 2;

        image.setLayoutParams(new LinearLayout.LayoutParams(pixels, pixels));
        switch (id) {
            case 1:
                Glide.with(this).load(game.getBlackNormalIconId()).into(image);
                break;
            case 2:
                Glide.with(this).load(game.getWhiteNormalIconId()).into(image);
                break;
            case 3:
                Glide.with(this).load(game.getBlackKingIconId()).into(image);
                break;
            default:
                Glide.with(this).load(game.getWhiteKingIconId()).into(image);
                break;
        }

        return image;
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepTurn();
    }

    Piece selectedPiece;
    Position selectedPosition;
    Piece[] selectablePieces;
    Position[] moveOptions;


    // подготавливаем ход
    public void prepTurn() {
        Board board = game.getBoard();

        selectedPiece = null;
        selectedPosition = null;
        selectablePieces = null;
        moveOptions = null;

        int turn = game.whoseTurn();

        if (game.getGameType() == GameType.Bot && turn == CheckersGame.BLACK) {
            currentPlayerText.setText(R.string.game_current_player_ai);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    makeComputerTurn();
                    actionInProgress = false;
                }
            }, 1000);

        } else {
            if (turn == CheckersGame.BLACK)
                currentPlayerText.setText(R.string.game_current_player_black);
            else
                currentPlayerText.setText(R.string.game_current_player_white);
            // подготовка хода для человека
            ArrayList<Piece> selectablePieces = new ArrayList<>();
            Move[] moves = game.getMoves();

            // ищем шашки, которыми можно походить
            for (Move move : moves) {
                Piece newPiece = board.getPiece(move.start());
                if (!selectablePieces.contains(newPiece)) {
                    selectablePieces.add(newPiece);
                }
            }

            // конвертируем в массив
            this.selectablePieces = selectablePieces.toArray(
                    new Piece[selectablePieces.size()]
            );

            if (selectablePieces.size() == 0) {
                game.setGameFinished(true);
                deleteFile("savedata");
                showWinDialog();
            }
        }

        updateCapturedPiecesUI();
        checkersView.refresh();
    }

    // ход бота
    private void makeComputerTurn() {
        if (game.whoseTurn() == CheckersGame.BLACK) {
            Move[] moves = game.getMoves();
            if (moves.length > 0) {
                //available: moves--> captures
                //int num = (int)(moves.length * Math.random());
                //final Move choice = moves[num];
                switch (game.searchDepth) {
                    case 0:
                        //0-1
                        maxDepth = new Random().nextInt(2);
                        break;
                    case 4:
                        //3-5
                        maxDepth = new Random().nextInt(3) + 3;
                        break;
                    case 8:
                        //7-9
                        maxDepth = new Random().nextInt(3) + 7;
                        break;
                    case 12:
                        //10-14
                        maxDepth = new Random().nextInt(5) + 10;
                        break;
                    case 16:
                        //15-20
                        maxDepth = new Random().nextInt(6) + 15;
                        break;
                }
                final Move choice = alphabetaSearch(moves);


                checkersView.animateMove(choice);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (game != null) {
                            game.makeMove(choice);
                            prepTurn();
                        }
                    }
                }, 300);


            } else {
                // игрок выиграл
                game.setGameFinished(true);
                showWinDialog();
            }
        }
    }

    /**
     * Алгоритм поиска хода, который воспроизводит будущее возможных ходов до maxdepth
     * и возвращает ход с наилучшим значением
     */
    Move alphabetaSearch(Move[] legalMoves) {
        Move best = null;
        ArrayList<Move> currentBest;
        startOwnPieces = game.getBoard().getPieceCount(1);
        startOwnKings = game.getBoard().getPieceCount(3);
        startEnemyPieces = game.getBoard().getPieceCount(2);
        startEnemyKings = game.getBoard().getPieceCount(4);

        currentBest = new ArrayList<Move>();
        int maxValue = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            //делаем ход и оцениваем игру
            CheckersGame next = new CheckersGame(game);
            next.makeMove(move);

            int value = min(next, Integer.MAX_VALUE, Integer.MIN_VALUE, 0);

            //определяем нужный ли ход
            if (value == maxValue) {
                currentBest.add(move);
            } else if (value > maxValue) {
                currentBest.clear();
                currentBest.add(move);
                maxValue = value;
            }

        }
        //Выбираем рандомный ход из лучших
        int ran = (int) (currentBest.size() * Math.random());
        best = currentBest.get(ran);

        return best;
    }

    /**
     * Алгоритм минимизации (перспектива врагов),
     * который чередуется с максимизацией (перспектива бота).
     */
    int min(CheckersGame game, int alpha, int beta, int depth) {
        Move[] legalMoves = game.getMoves();
        if (legalMoves.length == 0 || depth == maxDepth) {
            return evaluation(game);
        }
        int val = Integer.MAX_VALUE;
        for (Move move : legalMoves) {
            CheckersGame next = new CheckersGame(game);
            next.makeMove(move);
            val = Math.min(val, max(next, alpha, beta, depth + 1));
            if (val <= alpha) return val;
            beta = Math.min(beta, val);
        }

        return val;
    }

    /**
     * Алгоритм максимизации (перспектива бота),
     * который чередуется с минимизацией (перспектива врагов).
     */
    int max(CheckersGame game, int alpha, int beta, int depth) {
        Move[] legalMoves = game.getMoves();
        if (legalMoves.length == 0 || depth == maxDepth) {
            return evaluation(game);
        }
        int val = Integer.MIN_VALUE;
        for (Move move : legalMoves) {
            CheckersGame next = new CheckersGame(game);
            next.makeMove(move);
            val = Math.max(val, min(next, alpha, beta, depth + 1));
            if (val >= beta) return val;
            alpha = Math.max(alpha, val);
        }
        return val;
    }

    /**
     * вызывается при достижении максимальной глубины, задает значение состояния игры.
     */
    int evaluation(CheckersGame game) {
        int gameValue = 0;
        int ownPieces = 0;
        int ownKings = 0;
        int enemyPieces = 0;
        int enemyKings = 0;
        Board board = game.getBoard();


        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                switch (board.getPieceID(i, j)) {
                    case 1:
                        ownPieces++;
                        gameValue += defending(i, j, board) * 50 + (i == 0 ? 50 : 0) + 15 * i + 100 - ((Math.abs(4 - i) + Math.abs(4 - j)) * 10);
                        break;
                    case 2:
                        enemyPieces++;
                        gameValue -= defending(i, j, board) * 50 + (i == 0 ? 50 : 0) + 15 * (7 - i) + 100 - ((Math.abs(4 - i) + Math.abs(4 - j)) * 10);
                        break;
                    case 3:
                        ownKings++;
                        gameValue += 100 - ((Math.abs(4 - i) + Math.abs(4 - j)) * 10);
                        break;
                    case 4:
                        enemyKings++;
                        gameValue -= 100 - ((Math.abs(4 - i) + Math.abs(4 - j)) * 10);
                        break;
                }
            }
        }


        if (startOwnPieces + startOwnKings > startEnemyPieces + startEnemyKings
                && enemyPieces + enemyKings != 0
                && startEnemyPieces + startEnemyKings != 0
                && startEnemyKings != 1) {
            if ((ownPieces + ownKings) / (enemyPieces + enemyKings) > (startOwnPieces + startOwnKings) / (startEnemyPieces + startEnemyKings)) {
                gameValue += 150;
            } else {
                gameValue -= 150;
            }
        }

        //добавление результирующих частей для оценки
        gameValue += 600 * ownPieces + 1000 * ownKings - 600 * enemyPieces - 1000 * enemyKings;


        //Когда есть несколько ходов проверяем количество
        if (startOwnKings + startOwnPieces < 6 || startEnemyPieces + startEnemyKings < 6) {
            Move[] blackMoves = game.getMoves(CheckersGame.BLACK);
            Move[] whiteMoves = game.getMoves(CheckersGame.WHITE);
            if (blackMoves.length < 1) {
                return Integer.MIN_VALUE;
            }
            if (whiteMoves.length < 1) {
                return Integer.MAX_VALUE;
            }
        }

        if (enemyPieces + enemyKings == 0 && ownPieces + ownKings > 0) {
            gameValue = Integer.MAX_VALUE;
        }
        if (ownPieces + ownKings == 0 && enemyPieces + enemyKings > 0) {
            gameValue = Integer.MIN_VALUE;
        }

        return gameValue;
    }

    /**
     * присваиваем значение "безопасности" определенной фигуры на доске, в зависимости от защищающихся фигур
     */
    int defending(int yrow, int xcolumn, Board board) {
        int n = 0;

        switch (board.getPieceID(xcolumn, yrow)) {
            case 1:
                if (xcolumn + 1 < 8 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn + 1, yrow + 1) & 1) == 1) {
                        n++;
                    }
                }
                if (xcolumn + 1 < 8 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn + 1, yrow - 1) & 1) == 1) {
                        n++;
                    }
                }
                break;

            case 2:
                if (xcolumn - 1 >= 0 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn - 1, yrow + 1) & 1) == 0) {
                        n++;
                    }
                }
                if (xcolumn - 1 >= 0 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn - 1, yrow - 1) & 1) == 0) {
                        n++;
                    }
                }
                break;

            case 3:
                if (xcolumn + 1 < 8 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn + 1, yrow + 1) & 1) == 1) {
                        n++;
                    }
                }
                if (xcolumn + 1 < 8 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn + 1, yrow - 1) & 1) == 1) {
                        n++;
                    }
                }
                if (xcolumn - 1 >= 0 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn - 1, yrow + 1) & 1) == 1) {
                        n++;
                    }
                }
                if (xcolumn - 1 >= 0 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn - 1, yrow - 1) & 1) == 1) {
                        n++;
                    }
                }
                break;

            case 4:
                if (xcolumn + 1 < 8 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn + 1, yrow + 1) & 1) == 0) {
                        n++;
                    }
                }
                if (xcolumn + 1 < 8 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn + 1, yrow - 1) & 1) == 0) {
                        n++;
                    }
                }
                if (xcolumn - 1 >= 0 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn - 1, yrow + 1) & 1) == 0) {
                        n++;
                    }
                }
                if (xcolumn - 1 >= 0 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn - 1, yrow - 1) & 1) == 0) {
                        n++;
                    }
                }
                break;

        }

        return n;
    }

    private void updateCapturedPiecesUI() {
        int index;
        while (game.getCapturedBlackPieces().size() > capturedBlackPiecesUI.getChildCount()) {
            index = capturedBlackPiecesUI.getChildCount();
            capturedBlackPiecesUI.addView(generatePieceImage(game.getCapturedBlackPieces().get(index).getSummaryID()));
        }
        while (game.getCapturedWhitePieces().size() > capturedWhitePiecesUI.getChildCount()) {
            index = capturedWhitePiecesUI.getChildCount();
            capturedWhitePiecesUI.addView(generatePieceImage(game.getCapturedWhitePieces().get(index).getSummaryID()));
        }


    }

    // проверяем какая шашка выбрана
    public boolean isSelected(Piece piece) {
        return (piece != null && piece == selectedPiece);
    }

    // проверяем какой квадрат свободен
    public boolean isOption(Position checkPosition) {
        if (moveOptions == null) {
            return false;
        }
        for (Position position : moveOptions) {
            if (position.equals(checkPosition)) {
                return true;
            }
        }
        return false;
    }

    public void selectPiece(Piece piece, Position location) {
        selectedPiece = null;
        selectedPosition = null;
        moveOptions = null;

        if (piece != null && selectablePieces != null
                && piece.getColor() == game.whoseTurn()) {
            boolean isSelectable = false;
            for (Piece selectablePiece : selectablePieces) {
                if (selectablePiece == piece) {
                    isSelectable = true;
                }
            }

            if (isSelectable) {
                selectedPiece = piece;
                selectedPosition = location;

                // заполняем список возможных ходов

                ArrayList<Position> moveOptionsArr = new ArrayList<>();

                Move[] allMoves = game.getMoves();

                // Повторяем для каждого хода
                for (Move checkMove : allMoves) {
                    Position start = checkMove.start();
                    Position end = checkMove.end();

                    if (start.equals(location)) {
                        if (!moveOptionsArr.contains(end)) {
                            moveOptionsArr.add(end);
                        }
                    }
                }

                // сохраняем список результатов
                moveOptions = moveOptionsArr.toArray(new Position[moveOptionsArr.size()]);
            }
        }

        checkersView.refresh();
    }

    // Игрок делает ход
    public void makeMove(Position destination) {
        // делаем самый длинный возможный ход
        final Move move = game.getLongestMove(selectedPosition, destination);
        if (move != null) {
            checkersView.animateMove(move);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (game != null) {
                        game.makeMove(move);
                        prepTurn();
                        actionInProgress = false;
                    }
                }
            }, 300);
        } else {
            actionInProgress = false;
        }
    }

    // игрок производит нажатие
    public void onClick(int x, int y) {
        if (!actionInProgress) {
            if (game.getGameType() == GameType.Bot && game.whoseTurn() == CheckersGame.BLACK)
                ;
            else {
                Position location = new Position(x, y);
                Piece targetPiece = game.getBoard().getPiece(x, y);

                // пытаемся сделать ход
                if (selectedPiece != null && selectedPosition != null && targetPiece == null) {
                    //game.advanceTurn();
                    actionInProgress = true;
                    makeMove(location);
                } else {
                    selectPiece(targetPiece, location);
                    if (selectedPiece == null)
                        checkersView.highlightSelectablePieces(selectablePieces);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkersView.refresh();
                        }
                    }, 300);
                }
            }
        }
    }


    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    // обновляем состояние счёта
                    prepTurn();
                }
            };

    /**
     * Показываем диалог когда кто-то побеждает
     */
    public void showWinDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);

        final ScoreEntry newScoreItem = new ScoreEntry();
        final ScoreEntry oldScoreFirstPlayer = new ScoreEntry();
        final ScoreEntry oldScoreSecondPlayer = new ScoreEntry();
        Thread databaseThread = new Thread(new Runnable() {
            public void run() {
                ScoreEntry oldScoreEntryFirstPlayer = database.getByName(firstPlayerName);
                ScoreEntry oldScoreEntrySecondPlayer = database.getByName(secondPlayerName);
                oldScoreFirstPlayer.setScore(oldScoreEntryFirstPlayer == null ? 0 : oldScoreEntryFirstPlayer.getScore());
                oldScoreSecondPlayer.setScore(oldScoreEntrySecondPlayer == null ? 0 : oldScoreEntrySecondPlayer.getScore());
            }
        });
        databaseThread.start();
        try {
            databaseThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (game.getGameType() == GameType.Bot) {
            if (game.whoseTurn() == CheckersGame.BLACK) {
                builder.setTitle(R.string.playerWinDialogTitle);
                builder.setMessage(R.string.playerWinDialogText);
                newScoreItem.setPlayerName(firstPlayerName);
                newScoreItem.setScore(oldScoreFirstPlayer.getScore() + 10);
            } else {
                builder.setTitle(R.string.botWinDialogTitle);
                builder.setMessage(R.string.botWinDialogText);
            }
        } else {
            builder.setTitle(R.string.playerWinDialogTitle);
            if (game.whoseTurn() == CheckersGame.BLACK) {
                builder.setMessage(R.string.whiteWinDialogText);
                newScoreItem.setPlayerName(firstPlayerName);
                newScoreItem.setScore(oldScoreFirstPlayer.getScore() + 10);
                Log.v("tag", "updated score is: " + newScoreItem.getScore());
            } else {
                builder.setMessage(R.string.blackWinDialogText);
                newScoreItem.setPlayerName(secondPlayerName);
                newScoreItem.setScore(oldScoreSecondPlayer.getScore() + 10);
            }
        }


        builder.setPositiveButton(R.string.sWinDialogBack, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                new Thread(new Runnable() {
                    public void run() {
                        if (database.getByName(firstPlayerName) == null && database.getByName(secondPlayerName) == null) {
                            database.insert(newScoreItem);
                        } else {
                            database.updateScore(newScoreItem.getPlayerName(), newScoreItem.getScore());
                        }
                    }
                }).start();
                startActivity(intent);
                dialog.dismiss();
            }
        });
        if (!this.isFinishing()) {
            builder.show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Сохраняем текущее состояния игры

        savedInstanceState.putParcelable("game", game);

        // Сохраняем состояние иерархии представлений
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }

    public void onPause() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        //state will be saved in a file
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = openFileOutput("savedata", Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(game);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) try {
                oos.close();
            } catch (IOException ignored) {
            }
            if (fos != null) try {
                fos.close();
            } catch (IOException ignored) {
            }
        }

        super.onPause();
    }

    private CheckersGame loadFile() {
        ObjectInputStream ois = null;
        FileInputStream fis = null;
        try {
            fis = this.openFileInput("savedata");
            ois = new ObjectInputStream(fis);
            game = (CheckersGame) ois.readObject();
            return game;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (fis != null) try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void onBackPressed() {
        Intent mainActivity = new Intent(GameActivity.this, MainActivity.class);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
        finish();
    }
}


