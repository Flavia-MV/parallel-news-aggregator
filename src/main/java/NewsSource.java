import java.nio.file.Path;

public class NewsSource {
    private String name;
    private Path path;

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
