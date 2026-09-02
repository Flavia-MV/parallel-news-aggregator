package com.flavia.newsaggregator;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.flavia.newsaggregator.aggregator.NewsAggregator;
import com.flavia.newsaggregator.model.Article;
import com.flavia.newsaggregator.parser.ArticleParser;
import com.flavia.newsaggregator.source.NewsSource;
import com.flavia.newsaggregator.source.LocalFileSource;


public class Main {
    public static void main(String[] args) {
       
        ArrayList<NewsSource> sources = new ArrayList<>();

        ArticleParser parser = new ArticleParser();
        NewsSource google = new LocalFileSource("Google", Paths.get("data/google.json"), parser);
        NewsSource bbc = new LocalFileSource("BBC", Paths.get("data/bbc.json"), parser);
        NewsSource reuters = new LocalFileSource("Reuters", Paths.get("data/reuters.json"), parser);
        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);

        NewsAggregator aggregator = new NewsAggregator();

        long start = System.nanoTime();
        ArrayList<Article> aggregatedArticles = aggregator.aggregate(sources);
        long end = System.nanoTime();
        long durationMs = (end- start) / 1_000_000;
        System.out.println("Aggregation completed in: " + durationMs + " ms");
        System.out.println("Articles aggregated: " + aggregatedArticles.size());

        
    }   
}
