package com.boyko.streamnews;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.boyko.streamnews.api.ApiService;
import com.boyko.streamnews.api.Client;
import com.boyko.streamnews.model.Article;
import com.boyko.streamnews.model.ArticleList;
import com.boyko.streamnews.model.ObjectNew;
import com.boyko.streamnews.utils.InternetConnection;
import com.orm.SugarContext;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Splash extends AppCompatActivity {

    private Handler mHandler = new Handler();

    private static boolean firstPageOk = false;
    private boolean isStartHandler = false;

    public final static String Q = "android";
    public final static String FROM = "2019-04-00";
    public final static String SORT_BY = "publishedAt";
    public final static String API_KEY = "26eddb253e7840f988aec61f2ece2907";

    private ArrayList<Article> articleList_fist;
    private ArrayList<ObjectNew> objectNews_first = new ArrayList<>();

    ApiService api = Client.getApiService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SugarContext.init(this);
        if (savedInstanceState!=null)
        isStartHandler = savedInstanceState.getBoolean("isStartHandler");

        int SPLASH_DISPLAY_LENGHT = 1000;
        if (!isStartHandler)
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                    Splash.this.startActivity(mainIntent);
                    Splash.this.finish();
                }
            }, SPLASH_DISPLAY_LENGHT);
            if (InternetConnection.checkConnection(getApplicationContext()))
            fetchdata(1);  // Получаем первые данные во время работы сплэш скрина
        }
        isStartHandler = true;

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isStartHandler", true);
    }

    private void fetchdata(final int number_page){

        Observable<ArticleList> articleListObservable = api.getMyJSON(Splash.Q, Splash.FROM, Splash.SORT_BY, Splash.API_KEY, number_page);

        articleListObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<ArticleList>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(ArticleList articleList) {
                        ObjectNew.deleteAll(ObjectNew.class); // Очищаем базу после первого удачного получения данных

                        articleList_fist = articleList.getArticles();
                        objectNews_first = getObjectNews(articleList_fist);

                        for (ObjectNew N : objectNews_first) N.save(); // Сохраняем объекты в базу
                        firstPageOk = true;

                        prefetch(objectNews_first); //Pre fetch следующих 5 изображений

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

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
