import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.io.IOException;

public class NewsAggregatorTest {
    
    @Test
    void shouldRemoveDuplicateArticles() throws Exception {
        Article article1 = new Article("AI News", "John", "https://example.com/article", "First text", null);
        Article article2 = new Article("AI News Updated", "Emma", "https://example.com/article", "Different text", null);

        ArrayList<Article> googleArticles = new ArrayList<>();
        googleArticles.add(article1);

        ArrayList<Article> bbcArticles = new ArrayList<>();
        bbcArticles.add(article2);

        NewsSource google = new NewsSource("Google", Paths.get("data/google.json"));
        NewsSource bbc = new NewsSource("BBC", Paths.get("data/bbc.json"));

        ArticleParser parser = mock(ArticleParser.class);
        when(parser.parse(google.getPath())).thenReturn(googleArticles);
        when(parser.parse(bbc.getPath())).thenReturn(bbcArticles);

        NewsAggregator aggregator = new NewsAggregator(parser);
        ArrayList<NewsSource> sources = new ArrayList<>();
        sources.add(google);
        sources.add(bbc);

        ArrayList<Article> result = aggregator.aggregate(sources);
        assertEquals(1, result.size());
    }

    @Test
    void shouldSortArticlesByPublishedAt() throws Exception {
        Article article1 = new Article("Morning News", "John", "https://example.com/1", "Text 1", LocalDateTime.of(2026, 8, 31, 10, 0));
        Article article2 = new Article("Afternoon News", "Emma", "https://example.com/2", "Text 2", LocalDateTime.of(2026, 8, 31, 14, 0));
        Article article3 = new Article("Noon News", "Alex", "https://example.com/3", "Text 3", LocalDateTime.of(2026, 8, 31, 12, 0));

        ArrayList<Article> articles = new ArrayList<>();
        articles.add(article1);
        articles.add(article2);
        articles.add(article3);

        NewsSource google = new NewsSource("Google", Paths.get("data/google.json"));
        ArticleParser parser = mock(ArticleParser.class);
        when(parser.parse(google.getPath())).thenReturn(articles);

        NewsAggregator aggregator = new NewsAggregator(parser);
        ArrayList<NewsSource> sources = new ArrayList<>();
        sources.add(google);
        ArrayList<Article> result = aggregator.aggregate(sources);
     
        assertEquals(article2, result.get(0));
        assertEquals(article3, result.get(1));
        assertEquals(article1, result.get(2));
    }

    @Test
    void shouldContinueWhenOneSourceFails() throws Exception {
        Article article1 = new Article("Google News", "John", "https://example.com/google", "Google text", LocalDateTime.of(2026, 8, 31, 14, 0));
        Article article2 = new Article("Reuters News", "Emma", "https://example.com/reuters", "Reuters text", LocalDateTime.of(2026, 8, 31, 12, 0));
        
        ArrayList<Article> googleArticles = new ArrayList<>();
        googleArticles.add(article1);

        ArrayList<Article> reutersArticles = new ArrayList<>();
        reutersArticles.add(article2);
    
        NewsSource google = new NewsSource("Google", Paths.get("data/google.json"));
        NewsSource bbc = new NewsSource("BBC", Paths.get("data/bbc-invalid.json"));
        NewsSource reuters = new NewsSource("Reuters", Paths.get("data/reuters.json"));

        ArticleParser parser = mock(ArticleParser.class);
        when(parser.parse(google.getPath())).thenReturn(googleArticles);
        when(parser.parse(reuters.getPath())).thenReturn(reutersArticles);
        when(parser.parse(bbc.getPath())).thenThrow(new IOException("File not found"));

        NewsAggregator aggregator = new NewsAggregator(parser);
        ArrayList<NewsSource> sources = new ArrayList<>();
        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);

        ArrayList<Article> result = aggregator.aggregate(sources);
        assertEquals(2, result.size());
        assertEquals(article1, result.get(0));
        assertEquals(article2, result.get(1));   
    }
   
    @Test 
    void shouldReturnEmptyListWhenNoSourcesAreProvided() throws Exception {
        ArrayList<NewsSource> sources = new ArrayList<>();
        ArticleParser parser = mock(ArticleParser.class);
        NewsAggregator aggregator = new NewsAggregator(parser);
        ArrayList<Article> result = aggregator.aggregate(sources);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void compareSequentialAndParallel() throws Exception {
        ArrayList<NewsSource> sources = new ArrayList<>();

        NewsSource google = new NewsSource("Google", Paths.get("data/google.json"));
        NewsSource bbc = new NewsSource("BBC", Paths.get("data/bbc.json"));
        NewsSource reuters = new NewsSource("Reuters", Paths.get("data/reuters.json"));

        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);

        ArticleParser parser = mock(ArticleParser.class);
        when(parser.parse(google.getPath())).thenAnswer(invocation -> {
            Thread.sleep(1000);
            return new ArrayList<>();
        });
        when(parser.parse(bbc.getPath())).thenAnswer(invocation -> {
            Thread.sleep(1000);
            return new ArrayList<>();
        });
        when(parser.parse(reuters.getPath())).thenAnswer(invocation -> {
            Thread.sleep(1000);
            return new ArrayList<>();
        });

        long start = System.nanoTime();
        for (NewsSource source:sources) {
            parser.parse(source.getPath());
        }
        long sequentialTime = System.nanoTime() - start;
        System.out.println("Sequential: " + sequentialTime / 1_000_000 + " ms");
        assertEquals(3, sources.size());

        NewsAggregator aggregator = new NewsAggregator(parser);
        start = System.nanoTime();
        aggregator.aggregate(sources);
        long parallelTime = System.nanoTime() - start;
        System.out.println("Parallel: " + parallelTime / 1_000_000 + " ms");
    }
}
