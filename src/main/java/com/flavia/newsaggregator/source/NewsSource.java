package com.flavia.newsaggregator.source;

import com.flavia.newsaggregator.model.Article;
import java.io.IOException;
import java.util.ArrayList;

public interface NewsSource {
    String getName();

    ArrayList<Article> fetch() throws IOException;
}