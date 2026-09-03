package com.flavia.newsaggregator.source;

import com.flavia.newsaggregator.model.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.URLConnection;

public class RssNewsSource implements NewsSource{
    
    private final String name;
    private final URL url;
    private final int timeoutMs;
    private static final Logger logger = LoggerFactory.getLogger(RssNewsSource.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 500;

    public RssNewsSource(String name, URL url) {
        this(name, url, 3000);
    }

    public RssNewsSource(String name, URL url, int timeoutMs) {
        this.name = name;
        this.url = url;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArrayList<Article> fetch() throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchOnce();
            } catch (IOException e) {
                lastException = e;
                if (attempt == MAX_ATTEMPTS)
                    break;

                long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));

                logger.warn("Failed to fetch RSS source {} (attempt {}/{}). Retrying in {} ms.", name, attempt,
                    MAX_ATTEMPTS, backoff);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();

                    throw new IOException("RSS fetch interrupted for source: " + name, interruptedException);
                }

            }
        }
        throw new IOException("Failed to fetch RSS source after " + MAX_ATTEMPTS + " attempts: " + name, lastException);
        
    }

    private ArrayList<Article> fetchOnce() throws IOException {
        ArrayList<Article> articles = new ArrayList<>();

        try {
            SyndFeedInput input = new SyndFeedInput();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);

            try (InputStream inputStream = connection.getInputStream()) {
                var feed = input.build(new XmlReader(inputStream));
            
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
            }
        } catch (Exception e) {
            throw new IOException("Failed to fetch RSS source: " + name, e);
        }
        return articles;
    }
}
