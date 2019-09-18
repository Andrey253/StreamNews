package com.boyko.streamnews.api;

import com.boyko.streamnews.model.ArticleList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
 
    /*
    Retrofit get annotation with our URL
    And our method that will return us the List of Article
    */
    @GET("/v2/everything")
    Call<ArticleList> getMyJSON(
            @Query("q") String q,
            @Query("from") String from,
            @Query("sortBy") String sortBy,
            @Query("apiKey") String apiKey,
            @Query("page") int pageIndex
    );
}