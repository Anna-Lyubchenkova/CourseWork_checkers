package ru.spbstu.lyubchenkova.checkers.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import ru.spbstu.lyubchenkova.checkers.R;
import ru.spbstu.lyubchenkova.checkers.game.CheckersGame;
import ru.spbstu.lyubchenkova.checkers.game.GameType;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends BaseActivity {


    private ViewPager mViewPager;
    private ImageView mArrowLeft;
    private ImageView mArrowRight;
    private Boolean game_continuable;
    private CheckersGame currentGame;

    @Override
    protected void onStart() {
        super.onStart();
        currentGame = loadFile();
        Button game_continue = findViewById(R.id.continueButton);
        if (currentGame == null || currentGame.isGameFinished()) {
            // нет доступных сохранённых игр
            game_continuable = false;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.button_disabled);
        } else {
            game_continuable = true;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.standalone_button);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setContentView(R.layout.activity_main);

        overridePendingTransition(0, 0);
        final SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Устанавливаем ViewPager с адаптером разделов
        mViewPager = findViewById(R.id.scroller);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        int index = mSharedPreferences.getInt("lastChosenPage", 0);

        mViewPager.setCurrentItem(index);
        mArrowLeft = findViewById(R.id.arrow_left);
        mArrowRight = findViewById(R.id.arrow_right);
        Button newGameBtn = findViewById(R.id.play_button);

        //видимость стрелочек по бокам
        mArrowLeft.setVisibility((index == 0) ? View.INVISIBLE : View.VISIBLE);
        mArrowRight.setVisibility((index == mSectionsPagerAdapter.getCount() - 1) ? View.INVISIBLE : View.VISIBLE);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mArrowLeft.setVisibility((position == 0) ? View.INVISIBLE : View.VISIBLE);
                mArrowRight.setVisibility((position == mSectionsPagerAdapter.getCount() - 1) ? View.INVISIBLE : View.VISIBLE);

                //сохраняем позицию
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("lastChosenPage", position);
                editor.apply();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        currentGame = loadFile();
        Button game_continue = findViewById(R.id.continueButton);
        if (currentGame == null || currentGame.isGameFinished()) {
            // нет доступных игр
            game_continuable = false;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.button_disabled);
        } else {
            game_continuable = true;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.button_normal);
        }
    }

    private CheckersGame loadFile() {
        ObjectInputStream ois = null;
        FileInputStream fis = null;
        try {
            fis = this.openFileInput("savedata");
            ois = new ObjectInputStream(fis);
            currentGame = (CheckersGame) ois.readObject();
            return currentGame;
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

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_example;
    }

    @Override
    protected void onResume() {
        super.onResume();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.arrow_left:
                mViewPager.arrowScroll(View.FOCUS_LEFT);
                break;
            case R.id.arrow_right:
                mViewPager.arrowScroll(View.FOCUS_RIGHT);
                break;
            case R.id.play_button:
                if (game_continuable) {
                    // показываем диалог
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.OverwriteResumableGameTitle);
                    builder.setMessage(R.string.OverwriteResumableGame);

                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // delete file
                            deleteFile("savedata");
                            dialog.dismiss();
                            showDialog();
                        }
                    });

                    builder.setNegativeButton(R.string.cancel, null);
                    if (!this.isFinishing()) {
                        builder.show();
                    }
                } else {
                    showDialog();
                }

                break;
            case R.id.continueButton:
                if (game_continuable) {
                    Intent myintent = new Intent(MainActivity.this, GameActivity.class);
                    myintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myintent);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_resumable_game), Toast.LENGTH_LONG).show();
                }

                break;
            default:
                break;
        }
    }

    private void showDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.enter_players_name_title);
        alertDialogBuilder.setNegativeButton(R.string.cancel, null);

        final View alertDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_players_names, null);
        if (GameType.getValidGameTypes().get(mViewPager.getCurrentItem()) == GameType.Bot) {
            alertDialogView.findViewById(R.id.second_player_text).setVisibility(View.GONE);
        }
        alertDialogBuilder.setView(alertDialogView);
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText firstNameEditText = (EditText) alertDialogView.findViewById(R.id.first_player_text);
                String firstName = firstNameEditText.getText().toString();
                EditText secondNameEditText = (EditText) alertDialogView.findViewById(R.id.second_player_text);
                String secondName = secondNameEditText.getText().toString();

                mSharedPreferences
                        .edit()
                        .putString("first_player_name", firstName)
                        .putString("second_player_name", secondName)
                        .apply();
                startGame();
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    private void startGame() {
        GameType gameType = GameType.getValidGameTypes().get(mViewPager.getCurrentItem());

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("gameType", gameType.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (gameType == GameType.Bot) {
            SeekBar diffBar = findViewById(R.id.difficultyBar);
            mSharedPreferences.edit().putInt("lastChosenDifficulty", diffBar.getProgress()).apply();
            intent.putExtra("level", diffBar.getProgress());
        }

        startActivity(intent);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return GameTypeFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return GameType.getValidGameTypes().size();
        }
    }

    public static class GameTypeFragment extends Fragment {

        TextView levelText;
        SeekBar diffBar;

        /**
         * Аргумент фрагмента, представляющий номер раздела
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Возвращаем новый экземпляр этого фрагмента для заданного номера раздела.
         */
        public static GameTypeFragment newInstance(int sectionNumber) {
            GameTypeFragment fragment = new GameTypeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public GameTypeFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);

            GameType gameType = GameType.getValidGameTypes().get(getArguments().getInt(ARG_SECTION_NUMBER));

            TextView textView = rootView.findViewById(R.id.section_label);
            ImageView imageView = rootView.findViewById(R.id.gameTypeImage);
            diffBar = rootView.findViewById(R.id.difficultyBar);
            levelText = rootView.findViewById(R.id.levelText);

            imageView.setImageResource(gameType.getResIDImage());

            int difficulty = getDefaultSharedPreferences(getContext()).getInt("lastChosenDifficulty", 1);

            if (diffBar != null) {
                diffBar.setOnSeekBarChangeListener(seekBarChangeListener);
                diffBar.setProgress(difficulty);
            }

            if (gameType == GameType.Human) {
                diffBar.setVisibility(View.INVISIBLE);
                levelText.setVisibility(View.INVISIBLE);
            } else {
                diffBar.setVisibility(View.VISIBLE);
                levelText.setVisibility(View.VISIBLE);
            }

            textView.setText(gameType.getStringResID());
            return rootView;
        }

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                levelText.setText(getResources().getStringArray(R.array.levels)[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appbar_item1:
                int theme = mSharedPreferences.getInt("theme", 1);
                SharedPreferences.Editor ed = mSharedPreferences.edit();
                ed.putInt("theme", theme == 1 ? 2 : 1);
                ed.apply();
                setTheme();
                recreate();
                return true;

            default:
                // Если мы попали сюда, то действие пользователя не было распознано.
                // Вызываем суперкласс для его обработки.
                return super.onOptionsItemSelected(item);

        }
    }

    private void setTheme() {
        int theme = mSharedPreferences.getInt("theme", 1);
        if (theme == 1) {
            setTheme(R.style.AppTheme_NoActionBar);
        } else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
        Log.v("theme", "theme is " + theme);
    }

}
