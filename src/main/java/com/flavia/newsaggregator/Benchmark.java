package com.flavia.newsaggregator;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.flavia.newsaggregator.aggregator.NewsAggregator;
import com.flavia.newsaggregator.model.Article;
import com.flavia.newsaggregator.parser.ArticleParser;
import com.flavia.newsaggregator.source.NewsSource;

public class Benchmark {
    public static void main(String[] args) throws Exception {
        ArrayList<NewsSource> sources = new ArrayList<>();

        sources.add(new NewsSource("Google", Paths.get("data/google.json")));
        sources.add(new NewsSource("BBC", Paths.get("data/bbc.json")));
        sources.add(new NewsSource("Reuters", Paths.get("data/reuters.json")));

        ArticleParser parser = new ArticleParser() {
            @Override
            public ArrayList<Article> parse(java.nio.file.Path path) throws java.io.IOException {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new ArrayList<>();
            }
        };

        long start = System.nanoTime();

        for (NewsSource source:sources) {
            parser.parse(source.getPath());
        }

        long sequentialTime = System.nanoTime() - start;
        NewsAggregator aggregator = new NewsAggregator(parser);
        start = System.nanoTime();
        aggregator.aggregate(sources);
        long parallelTime = System.nanoTime() - start;

        System.out.println("Sequential: " + sequentialTime / 1_000_000 + " ms");
        System.out.println("Parallel: " + parallelTime / 1_000_000 + " ms");
        System.out.println("Speedup: " + String.format("%.2fx", (double) sequentialTime/parallelTime));

    }
}
