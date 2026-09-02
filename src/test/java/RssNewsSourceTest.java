import static org.junit.jupiter.api.Assertions.assertEquals;
import com.flavia.newsaggregator.model.Article;
import com.flavia.newsaggregator.source.RssNewsSource;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;


class RssNewsSourceTest {
    
    @Test
    void shouldParseRssFeedIntoArticles() throws Exception {

        String rss="""
                 <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                    <channel>
                        <title>Test News</title>

                        <item>
                            <title>AI Changes Software Development</title>
                            <author>John Doe</author>
                            <link>https://example.com/ai</link>
                            <description>AI is changing software development.</description>
                            <pubDate>Wed, 02 Sep 2026 08:00:00 GMT</pubDate>
                        </item>

                        <item>
                            <title>Java 25 Released</title>
                            <author>Jane Doe</author>
                            <link>https://example.com/java</link>
                            <description>Java 25 introduces new features.</description>
                            <pubDate>Wed, 02 Sep 2026 09:00:00 GMT</pubDate>
                        </item>
                    </channel>
                </rss>
                """;
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            byte[] response = rss.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "application/rss+xml");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            URL url = new URL("http://localhost:" + port + "/");
            RssNewsSource source = new RssNewsSource("Test RSS", url);

            ArrayList<Article> articles = source.fetch();

            assertEquals(2, articles.size());

            assertEquals("AI Changes Software Development", articles.get(0).getTitle());

            assertEquals("John Doe", articles.get(0).getAuthor());

            assertEquals("https://example.com/ai", articles.get(0).getUrl());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldThrowIOExceptionWhenRssSourceCannotBeReached() throws Exception {
        URL url = new URL("https://localhost:1/");

        RssNewsSource source = new RssNewsSource("Unavailable RSS", url);

        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class, source::fetch);
    }
}
