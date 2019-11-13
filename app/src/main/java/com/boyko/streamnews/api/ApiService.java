package com.boyko.streamnews.api;


import com.boyko.streamnews.model.ArticleList;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
 
    @GET("/v2/everything")
    Observable<ArticleList> getMyJSON(
            @Query("q") String q,
            @Query("from") String from,
            @Query("sortBy") String sortBy,
            @Query("apiKey") String apiKey,
            @Query("page") int pageIndex
    );
}