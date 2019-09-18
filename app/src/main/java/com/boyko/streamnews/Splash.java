package com.boyko.streamnews;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.boyko.streamnews.api.ApiService;
import com.boyko.streamnews.api.Client;
import com.boyko.streamnews.model.Article;
import com.boyko.streamnews.model.ArticleList;
import com.boyko.streamnews.model.ObjectNew;
import com.orm.SugarContext;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Splash extends AppCompatActivity {

    private Handler mHandler = new Handler();

    private static boolean firstPageOk = false;

    public final static String Q = "android";
    public final static String FROM = "2019-04-00";
    public final static String SORT_BY = "publishedAt";
    public final static String API_KEY = "26eddb253e7840f988aec61f2ece2907";

    private ArrayList<Article> articleList_fist;
    private ArrayList<ObjectNew> objectNews_first = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SugarContext.init(this);

        int SPLASH_DISPLAY_LENGHT = 3000;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGHT);

        loadfirstpage();  // Получаем первые данные во время работы сплэш скрина
    }

    private void loadfirstpage() {
        try {

            final ApiService api = Client.getApiService();

            Call<ArticleList> call = api.getMyJSON(Q, FROM, SORT_BY, API_KEY, 1);

            call.enqueue(new Callback<ArticleList>() {
                @Override
                public void onResponse(Call<ArticleList> call, Response<ArticleList> response) {

                    if (response.isSuccessful()) {

                        ObjectNew.deleteAll(ObjectNew.class); // Очищаем базу перед первым удачным получением данных

                        articleList_fist = response.body().getArticles();
                        objectNews_first = getObjectNews(articleList_fist);

                        for (ObjectNew N : objectNews_first) N.save(); // Сохраняем объекты в базу
                        firstPageOk = true;

                        prefetch(objectNews_first); //Pre fetch следующих 10 изображений

                    } else {
                        System.out.println("my Ошибка получения данных ");
                    }
                }

                @Override
                public void onFailure(Call<ArticleList> call, Throwable t) {
                    System.out.println("my Ошибка получения данных ");
                }
            });

        } catch(Exception ex) {
            System.out.println("my tag Exception catch ");
        }
    }
    //______________________ Helpers

    private static ArrayList<ObjectNew> getObjectNews(ArrayList<Article> art){   // конвертация объектов
        ArrayList<ObjectNew> news = new ArrayList<>();
        for (Article A : art){
            news.add(new ObjectNew(A.getTitle()
                    , A.getDescription()
                    , A.getSource().getName()
                    , A.getUrlToImage()
                    , A.getPublishedAt()));
        }
        return news;
    }
    //Pre fetch следующих 5 изображений
    private void prefetch(ArrayList<ObjectNew> array) {
        for (int i = 0 ; i < 5; i++) {
            if (i < array.size()){
                String url = array.get(i).getUrlToImage();
                if (url!= null && url.length()!=0){
                    Picasso.get().load(url).fetch();
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeCallbacksAndMessages(null);
    }
    public static boolean isFirstPageOk() {
        return firstPageOk;
    }

    public static void setFirstPageOk(boolean firstPageOk) {
        Splash.firstPageOk = firstPageOk;
    }
}
