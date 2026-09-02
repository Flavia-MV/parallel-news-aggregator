package com.flavia.newsaggregator.source;

import com.flavia.newsaggregator.model.Article;
import com.flavia.newsaggregator.parser.ArticleParser;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class LocalFileSource implements NewsSource{
    private final String name;
    private final Path path;
    private final ArticleParser parser;

    public LocalFileSource(String name, Path path, ArticleParser parser) {
        this.name = name;
        this.parser = parser;
        this.path = path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArrayList<Article> fetch() throws IOException {
        return parser.parse(path);
    }

}
