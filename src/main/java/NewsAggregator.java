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

public class NewsAggregator {
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
                    } catch (Exception e) {
                        return new ProcessingResult(source, null, e);
                    }
                };
                completionService.submit(task);
            }

            for (int i = 0; i < sources.size(); i++) {

                try {
                    Future<ProcessingResult> future = completionService.take();
                    ProcessingResult  result = future.get();

                    if (result.error != null) {
                        System.out.println("Failed to process source: " + result.source.getName());
                        System.out.println("Reason: " + result.error);
                    } else {
                        uniqueArticles.addAll(result.articles);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ExecutionException e) {
                    System.out.println("Failed to process source");
                    System.out.println("Reason: " + e.getCause());
                }
            }
        } finally {
            executor.shutdown();
        }
        

        aggregatedArticles.addAll(uniqueArticles);
        aggregatedArticles.sort(Comparator.comparing((Article article) -> article.getPublishedAt()).reversed());

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
