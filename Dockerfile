FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
CMD ["java", "-cp", "target/classes:target/dependency/*", "com.flavia.newsaggregator.Main"]