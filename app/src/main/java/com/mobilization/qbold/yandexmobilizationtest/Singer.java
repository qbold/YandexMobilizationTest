package com.mobilization.qbold.yandexmobilizationtest;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// Модель исполнителя
public class Singer implements Parcelable {

    private String name; // Имя или псевдоним
    private String genres; // Строка жанров
    private Bitmap small; // Фотки
    private String small_link, big_link; // Ссылки на фотки
    private String description; // Описание
    private String link; // Ссылка
    private String albums, tracks; // N альбомов, K песен
    private String id;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSmallLink() {
        return small_link;
    }

    public String getBigLink() {
        return big_link;
    }

    public String getID() {
        return id;
    }

    public String getAlbums() {
        return albums;
    }

    public String getTracks() {
        return tracks;
    }

    public Bitmap getSmallIcon() {
        return small;
    }

    public String getGenres() {
        return genres;
    }

    public void setIcons(Bitmap small) {
        this.small = small;
    }

    // Собираем жанры исполнителя из json массива в строку
    private static String buildGenres(JSONArray ob) throws JSONException {
        String[] ar = new String[ob.length()];
        for (int i = 0; i < ob.length(); i++) {
            ar[i] = ob.getString(i);
        }
        return TextUtils.join(", ", ar);
    }

    private static String genTracks(int tr) {
        if (tr == 1) return "песня";
        if (tr > 1 && tr < 5) return "песни";
        return "песен";
    }

    private static String genAlbums(int alb) {
        if (alb == 1) return "альбом";
        if (alb > 1 && alb < 5) return "альбома";
        return "альбомов";
    }

    // Парсинг списка исполнителей из json массива
    public static ArrayList<Singer> parseSingers(String json) {
        ArrayList<Singer> s = new ArrayList<>();
        try {
            JSONArray ar = new JSONArray(json);
            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);
                Singer sing = new Singer();
                sing.id = obj.getString("id");
                sing.name = obj.getString("name");
                sing.small_link = obj.getJSONObject("cover").getString("small");
                sing.big_link = obj.getJSONObject("cover").getString("big");
                sing.genres = buildGenres(obj.getJSONArray("genres"));
                sing.description = obj.getString("description");
                sing.albums = obj.getString("albums") + " " + genAlbums(Integer.parseInt(obj.getString("albums")));
                sing.tracks = obj.getString("tracks") + " " + genTracks(Integer.parseInt(obj.getString("tracks")));
                if (obj.has("link"))
                    sing.link = obj.getString("link");
                s.add(sing);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return s;
    }

    // Всё что ниже - необходимо для передачи объекта Singer между активностями

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(genres);
        dest.writeString(small_link);
        dest.writeString(big_link);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(albums);
        dest.writeString(tracks);
    }

    public static final Parcelable.Creator<Singer> CREATOR
            = new Parcelable.Creator<Singer>() {
        public Singer createFromParcel(Parcel in) {
            return new Singer(in);
        }

        public Singer[] newArray(int size) {
            return new Singer[size];
        }
    };

    public Singer() {
    }

    private Singer(Parcel in) {
        id = in.readString();
        name = in.readString();
        genres = in.readString();
        small_link = in.readString();
        big_link = in.readString();
        description = in.readString();
        link = in.readString();
        albums = in.readString();
        tracks = in.readString();
    }
}
