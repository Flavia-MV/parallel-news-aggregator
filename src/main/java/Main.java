import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        Article article1 = new Article("Google", "John", "...", "News");
        Article article2 = new Article("Google", "Pam", "...", "News");
        Article article3 = new Article("Google", "Ann", "...", "News");

        ArrayList<Article> articles = new ArrayList<>();
        articles.add(article1);  
        articles.add(article2);   
        articles.add(article3);  

        for (Article article:articles)
            System.out.println(article.getTitle());

        Path relativePath = Paths.get("data/article.json");

        String content = "";
        ObjectMapper objectMapper = new ObjectMapper();
        Article article = null;

        try {
            content = Files.readString(relativePath);
            article = objectMapper.readValue(content, Article.class);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(content);
        System.out.println(article.getTitle());
        System.out.println(article.getAuthor());

    }   
}
