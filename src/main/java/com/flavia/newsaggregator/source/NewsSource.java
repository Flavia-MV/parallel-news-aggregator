package com.flavia.newsaggregator.source;
import java.nio.file.Path;

public class NewsSource {
    private final String name;
    private final Path path;

    public NewsSource(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public Path getPath() {
        return this.path;
    }

}
