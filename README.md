# Parallel News Aggregator

A Java application for aggregating and processing articles from multiple news sources concurrently.

The project focuses on Java concurrency, modular design, exception handling, automated testing, and performance comparison between sequential and parallel execution.

## Overview

The application reads articles from multiple JSON-based news sources and combines them into a single collection.

During aggregation, the application:

* processes independent news sources concurrently;
* handles failures from individual sources without interrupting the entire aggregation process;
* removes duplicate articles based on their URL;
* orders articles by publication time.

The current implementation uses local JSON files as data sources, allowing the project to run without external services or API credentials.

## Architecture

The application is organized around several focused components.

### `Article`

Represents a news article and contains its title, author, URL, text, and publication time.

Articles are identified by their URL. The `equals()` and `hashCode()` methods are implemented based on the URL, allowing duplicate articles to be removed using a `Set`.

### `NewsSource`

Represents a news source through its name and the path of its corresponding data file.

### `ArticleParser`

Responsible for reading JSON files and deserializing their contents into `Article` objects.

Jackson is used for JSON processing, with `JavaTimeModule` providing support for `LocalDateTime`.

### `NewsAggregator`

Coordinates the aggregation process.

For every configured source, a separate task is submitted to a fixed-size thread pool. The results are retrieved using `ExecutorCompletionService`.

After processing the sources, the aggregator:

1. collects successful results;
2. handles sources that could not be processed;
3. removes duplicate articles;
4. sorts the resulting articles by publication time.

## Concurrent Processing

The news sources are independent of each other, so their processing can be performed concurrently.

The application uses a fixed-size thread pool:

```java
ExecutorService executor =
        Executors.newFixedThreadPool(Math.min(3, sources.size()));
```

Each source is processed by a `Callable<ProcessingResult>`.

`ExecutorCompletionService` is used to retrieve completed tasks as they become available instead of waiting for sources in submission order.

This is useful when different sources require different amounts of processing time, since a completed source can be handled immediately without waiting for earlier tasks to finish.

### Processing Flow

```text
                 ┌── Source 1 ──┐
                 │              │
News Sources ────┼── Source 2 ──┼──> Aggregation
                 │              │
                 └── Source 3 ──┘
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

The articles successfully retrieved from Google and Reuters are still returned.

`IOException` raised during parsing is captured for the individual source, allowing the remaining sources to continue processing.

This behavior is tested using Mockito to simulate a failed parser operation.

## Deduplication

Duplicate articles are identified using their URL.

For example, if two sources contain articles with:

```text
https://example.com/article
```

they are considered the same article, even if their titles, authors, or text differ.

The aggregator uses a `LinkedHashSet<Article>` for deduplication while preserving insertion order before the final sorting step.

The equality contract is tested separately in `ArticleTest`.

## Sorting

After aggregation and deduplication, articles are sorted by `publishedAt` in descending order, so the most recently published articles appear first.

Articles without a publication time are handled explicitly and placed after articles with a valid publication time.

```java
aggregatedArticles.sort(
        Comparator.comparing(
                Article::getPublishedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        )
);
```

This behavior is covered by an automated test.

## Testing

The project uses **JUnit 5** for automated testing and **Mockito** for mocking dependencies.

The current test suite contains **10 tests** covering:

* JSON parsing;
* article field deserialization;
* article equality and hash code behavior;
* duplicate article removal;
* sorting by publication time;
* handling a failed news source;
* handling articles without a publication time;
* aggregation with no sources;
* comparison of sequential and parallel processing.

`ArticleParser` is injected into `NewsAggregator`, which allows the parser to be replaced with a mock during testing.

This makes it possible to test the aggregation logic independently of file I/O.

All tests are currently passing.

## Performance Comparison

A benchmark is included to compare sequential and parallel processing.

The benchmark uses a simulated one-second delay for each source to represent an I/O-bound workload.

Example result:

```text
Sequential: 3035 ms
Parallel:   1012 ms
Speedup:    3.00x
```

With three independent sources and three worker threads, the parallel implementation completes the simulated workload in approximately the time required by the slowest individual task.

The benchmark uses artificial delays and therefore does not represent production performance. Its purpose is to demonstrate the effect of concurrent execution under a controlled workload.

## Project Structure

```text
parallel-news-aggregator/
│
├── .github/
│   └── workflows/
│       └── ...
│
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
│
├── data/
│   ├── article.json
│   ├── bbc.json
│   ├── google.json
│   └── reuters.json
│
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── Article.java
│   │       ├── ArticleParser.java
│   │       ├── Benchmark.java
│   │       ├── Main.java
│   │       ├── NewsAggregator.java
│   │       └── NewsSource.java
│   │
│   └── test/
│       └── java/
│           ├── ArticleParserTest.java
│           ├── ArticleTest.java
│           └── NewsAggregatorTest.java
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
* **JUnit 5**
* **Mockito**
* **Java Concurrency API**
* **GitHub Actions**

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

The current test suite consists of 10 tests.

### Run the Application

Run `Main.java` from the IDE or use the configured Java environment.

The application reads the sample news sources from the `data/` directory and aggregates their articles.

### Run the Benchmark

Run `Benchmark.java` to compare sequential and parallel execution using the simulated workload.

## Continuous Integration

The project uses GitHub Actions to automatically run the test suite when changes are pushed to the `main` branch or when a pull request targets `main`.

The CI workflow uses Java 17 and Maven to verify that the project builds successfully and that all tests pass.

## Future Improvements

Possible extensions include:

* configurable news sources;
* integration with real news APIs or RSS feeds;
* configurable concurrency limits;
* timeouts for sources that become unresponsive;
* structured application logging;
* additional integration tests;
* improved benchmark methodology;
* configuration through external files or environment variables;
* support for larger numbers of concurrent sources.
