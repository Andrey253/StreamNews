package com.boyko.streamnews.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ArticleList {

        @SerializedName("articles")
        @Expose
        private ArrayList<Article> articles = new ArrayList<>();

        /**
         * @return The articles
         */
        public ArrayList<Article> getArticles() {
            return articles;
        }
}
