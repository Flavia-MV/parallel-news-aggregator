import java.nio.file.Paths;
import java.util.ArrayList;

import com.flavia.newsaggregator.model.Article;


public class Main {
    public static void main(String[] args) {
       
        ArrayList<NewsSource> sources = new ArrayList<>();
        NewsSource google = new NewsSource("Google", Paths.get("data/google.json"));
        NewsSource bbc = new NewsSource("BBC", Paths.get("data/bbc.json"));
        NewsSource reuters = new NewsSource("Reuters", Paths.get("data/reuters.json"));
        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);

        ArticleParser parser = new ArticleParser();
        NewsAggregator aggregator = new NewsAggregator(parser);

        long start = System.nanoTime();
        ArrayList<Article> aggregatedArticles = aggregator.aggregate(sources);
        long end = System.nanoTime();
        long durationMs = (end- start) / 1_000_000;
        System.out.println("Aggregation completed in: " + durationMs + " ms");
        System.out.println("Articles aggregated: " + aggregatedArticles.size());

        
    }   
}
