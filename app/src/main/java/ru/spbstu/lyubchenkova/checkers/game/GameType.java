package ru.spbstu.lyubchenkova.checkers.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.LinkedList;

import ru.spbstu.lyubchenkova.checkers.R;


/**
 * Enum используется для идентификации типов игры, человек-человек, человек-бот
 */
public enum GameType implements Parcelable, Serializable {
    Bot(R.string.game_type_bot, R.drawable.icon_bot),
    Human(R.string.game_type_human,R.drawable.icon_human);

    int resIDString;
    int resIDImage;

    GameType(int resIDString, int resIDImage) {
        this.resIDImage = resIDImage;
        this.resIDString = resIDString;
    }

    public static LinkedList<GameType> getValidGameTypes() {
        LinkedList<GameType> result = new LinkedList<>();
        result.add(Human);
        result.add(Bot);
        return result;
    }

    public int getResIDImage(){return resIDImage;   }

    public int getStringResID() {
        return resIDString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
        dest.writeInt(resIDString);
        dest.writeInt(resIDImage);
    }

    public static final Parcelable.Creator<GameType> CREATOR = new Parcelable.Creator<GameType>() {
        public GameType createFromParcel(Parcel in) {
            GameType g = GameType.values()[in.readInt()];
            g.resIDString = in.readInt();
            g.resIDImage = in.readInt();
            return g;
        }

        public GameType[] newArray(int size) {
            return new GameType[size];
        }
    };
}
