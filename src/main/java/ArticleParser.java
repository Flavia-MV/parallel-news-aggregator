import java.nio.file.Path;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;

public class ArticleParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ArrayList<Article> parse(Path relativePath) {
        ArrayList<Article> articles = null;

        try {
            String content = Files.readString(relativePath);
            articles = objectMapper.readValue(content, new TypeReference<ArrayList<Article>>() {});

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return articles;
    }
}
