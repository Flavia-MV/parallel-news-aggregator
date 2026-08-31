import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class ArticleParserTest {
    @Test
    void shouldParseGoogleArticles() throws Exception {
        Path path = Paths.get("data/google.json");
        ArticleParser parser = new ArticleParser();

        ArrayList<Article> articles = parser.parse(path);

        assertNotNull(articles);
        assertEquals(2, articles.size());
    }

    @Test
    void shouldParseArticleDataCorrectly() throws Exception {
        Path path = Paths.get("data/google.json");
        ArticleParser parser = new ArticleParser();

        ArrayList<Article> articles = parser.parse(path);
        Article firsArticle = articles.get(0);

        assertEquals("Google Announces New AI Technology", firsArticle.getTitle());
        assertEquals("John Smith", firsArticle.getAuthor());
        assertEquals("https://news.example.com/google-ai", firsArticle.getUrl());
    }
}
