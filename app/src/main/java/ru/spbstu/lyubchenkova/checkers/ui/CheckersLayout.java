package ru.spbstu.lyubchenkova.checkers.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;

import ru.spbstu.lyubchenkova.checkers.R;
import ru.spbstu.lyubchenkova.checkers.game.Board;
import ru.spbstu.lyubchenkova.checkers.game.CheckersGame;
import ru.spbstu.lyubchenkova.checkers.game.Move;
import ru.spbstu.lyubchenkova.checkers.game.Piece;
import ru.spbstu.lyubchenkova.checkers.game.Position;

/**
 * Класс используется для создания пользовательского представления доски шашек.
 * Размер каждой ячейки настраивается на размер экрана и обновляется при каждом изменении макета,
 * т. е. при повороте устройства. Метод обновления вызывается всякий раз,
 * когда состояние платы изменилось и представление должно быть обновлено.
 * Кроме того, движения анимируются с помощью анимации затухания
 * и затухания для улучшения видимости движений.
 */
public class CheckersLayout extends TableLayout {

    public class CheckerImageView extends AppCompatImageView {
        public int x;
        public int y;
        public CheckerImageView(Activity activity) {
            super(activity);
        }
    }

    protected GameActivity myActivity;
    protected CheckersGame myGame;
    protected CheckerImageView[][] cells;

    private final OnClickListener CellClick = new OnClickListener() {
        @Override
        public void onClick(View _view) {
            CheckerImageView view = (CheckerImageView)_view;
            myActivity.onClick(view.x, view.y);
        }
    };

    public void highlightSelectablePieces(Piece[] selectablePieces) {
        for (Piece p: selectablePieces) {
            Position pos = myGame.getBoard().getPosition(p);
            CheckerImageView posCell = cells[pos.getX()][pos.getY()];

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getContext().getTheme();
            theme.resolveAttribute(R.attr.yellow, typedValue, true);
            @ColorInt int color = typedValue.data;
            posCell.setBackgroundColor(color);
        }

    }

    public void animateMove(Move move) {
        CheckerImageView cellFrom = cells[move.start().getX()][move.start().getY()];
        CheckerImageView cellTo = cells[move.end().getX()][move.end().getY()];
        CheckerImageView cellCapturedPiece;

        int imgID = myGame.getBoard().getPiece(move.start()).getSummaryID();

        switch (imgID) {
            case 1: Glide.with(this).load(myGame.getBlackNormalIconId()).into(cellTo); break;
            case 2: Glide.with(this).load(myGame.getWhiteNormalIconId()).into(cellTo); break;
            case 3: Glide.with(this).load(myGame.getBlackKingIconId()).into(cellTo); break;
            default: Glide.with(this).load(myGame.getWhiteKingIconId()).into(cellTo); break;
        }

        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(300);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.yellow, typedValue, true);
        @ColorInt int colorYellow = typedValue.data;
        cellFrom.setBackgroundColor(colorYellow);
        cellTo.setBackgroundColor(colorYellow);
        cellFrom.startAnimation(fadeOut);


        for (Position p: move.capturePositions) {
            cellCapturedPiece = cells[p.getX()][p.getY()];

            cellCapturedPiece.setBackgroundColor(colorYellow);
            cellCapturedPiece.startAnimation(fadeOut);
        }

        for (Position p: move.positions) {
            cellCapturedPiece = cells[p.getX()][p.getY()];

            cellCapturedPiece.setBackgroundColor(colorYellow);
            cellCapturedPiece.startAnimation(fadeOut);
        }


        cellTo.startAnimation(fadeIn);
    }

    public void refresh() {
        Board myBoard = myGame.getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++)
                if (myBoard.isGameSquare(x, y)) {
                    CheckerImageView cell = cells[x][y];
                    Piece piece = myBoard.getPiece(x, y);

                    TypedValue typedValue = new TypedValue();
                    Resources.Theme theme = getContext().getTheme();
                    theme.resolveAttribute(R.attr.cellBlack, typedValue, true);
                    @ColorInt int colorBlack = typedValue.data;
                    theme.resolveAttribute(R.attr.cellSelect, typedValue, true);
                    @ColorInt int colorSelected = typedValue.data;
                    theme.resolveAttribute(R.attr.cellOption, typedValue, true);
                    @ColorInt int colorOption = typedValue.data;
                    if (piece != null) {
                        int id = piece.getSummaryID();
                        switch (id) {
                            case 1: Glide.with(this).load(myGame.getBlackNormalIconId()).into(cell); break;
                            case 2: Glide.with(this).load(myGame.getWhiteNormalIconId()).into(cell); break;
                            case 3: Glide.with(this).load(myGame.getBlackKingIconId()).into(cell); break;
                            default: Glide.with(this).load(myGame.getWhiteKingIconId()).into(cell); break;
                        }
                        if (myActivity.isSelected(piece)) {
                            cell.setBackgroundColor(colorSelected);
                        } else {
                            cell.setBackgroundColor(colorBlack);
                        }
                    } else {
                        cell.setImageDrawable(null);
                        Position curPos = new Position(x, y);
                        if (myActivity.isOption(curPos)) {
                            cell.setBackgroundColor(colorOption);
                        } else {
                            cell.setBackgroundColor(colorBlack);
                        }
                    }
                }
        }
    }

    public CheckersLayout(CheckersGame game, GameActivity activity) {
        super(activity);
        myActivity = activity;

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dimension = displayMetrics.widthPixels;
        if (displayMetrics.heightPixels < dimension) {
            dimension = displayMetrics.heightPixels;
        }
        int cellDimension = dimension / 8 ;

        LayoutParams params;

        myGame = game;
        Board myBoard = myGame.getBoard();

        params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);
        setLayoutParams(params);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.cellBlack, typedValue, true);
        @ColorInt int colorBlack = typedValue.data;
        setBackgroundColor(colorBlack);

        // добавляем таблицу картинок
        cells = new CheckerImageView[8][8];
        for (int y = 0; y < 8; y++) {
            TableRow row = new TableRow(activity);
            LayoutParams tableParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tableParams.setMargins(0, 0, 0, 0);
            row.setLayoutParams(tableParams);

            for (int x = 0; x < 8; x++) {
                CheckerImageView cell;
                cells[x][y] = cell = new CheckerImageView(activity);
                cell.x = x;
                cell.y = y;

                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                rowParams.setMargins(0, 0, 0, 0);
                rowParams.width = cellDimension;
                rowParams.height = cellDimension;
                cell.setLayoutParams(rowParams);

                int bgColor;
                if (myBoard.isGameSquare(x,y)) {
                    // добавляем обработчик нажатий
                    cell.setOnClickListener(CellClick);
                    theme.resolveAttribute(R.attr.cellBlack, typedValue, true);
                    bgColor = typedValue.data;
                }
                else {
                    theme.resolveAttribute(R.attr.cellWhite, typedValue, true);
                    bgColor = typedValue.data;
                }

                cell.setBackgroundColor(bgColor);
                row.addView(cell);
            }
            addView(row);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if(changed) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    ViewGroup.LayoutParams params = cells[i][j].getLayoutParams();
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        params.width = (right - left) / 8;
                        params.height = (right - left) / 8;

                    } else {
                        params.width = (bottom - top) / 8;
                        params.height = (bottom - top) / 8;
                    }
                }
            }
        }
    }
}
