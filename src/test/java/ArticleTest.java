import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import com.flavia.newsaggregator.model.Article;

public class ArticleTest {
    @Test
    void articleWithSameUrlShouldBeEqual() {
        Article article1 = new Article("AI News", "John", "https://example.com/article", "First text", LocalDateTime.of(2026, 8,31,10,0));
        Article article2 = new Article("Update AI News", "Emma", "https://example.com/article", "Different text", LocalDateTime.of(2026, 8,31,12,0));
        assertEquals(article1, article2);
        assertEquals(article1.hashCode(), article2.hashCode());
    }

    @Test 
    void articlesWithDifferentUrlsShouldNotBeEqual() {
        Article article1 = new Article("AI News", "John", "https://example.com/article1", "Text", null);
        Article article2 = new Article("Update AI News", "Emma", "https://example.com/article2", "Text", null);
       assertNotEquals(article1, article2);
    }
}
