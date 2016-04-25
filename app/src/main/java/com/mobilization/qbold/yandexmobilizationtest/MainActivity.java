package com.mobilization.qbold.yandexmobilizationtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static MainActivity main;
    public AppAdapter adp;
    private ListView listView;
    private ArrayList<Singer> ad;

    private int firstVisible, countVisible, old_first;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = this;
        IconsLoader.init();
        ad = new ArrayList<>();
        setContentView(R.layout.activity_main);
        ini();
    }

    // Open the second activity
    private void viewSinger(Singer s) {
        Intent in = new Intent(this, SingerActivity.class);
        in.putExtra("singer", s);
        startActivity(in);
    }

    private void ini() {
        listView = ((ListView) findViewById(R.id.listView));
        adp = new AppAdapter();
        listView.setAdapter(adp);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                if (ad == null || ad.isEmpty()) return;
                viewSinger(ad.get(position));
            }
        });



        // У нас ограничено количество одновременно подгружаемых иконок 10 штуками. Но пользователь может листать список очень быстро
        // Поэтому начинаем подгружать только самые актуальные иконки (которые видимы пользователю более 300 миллисекунд)
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstVisible = firstVisibleItem;
                countVisible = visibleItemCount;
            }
        });
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (firstVisible == old_first) {
                    for (int i = firstVisible; i < firstVisible + countVisible; i++) {
                        loadIconSinger(ad.get(i));
                    }
                }
                old_first = firstVisible;
            }
        }, 0, 300);




        // Загружаем данные об исполнителях (Кэшировать json не очень было бы хорошо, т.к. он может измениться на сервере)
        Networking.loadSingers(ad, new Networking.NetworkingCallback() {
            @Override
            public void done() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adp.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    // Подгрузка иконки для указанного исполнителя
    private void loadIconSinger(final Singer s) {
        if (s.getSmallIcon() == null) {
            IconsLoader.loadSmallIcon(s, new IconsLoader.SetIconCallback() {
                @Override
                public void setIcon(Bitmap small) {
                    s.setIcons(small);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adp.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    // Адаптер для списка исполнителей
    class AppAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ad.size();
        }

        @Override
        public Singer getItem(int position) {
            return ad.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.button, null);
                new ViewHolder(convertView);
            }
            final ViewHolder holder = (ViewHolder) convertView.getTag();
            final Singer item = getItem(position);
            holder.name.setText(item.getName());
            holder.genres.setText(item.getGenres());
            holder.disco.setText(item.getAlbums() + ", " + item.getTracks());
            if (item.getSmallIcon() == null) {
                holder.progress.setVisibility(View.VISIBLE);
                holder.icon.setImageBitmap(null);
            } else {
                holder.progress.setVisibility(View.GONE);
                holder.icon.setImageBitmap(item.getSmallIcon());
            }
            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView genres;
            TextView disco;
            ProgressBar progress;
            ImageView icon;

            public ViewHolder(View view) {
                icon = (ImageView) view.findViewById(R.id.imageView);
                name = (TextView) view.findViewById(R.id.textView);
                genres = (TextView) view.findViewById(R.id.textView2);
                disco = (TextView) view.findViewById(R.id.textView3);
                progress = (ProgressBar) view.findViewById(R.id.pb);
                view.setTag(this);
            }
        }
    }
}
