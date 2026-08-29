import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Path relativePath = Paths.get("data/article.json");

        ArticleParser parser = new ArticleParser();
        ArrayList<Article> articles = parser.parse(relativePath);
       
        for (Article article:articles) {
            System.out.println(article.getTitle());
            System.out.println(article.getAuthor());
        }
       

    }   
}
