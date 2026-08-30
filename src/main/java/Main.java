import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
       
        ArrayList<NewsSource> sources = new ArrayList<>();
        NewsSource google = new NewsSource("Google", Paths.get("data/google.json"));
        NewsSource bbc = new NewsSource("BBC", Paths.get("data/bbc.json"));
        NewsSource reuters = new NewsSource("Reuters", Paths.get("data/reuters.json"));
        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);

        NewsAggregator aggregator = new NewsAggregator();

        long start = System.nanoTime();
        ArrayList<Article> aggregatedArticles = aggregator.aggregate(sources);
        long end = System.nanoTime();
        long duration = end- start;
        System.out.println(duration);
        System.out.println(aggregatedArticles.size());
    }   
}
