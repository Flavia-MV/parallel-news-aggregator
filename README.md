# Parallel News Aggregator

A Java application for aggregating and processing articles from multiple news sources concurrently.

The project focuses on **Java concurrency, modular design, failure handling, automated testing, and performance comparison between sequential and parallel execution**.

## Overview

The application aggregates articles from multiple news sources and combines them into a single collection.

The current implementation supports:

* local JSON files through `LocalFileSource`;
* real RSS feeds through `RssNewsSource`;
* concurrent processing of independent sources;
* failure isolation between sources;
* duplicate removal based on article URL;
* sorting by publication time.

The project is designed around the `NewsSource` interface, allowing new types of news sources to be added without modifying the aggregation logic.

## Architecture

The application is organized around several focused components.

### `Article`

Represents a news article and contains:

* title;
* author;
* URL;
* text;
* publication time.

Articles are identified by their URL. The `equals()` and `hashCode()` methods are based on the URL, allowing duplicate articles to be removed using a `Set`.

### `NewsSource`

`NewsSource` is the main abstraction used by the aggregator.

```java
public interface NewsSource {

    String getName();

    ArrayList<Article> fetch() throws IOException;
}
```

The aggregator depends on this interface rather than on a specific source implementation.

Currently, two implementations are available:

```text
                 NewsSource
                     │
          ┌──────────┴──────────┐
          │                     │
 LocalFileSource          RssNewsSource
          │                     │
      JSON files            RSS feed
```

This makes it possible to introduce another source, such as an HTTP API, without changing `NewsAggregator`.

For example, a future source could simply implement:

```java
public class ApiNewsSource implements NewsSource {

    @Override
    public String getName() {
        return "News API";
    }

    @Override
    public ArrayList<Article> fetch() throws IOException {
        // Fetch and convert API data into Article objects
    }
}
```

The `NewsAggregator` can then process the new source through the same `NewsSource` interface.

### `LocalFileSource`

Provides articles from local JSON files.

It uses `ArticleParser` to read and deserialize the file contents.

Local sources are useful for:

* deterministic tests;
* offline development;
* reproducible demonstrations.

### `RssNewsSource`

Fetches articles from a real RSS feed using the Rome RSS/Atom library.

The RSS entries are converted into the common `Article` model, allowing RSS sources and local sources to be processed by the same aggregation pipeline.

### `ArticleParser`

Responsible for reading JSON files and deserializing their contents into `Article` objects.

Jackson is used for JSON processing, with `JavaTimeModule` providing support for `LocalDateTime`.

### `NewsAggregator`

Coordinates the aggregation process.

For every configured source, a separate task is submitted to a fixed-size thread pool.

`ExecutorCompletionService` is used to retrieve completed tasks as they become available instead of waiting for sources in submission order.

After processing the sources, the aggregator:

1. collects successful results;
2. handles failures from individual sources;
3. removes duplicate articles;
4. sorts the resulting articles by publication time.

## Concurrent Processing

The news sources are independent of each other, so their processing can be performed concurrently.

The application currently uses a fixed-size thread pool:

```java
ExecutorService executor =
        Executors.newFixedThreadPool(Math.min(3, sources.size()));
```

Each source is processed by a `Callable<ProcessingResult>`.

`ExecutorCompletionService` allows completed tasks to be processed as soon as they finish.

This is useful when sources have different processing times because a fast source does not have to wait for a slower source that was submitted earlier.

### Processing Flow

```text
                    ┌── LocalFileSource ──┐
                    │                     │
News Sources ───────┼── LocalFileSource ──┼──> Concurrent Processing
                    │                     │
                    └── RssNewsSource ────┘
                                              │
                                              ▼
                                        Deduplication
                                              │
                                              ▼
                                           Sorting
                                              │
                                              ▼
                                      Final article list
```

## Failure Handling

A failure in one news source does not cause the entire aggregation process to fail.

For example:

```text
Google     → successful
BBC        → failed
Reuters    → successful
```

The successfully retrieved articles are still returned.

`IOException` raised while processing an individual source is captured and logged without preventing the remaining sources from being processed.

This behavior is covered by automated tests using Mockito to simulate source failures.

## Deduplication

Duplicate articles are identified using their URL.

For example, if two sources contain:

```text
https://example.com/article
```

