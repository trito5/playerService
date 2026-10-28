# Player Service

Built with Java 25, Micronaut, Reactor, PostgreSQL (R2DBC), Liquibase and Lombok.
Tests use JUnit 5, Micronaut Test and Testcontainers.

## Running tests

Install JDK 25 and make sure Docker is running.

Open the project in IntelliJ, set the Project SDK and Gradle JVM to Java 25,
let Gradle sync and run the tests from the IDE.

Or run `./gradlew test` from the project root (`.\gradlew.bat test` on Windows).

Testcontainers starts and cleans up the test database, and Liquibase creates the schema.
Other services are mocked, so no extra setup is needed.

## Not implemented - Possible improvements

- Cache config and news responses.
- Use caching or Redis for leaderboard data and player names.
- Improve error handling when calling internal services, for example with retries.
- Logging

