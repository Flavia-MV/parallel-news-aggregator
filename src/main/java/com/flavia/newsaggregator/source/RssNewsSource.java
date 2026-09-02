package com.flavia.newsaggregator.source;

import com.flavia.newsaggregator.model.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class RssNewsSource implements NewsSource{
    
    private final String name;
    private final URL url;

    public RssNewsSource(String name, URL url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArrayList<Article> fetch() throws IOException {
        ArrayList<Article> articles = new ArrayList<>();

        try {
            SyndFeedInput input = new SyndFeedInput();

            var feed = input.build(new XmlReader(url));

            for (SyndEntry entry:feed.getEntries()) {
                Article article = new Article(
                    entry.getTitle(),
                    entry.getAuthor(),
                    entry.getLink(),
                    entry.getDescription() != null
                        ? entry.getDescription().getValue()
                        : null,
                    entry.getPublishedDate() != null
                        ? entry.getPublishedDate()
                            .toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                        : null
                );
                articles.add(article);
            }
        } catch (Exception e) {
            throw new IOException("Failed to fetch RSS source: " + name, e);
        }
        return articles;
    }
}