they are considered the same article even if their titles, authors, or text differ.

The aggregator uses a `LinkedHashSet<Article>` for deduplication before converting the results back into a list.

The equality and hash code contract is tested separately in `ArticleTest`.

## Sorting

After aggregation and deduplication, articles are sorted by `publishedAt` in descending order, so the most recently published articles appear first.

Articles without a publication time are explicitly handled and placed after articles with a valid publication time.

```java
aggregatedArticles.sort(
        Comparator.comparing(
                Article::getPublishedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        )
);
```

This behavior is covered by automated tests.

## Testing

The project uses **JUnit 5** for automated testing and **Mockito** for mocking dependencies.

The current test suite contains **13 tests** covering:

* JSON parsing;
* article field deserialization;
* article equality and hash code behavior;
* duplicate article removal;
* sorting by publication time;
* handling failed news sources;
* handling articles without a publication time;
* aggregation with no sources;
* sequential versus parallel processing;
* RSS feed parsing;
* RSS source connection failures.

The RSS tests use a local HTTP server instead of relying on an external website, making the tests deterministic and independent of internet availability.

Run the complete test suite with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

All tests currently pass.

## Performance Comparison

A benchmark is included to compare sequential and parallel processing.

The benchmark uses a simulated one-second delay for each source to represent an I/O-bound workload.

Example result:

```text
Sequential: 3007 ms
Parallel:   1006 ms
Speedup:     2.99x
```

With three independent sources and three worker threads, the parallel implementation completes the simulated workload in approximately the time required by the slowest individual task.

The benchmark uses artificial delays and therefore does not represent production performance. Its purpose is to demonstrate the effect of concurrent execution under a controlled workload.

## Project Structure

```text
parallel-news-aggregator/
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── data/
│   ├── article.json
│   ├── bbc.json
│   ├── google.json
│   └── reuters.json
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── flavia/
│   │   │           └── newsaggregator/
│   │   │               ├── aggregator/
│   │   │               │   └── NewsAggregator.java
│   │   │               ├── model/
│   │   │               │   └── Article.java
│   │   │               ├── parser/
│   │   │               │   └── ArticleParser.java
│   │   │               └── source/
│   │   │                   ├── LocalFileSource.java
│   │   │                   ├── NewsSource.java
│   │   │                   └── RssNewsSource.java
│   │   │
│   │   │   ├── Benchmark.java
│   │   │   └── Main.java
│   │   │
│   │   └── resources/
│   │       └── logback.xml
│   │
│   └── test/
│       └── java/
│           ├── ArticleParserTest.java
│           ├── ArticleTest.java
│           ├── NewsAggregatorTest.java
│           └── RssNewsSourceTest.java
│
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Technologies

* **Java 17**
* **Maven**
* **Jackson**
* **Rome RSS/Atom**
* **JUnit 5**
* **Mockito**
* **SLF4J**
* **Logback**
* **Java Concurrency API**
* **GitHub Actions**
* **JaCoCo**

## Running the Project

### Requirements

* Java 17
* Git

Maven does not need to be installed separately because the project includes the Maven Wrapper.

### Run Tests

On Windows:

```powershell
.\mvnw.cmd test
```

On Linux/macOS:

```bash
./mvnw test
```

### Run the Application

Run `Main.java` from the IDE.

The application currently combines local JSON fixtures with a real RSS feed and processes them through the same `NewsAggregator` pipeline.

Example output:

```text
Aggregation completed in: 1032 ms
Articles aggregated: 35
```

The application also prints the aggregated articles ordered by publication time.

### Run the Benchmark

Run `Benchmark.java` to compare sequential and parallel execution using the simulated I/O workload.

## Continuous Integration

The project uses GitHub Actions to automatically run the test suite when changes are pushed to the `main` branch or when a pull request targets `main`.

The CI workflow uses Java 17 and Maven to verify that the project builds successfully and that all tests pass.

## Future Improvements

Possible extensions include:

* retry and exponential backoff for temporary RSS/network failures;
* multiple real RSS sources;
* configurable per-source timeouts;
* configurable concurrency limits;
* external application configuration;
* improved benchmark methodology using real I/O workloads;
* additional integration tests;
* Docker support;
* JaCoCo coverage reporting in CI;
* CLI configuration for selecting news sources.