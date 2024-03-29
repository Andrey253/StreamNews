package com.boyko.streamnews;


import android.os.AsyncTask;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements RVAdapter.customButtonListener{

    private View     parentView;
    private RVAdapter adapter;

    private ArrayList<Article> articleList;
    private ArrayList<ObjectNew> objectNews;
    private LinearLayoutManager linearLayoutManager;

    private boolean isLoading=false;

    public final static int TOTAL_PAGE = 5;
    public static int current_page=0;

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
                        && !isLoading
                        && InternetConnection.checkConnection(getApplicationContext()))
                {

                    current_page+=1;
                    loadNextPage(current_page);
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetConnection.checkConnection(getApplicationContext()) && !isLoading){
                    current_page=1;
                    loadNextPage(current_page);
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

    private void loadNextPage(int next_page){
        MyRequest request = new MyRequest();
        request.execute(next_page);
    }

    @Override
    public void onButtonClickListner() {

        if (InternetConnection.checkConnection(getApplicationContext()) && !isLoading){

            if (current_page<TOTAL_PAGE){

                current_page+=1;
                loadNextPage(current_page);
            }
            else {
                Snackbar.make(parentView, R.string.string_is_all_new_downloaded, Snackbar.LENGTH_LONG).show();
            }
        }else {
            Snackbar.make(parentView, R.string.string_internet_connection_not_available, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Асинхронный запрос для получения данных
     */
    class MyRequest extends AsyncTask<Integer, Void,Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            try {

                final int page  = params[0];
                //Creating an object of our api interface
                final ApiService api = Client.getApiService();

                Call<ArticleList> call = api.getMyJSON(Splash.Q, Splash.FROM, Splash.SORT_BY, Splash.API_KEY, page);

                call.enqueue(new Callback<ArticleList>() {
                    @Override
                    public void onResponse(@SuppressWarnings("NullableProblems") Call<ArticleList> call, @SuppressWarnings("NullableProblems") Response<ArticleList> response) {

                        if (response.isSuccessful()) {

                            if (current_page==1){
                                Snackbar.make(parentView, "Очищаем базу перед первым получением данных", Snackbar.LENGTH_LONG).show();
                                ObjectNew.deleteAll(ObjectNew.class);   // Очищаем базу перед первым удачным получением данных
                                adapter.clear();                        // Очищаем адаптер
                            }
                            isLoading=false;
                            articleList = response.body().getArticles();
                            objectNews = getObjectNews(articleList);

                            for (ObjectNew N : objectNews) N.save(); // Сохраняем объекты из списка
                            adapter.addAll(objectNews);         // Добавляем полученные данные в адаптер
                            Snackbar.make(parentView, "Загрузили страницу № "+page, Snackbar.LENGTH_LONG).show();

                        } else {
                            current_page-=1;
                            Snackbar.make(parentView, R.string.string_some_thing_wrong, Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@SuppressWarnings("NullableProblems") Call<ArticleList> call, @SuppressWarnings("NullableProblems") Throwable t) {

                    }
                });

            } catch(Exception ex) {
                System.out.println("my tag Exception catch ");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isLoading = true;
        }

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