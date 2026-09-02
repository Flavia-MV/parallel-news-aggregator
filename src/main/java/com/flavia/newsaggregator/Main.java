package com.flavia.newsaggregator;
import java.nio.file.Paths;
import  java.net.URL;
import java.util.ArrayList;

import com.flavia.newsaggregator.aggregator.NewsAggregator;
import com.flavia.newsaggregator.model.Article;
import com.flavia.newsaggregator.parser.ArticleParser;
import com.flavia.newsaggregator.source.NewsSource;
import com.flavia.newsaggregator.source.LocalFileSource;
import com.flavia.newsaggregator.source.RssNewsSource;

public class Main {
    public static void main(String[] args) throws Exception{
       
        ArrayList<NewsSource> sources = new ArrayList<>();

        ArticleParser parser = new ArticleParser();
        NewsSource google = new LocalFileSource("Google", Paths.get("data/google.json"), parser);
        NewsSource bbc = new LocalFileSource("BBC", Paths.get("data/bbc.json"), parser);
        NewsSource reuters = new LocalFileSource("Reuters", Paths.get("data/reuters.json"), parser);
        NewsSource rss = new RssNewsSource("Example RSS", new URL("https://feeds.bbci.co.uk/news/rss.xml"));
        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);
        sources.add(rss);

        NewsAggregator aggregator = new NewsAggregator();

        long start = System.nanoTime();
        ArrayList<Article> aggregatedArticles = aggregator.aggregate(sources);
        long end = System.nanoTime();
        long durationMs = (end- start) / 1_000_000;
        System.out.println("Aggregation completed in: " + durationMs + " ms");
        System.out.println("Articles aggregated: " + aggregatedArticles.size());
        for (Article article:aggregatedArticles) {
            System.out.println(article.getTitle() + " | " + article.getAuthor() + " | " + article.getPublishedAt());
        }
        
    }   
}
