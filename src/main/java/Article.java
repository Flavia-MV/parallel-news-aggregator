public class Article {
    private String title;
    private String author;
    private String url;
    private String text;

    public Article() {
    }

    public Article(String title, String author, String url, String text) {
        this.title = title;
        this.author = author;
        this.url = url;
        this.text = text;
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
}

