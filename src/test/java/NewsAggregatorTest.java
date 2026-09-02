import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import com.flavia.newsaggregator.aggregator.NewsAggregator;
import com.flavia.newsaggregator.model.Article;
import com.flavia.newsaggregator.parser.ArticleParser;
import com.flavia.newsaggregator.source.NewsSource;
import com.flavia.newsaggregator.source.LocalFileSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class NewsAggregatorTest {
    
    @Test
    void shouldRemoveDuplicateArticles() throws Exception {
        Article article1 = new Article("AI News", "John", "https://example.com/article", "First text", null);
        Article article2 = new Article("AI News Updated", "Emma", "https://example.com/article", "Different text", null);

        ArrayList<Article> googleArticles = new ArrayList<>();
        googleArticles.add(article1);

        ArrayList<Article> bbcArticles = new ArrayList<>();
        bbcArticles.add(article2);

        ArticleParser parser = mock(ArticleParser.class);
        NewsSource google = new LocalFileSource("Google", Paths.get("data/google.json"), parser);
        NewsSource bbc = new LocalFileSource("BBC", Paths.get("data/bbc.json"), parser);

        when(parser.parse(Paths.get("data/google.json"))).thenReturn(googleArticles);
        when(parser.parse(Paths.get("data/bbc.json"))).thenReturn(bbcArticles);

        NewsAggregator aggregator = new NewsAggregator();
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

        ArticleParser parser = mock(ArticleParser.class);
        NewsSource google = new LocalFileSource("Google", Paths.get("data/google.json"), parser);
        when(parser.parse(Paths.get("data/google.json"))).thenReturn(articles);

        NewsAggregator aggregator = new NewsAggregator();
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
    
        ArticleParser parser = mock(ArticleParser.class);
        NewsSource google = new LocalFileSource("Google", Paths.get("data/google.json"), parser);
        NewsSource bbc = new LocalFileSource("BBC", Paths.get("data/bbc-invalid.json"), parser);
        NewsSource reuters = new LocalFileSource("Reuters", Paths.get("data/reuters.json"), parser);

        when(parser.parse(Paths.get("data/google.json"))).thenReturn(googleArticles);
        when(parser.parse(Paths.get("data/reuters.json"))).thenReturn(reutersArticles);
        when(parser.parse(Paths.get("data/bbc-invalid.json"))).thenThrow(new IOException("File not found"));

        NewsAggregator aggregator = new NewsAggregator();
        ArrayList<NewsSource> sources = new ArrayList<>();
        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);

        ArrayList<Article> result = aggregator.aggregate(sources);
        assertEquals(2, result.size());
        assertTrue(result.contains(article1));
        assertTrue(result.contains(article2));  
    }
   
    @Test 
    void shouldReturnEmptyListWhenNoSourcesAreProvided() throws Exception {
        ArrayList<NewsSource> sources = new ArrayList<>();
        ArticleParser parser = mock(ArticleParser.class);
        NewsAggregator aggregator = new NewsAggregator();
        ArrayList<Article> result = aggregator.aggregate(sources);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void compareSequentialAndParallel() throws Exception {
        ArrayList<NewsSource> sources = new ArrayList<>();

        ArticleParser parser = mock(ArticleParser.class);
        NewsSource google = new LocalFileSource("Google", Paths.get("data/google.json"), parser);
        NewsSource bbc = new LocalFileSource("BBC", Paths.get("data/bbc.json"), parser);
        NewsSource reuters = new LocalFileSource("Reuters", Paths.get("data/reuters.json"), parser);

        sources.add(google);
        sources.add(bbc);
        sources.add(reuters);

        when(parser.parse(Paths.get("data/google.json"))).thenAnswer(invocation -> {
            Thread.sleep(1000);
            return new ArrayList<>();
        });
        when(parser.parse(Paths.get("data/bbc.json"))).thenAnswer(invocation -> {
            Thread.sleep(1000);
            return new ArrayList<>();
        });
        when(parser.parse(Paths.get("data/reuters.json"))).thenAnswer(invocation -> {
            Thread.sleep(1000);
            return new ArrayList<>();
        });

        long start = System.nanoTime();
        for (NewsSource source:sources) {
            source.fetch();
        }
        long sequentialTime = System.nanoTime() - start;
        System.out.println("Sequential: " + sequentialTime / 1_000_000 + " ms");
        assertEquals(3, sources.size());

        NewsAggregator aggregator = new NewsAggregator();
        start = System.nanoTime();
        aggregator.aggregate(sources);
        long parallelTime = System.nanoTime() - start;
        System.out.println("Parallel: " + parallelTime / 1_000_000 + " ms");
    }

    @Test
    void ShouldPlaceArticleWithoutPublishedAtLast() throws Exception {
        Article articleWithDate = new Article("News with date", "John", "https://example.com/dated", "Text", LocalDateTime.of(2026, 8, 31, 14, 0));
        Article articleWithoutDate = new Article("News without date", "Emma", "https://example.com/undated", "Texr", null);

        ArrayList<Article> articles = new ArrayList<>();
        articles.add(articleWithoutDate);
        articles.add(articleWithDate);

        ArticleParser parser = mock(ArticleParser.class);
        NewsSource source = new LocalFileSource("Test Source", Paths.get("data/test.json"), parser);

        when(parser.parse(Paths.get("data/test.json"))).thenReturn(articles);

        NewsAggregator aggregator = new NewsAggregator();

        ArrayList<NewsSource> sources = new ArrayList<>();
        sources.add(source);

        ArrayList<Article> result = new ArrayList<>(aggregator.aggregate(sources));

        assertEquals(articleWithDate, result.get(0));
        assertEquals(articleWithoutDate, result.get(1));

    }

    @Test
    void shouldTimeoutWhenSourceTakesTooLong() throws Exception {
        ArticleParser parser = mock(ArticleParser.class);
        NewsSource source = new LocalFileSource("Slow Source", Paths.get("data/slow.json"), parser);


        when(parser.parse(Paths.get("data.slow.json"))).thenAnswer(invocation -> {
            Thread.sleep(10_000);
            return new ArrayList<>();
        });

        NewsAggregator aggregator = new NewsAggregator();

        ArrayList<NewsSource> sources = new ArrayList<>();
        sources.add(source);

        long start = System.nanoTime();

        ArrayList<Article> result = aggregator.aggregate(sources);

        long duration = System.nanoTime() - start;

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertTrue(duration < 7_000_000_000L, "Aggregation should stop after the timeout");
    }
}
