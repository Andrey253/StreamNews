package com.boyko.streamnews.api;


import com.boyko.streamnews.model.ArticleList;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
 
    /*
    Retrofit get annotation with our URL
    And our method that will return us the List of Article
    */
    @GET("/v2/everything")
    Observable<ArticleList> getMyJSON(
            @Query("q") String q,
            @Query("from") String from,
            @Query("sortBy") String sortBy,
            @Query("apiKey") String apiKey,
            @Query("page") int pageIndex
    );
}