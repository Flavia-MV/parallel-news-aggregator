import java.util.Objects;
import java.time.LocalDateTime;


public class Article {
    private String title;
    private String author;
    private String url;
    private String text;
    private LocalDateTime publishedAt;

    public Article() {
    }

    public Article(String title, String author, String url, String text, LocalDateTime publishedAt) {
        this.title = title;
        this.author = author;
        this.url = url;
        this.text = text;
        this.publishedAt = publishedAt;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getUrl() {
        return url;
    }
    public String getText() {
        return text;
    }
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Article)) {
            return false;
        }
        Article article = (Article) o;
        return Objects.equals(url, article.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}

