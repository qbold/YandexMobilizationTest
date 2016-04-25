package com.mobilization.qbold.yandexmobilizationtest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Networking {

    // Загрузить текстовые данные
    public static String runQuery(String h) {
        HttpURLConnection con = null;
        StringBuilder buf = new StringBuilder();
        try {
            URL url = new URL(h);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            int code = con.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            reader.close();
            String sw = buf.toString();
            if (sw.equals("")) {
                sw = "{}";
            }
            return sw;
        } catch (Exception exc) {
            exc.printStackTrace();
            if (con != null) con.disconnect();
        }
        return "";
    }

    // Загрузить бинарные данные
    public static byte[] runQueryBytes(String h) throws IOException {
        HttpURLConnection con = null;
        ByteArrayOutputStream baos = null;
        try {
            URL url = new URL(h);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            int code = con.getResponseCode();
            InputStream is = con.getInputStream();
            baos = new ByteArrayOutputStream();
            int rd;
            byte[] bt = new byte[4096];
            while ((rd = is.read(bt)) != -1) {
                baos.write(bt, 0, rd);
            }
            is.close();
            return baos.toByteArray();
        } catch (Exception exc) {
            exc.printStackTrace();
            if (con != null) con.disconnect();
        } finally {
            baos.close();
        }
        return null;
    }

    // Загружаем список исполнителей в ArrayList
    public static void loadSingers(final ArrayList<Singer> l, final NetworkingCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Singer> list = Singer.parseSingers(runQuery("http://cache-default04h.cdn.yandex.net/download.cdn.yandex.net/mobilization-2016/artists.json"));
                    l.clear();
                    for (Singer s : list) {
                        l.add(s);
                    }
                    callback.done();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Необходимо для оповещения об окончании загрузки json'а
    public interface NetworkingCallback {
        void done();
    }
}
