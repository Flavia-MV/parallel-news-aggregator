import java.nio.file.Path;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ArticleParser {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ArrayList<Article> parse(Path relativePath) throws IOException{

        String content = Files.readString(relativePath);

        return objectMapper.readValue(content, new TypeReference<ArrayList<Article>>() {});
    }
}
