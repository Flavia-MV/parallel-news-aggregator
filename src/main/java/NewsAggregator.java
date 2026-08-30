import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.HashMap;
import java.util.Map;

public class NewsAggregator {
    
    public ArrayList<Article> aggregate(ArrayList<NewsSource> sources) {
        ArrayList<Article> aggregatedArticles = new ArrayList<>();
        ArticleParser parser = new ArticleParser();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(3, sources.size()));
        Map<NewsSource, Future<ArrayList<Article>>> futures = new HashMap<>();

        for (NewsSource source:sources) {
            Callable<ArrayList<Article>> task = () -> {
                return parser.parse(source.getPath());
            };
            Future<ArrayList<Article>> future = executor.submit(task);
            futures.put(source, future);
        }

        for (Map.Entry<NewsSource, Future<ArrayList<Article>>> entry:futures.entrySet()) {
            NewsSource source = entry.getKey();
            Future<ArrayList<Article>> future = entry.getValue();

            try {
                ArrayList<Article> sourceArticles = future.get();
                aggregatedArticles.addAll(sourceArticles);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                System.out.println("Failed to process source: " + source.getName());
                System.out.println("Reason: " + e.getCause());
            }
        }

        executor.shutdown();
        return aggregatedArticles;
    }
}
