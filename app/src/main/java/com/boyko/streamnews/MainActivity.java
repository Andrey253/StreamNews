package com.boyko.streamnews;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boyko.streamnews.adapter.RVAdapter;
import com.boyko.streamnews.api.ApiService;
import com.boyko.streamnews.api.Client;
import com.boyko.streamnews.model.Article;
import com.boyko.streamnews.model.ArticleList;
import com.boyko.streamnews.model.ObjectNew;
import com.boyko.streamnews.utils.InternetConnection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.orm.SugarContext;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements RVAdapter.customButtonListener{

    private View parentView;

    private RVAdapter adapter;

    private ArrayList<ObjectNew> objectNews; // Создание новых объектов с нужными полями для адаптера

    private LinearLayoutManager linearLayoutManager;

    public final static int TOTAL_PAGE = 5;

    public static int current_page=0;

    ApiService api = Client.getApiService();

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SugarContext.init(this);

        parentView = findViewById(R.id.main_activity);
        RecyclerView recyclerView = findViewById(R.id.recycle);
        objectNews = (ArrayList<ObjectNew>) ObjectNew.listAll(ObjectNew.class); // Получаем данные из базы
        adapter = new RVAdapter(MainActivity.this, objectNews);
        adapter.setCustomButtonListner(MainActivity.this); // Слушатель на адаптер, обработка нажатия на кнопку "Загрузить новости"
        recyclerView.setAdapter(adapter);

        linearLayoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) { // Обработчик достижения конца списка
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItemPosition)+5 >= totalItemCount
                        && current_page<TOTAL_PAGE
                        && InternetConnection.checkConnection(getApplicationContext()))
                {

                    current_page+=1;
                    fetchdata(current_page);
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab); // загрузка всех новостей заново с первой страницы
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetConnection.checkConnection(getApplicationContext()) /*&& !isLoading*/){

                    current_page=1;
                    fetchdata(current_page);
                } else {
                    Snackbar.make(parentView, R.string.string_internet_connection_not_available, Snackbar.LENGTH_LONG).show();
                }

            }});

        if (Splash.isFirstPageOk()){
            Splash.setFirstPageOk(false);
            current_page=1;
            Snackbar.make(parentView, R.string.first_page_is_ok, Snackbar.LENGTH_LONG).show();
        }else if (!InternetConnection.checkConnection(getApplicationContext())){
            Snackbar.make(parentView, R.string.offline_data, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onButtonClickListner() {

        if (InternetConnection.checkConnection(getApplicationContext()) /*&& !isLoading*/){

            if (current_page<TOTAL_PAGE){

                current_page+=1;
                fetchdata(current_page);
            }
            else {
                Snackbar.make(parentView, R.string.string_is_all_new_downloaded, Snackbar.LENGTH_LONG).show();
            }
        }else {
            Snackbar.make(parentView, R.string.string_internet_connection_not_available, Snackbar.LENGTH_LONG).show();
        }
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

                        if (current_page==1){
                            ObjectNew.deleteAll(ObjectNew.class);   // Очищаем базу перед первым удачным получением данных
                            adapter.clear();                        // Очищаем адаптер
                        }
                        objectNews = getObjectNews(articleList.getArticles());

                        for (ObjectNew N : objectNews) N.save(); // Сохраняем объекты из списка
                        adapter.addAll(objectNews);         // Добавляем полученные данные в адаптер
                        Snackbar.make(parentView, "Загрузили страницу № "+number_page, Snackbar.LENGTH_LONG).show();   // Отображение номера страницы для теста программы
                    }

                    @Override
                    public void onError(Throwable e) {
                        current_page--;
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
//______________________ Helpers

    private static ArrayList<ObjectNew> getObjectNews(ArrayList<Article> art){
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

}