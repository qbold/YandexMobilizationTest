package com.mobilization.qbold.yandexmobilizationtest;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

// Класс для загрузки и кэширования иконок
public class IconsLoader {

    private static SharedPreferences preferences;
    private static Semaphore sem = new Semaphore(10); // семафор ограничивает количество одновременно подгружаемых иконок из интернета

    public static void init() {
        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.main.getApplicationContext());
    }

    public static void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    // Загружаем иконку из интернета если её нет в хранилище и кэшируем, иначе берём иконку из хранилища
    private static void loadIcon(final String key, final String link, final SetIconCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!preferences.contains(key)) {
                    try {
                        sem.acquire();
                        byte[] data_big = Networking.runQueryBytes(link);
                        try (ByteArrayInputStream bais1 = new ByteArrayInputStream(data_big);
                             ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             DataOutputStream dos = new DataOutputStream(baos)) {
                            dos.writeInt(data_big.length);
                            dos.write(data_big);
                            SharedPreferences.Editor ed = preferences.edit();
                            ed.putString(key, Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
                            ed.commit();
                            callback.setIcon(BitmapFactory.decodeStream(bais1));
                        }
                        sem.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                String data = preferences.getString(key, "");
                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(Base64.decode(data, Base64.DEFAULT)))) {
                    int len1 = dis.readInt();
                    byte[] a = new byte[len1];
                    dis.readFully(a);
                    try (ByteArrayInputStream bais1 = new ByteArrayInputStream(a);
                    ) {
                        callback.setIcon(BitmapFactory.decodeStream(bais1));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Различаем большую и маленькую иконки для каждого исполнителя

    public static void loadBigIcon(final Singer singer, final SetIconCallback callback) {
        loadIcon(singer.getID() + "_big", singer.getBigLink(), callback);
    }

    public static void loadSmallIcon(final Singer singer, final SetIconCallback callback) {
        loadIcon(singer.getID() + "_small", singer.getSmallLink(), callback);
    }

    // Позволяет загружать иконки и обновлять отображаемые данные асинхронно
    public interface SetIconCallback {
        void setIcon(Bitmap icon);
    }
}
