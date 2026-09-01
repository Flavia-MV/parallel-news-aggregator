package com.flavia.newsaggregator.aggregator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import com.flavia.newsaggregator.model.Article;
import com.flavia.newsaggregator.parser.ArticleParser;
import com.flavia.newsaggregator.source.NewsSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsAggregator {
    private static final Logger logger = LoggerFactory.getLogger(NewsAggregator.class);
    private final ArticleParser parser;

    public NewsAggregator(ArticleParser parser) {
        this.parser = parser;
    }


    public ArrayList<Article> aggregate(ArrayList<NewsSource> sources) {
        ArrayList<Article> aggregatedArticles = new ArrayList<>();
        if (sources.isEmpty()) {
            return aggregatedArticles;
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(3, sources.size()));
        CompletionService<ProcessingResult> completionService = new ExecutorCompletionService<>(executor);
        Set<Article> uniqueArticles = new LinkedHashSet<>();

        try {
            for (NewsSource source:sources) {
                Callable<ProcessingResult> task = () -> {
                    try {
                         ArrayList<Article> articles = parser.parse(source.getPath());
                        return new ProcessingResult(source, articles, null);
                    } catch (IOException e) {
                        return new ProcessingResult(source, null, e);
                    }
                };
                completionService.submit(task);
            }

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            for (int i = 0; i < sources.size(); i++) {

                long remainingTime = deadline - System.nanoTime();
                if (remainingTime <= 0) {
                    logger.warn("Aggregation timed out before all sources completed.");
                    break;
                }

                try {
                    Future<ProcessingResult> future = completionService.poll(remainingTime, TimeUnit.NANOSECONDS);
                    if (future == null) {
                        logger.warn("Aggregation timed out before all sources completed.");
                        break;
                    }
                    
                    ProcessingResult result = future.get();
                    if (result.error != null) {
                        logger.error("Failed to process source: {}", result.source.getName(), result.error);
                    } else {
                        uniqueArticles.addAll(result.articles);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Aggregation interrupted", e);
                    break;
                } catch (ExecutionException e) {
                    logger.error("Unexpected failure while processing source", e.getCause());
                }
            }
        } finally {
            executor.shutdownNow();
        }
        

        aggregatedArticles.addAll(uniqueArticles);
        aggregatedArticles.sort(Comparator.comparing(Article::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        return aggregatedArticles;
    }

    private static class ProcessingResult {
        private final NewsSource source;
        private final ArrayList<Article> articles;
        private final Exception error;

        ProcessingResult(NewsSource source, ArrayList<Article> articles, Exception error) {
            this.source = source;
            this.articles = articles;
            this.error =error;
        }
    }
}
