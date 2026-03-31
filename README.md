# Guessing Game

A Spring Boot web application where players try to guess a randomly generated 4-digit secret number. Each digit in the secret number is unique. After every guess, the game reports how many digits are in the correct position (**P**) and how many are present but in the wrong position (**M**).

## Features

- **Dual login modes** — play with a custom display name or via Google OAuth2
- **Session-based game state** — each player gets an isolated game session
- **Persistent leaderboard** — win rates and guess counts tracked per player (guests excluded)
- **H2 database** — file-based persistence out of the box; swap to any JPA-compatible database via config
- **Responsive dark-themed UI** — Thymeleaf templates with animated transitions
- **Shareable results** — Web Share API with clipboard fallback on the game-over screen

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5 |
| Language | Java 17 |
| Templating | Thymeleaf |
| Persistence | Spring Data JPA + H2 |
| Security | Spring Security + OAuth2 Client (Google) |
| Build | Maven |
| Testing | JUnit 5, Mockito, MockMvc, JaCoCo |

## How to Run Locally

### Prerequisites

- Java 17+
- Maven 3.6+ (or use the included `mvnw` wrapper)

### Steps

```bash
# Clone the repository
git clone <repo-url>
cd guessing-game

# Run the application
./mvnw spring-boot:run
```

The application starts on **http://localhost:8080** by default.

> **Note:** Google OAuth2 login will not work until credentials are configured (see below). The manual name-entry login works without any OAuth2 setup.

## How to Configure Google OAuth2

1. Go to the [Google Cloud Console](https://console.cloud.google.com/) and create a new project (or select an existing one).
2. Navigate to **APIs & Services → Credentials** and create an **OAuth 2.0 Client ID** of type *Web application*.
3. Add `http://localhost:8080/login/oauth2/code/google` as an **Authorised redirect URI**.
4. Copy the generated **Client ID** and **Client Secret**.
5. Open `src/main/resources/application.properties` and replace the placeholder values:

```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

Restart the application. The *Play with Google Account* button on the home page will now complete the OAuth2 flow and start a game under your Google display name.

## How to Access the H2 Console

The embedded H2 console is enabled in development mode and is available at:

```
http://localhost:8080/h2-console
```

Use the following connection settings (matching `application.properties`):

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/guessinggame` |
| User Name | `sa` |
| Password | `password` |

## How to Run Tests

```bash
# Run all tests
./mvnw test

# Run tests and generate the JaCoCo coverage report
./mvnw verify
```

The HTML coverage report is written to `target/site/jacoco/index.html` after `mvn verify`.

The build enforces a **minimum 70% instruction coverage** via the JaCoCo `check` goal. The build will fail if coverage drops below this threshold.

## Game Rules

- The program picks a secret 4-digit number where all digits are different.
- The player has **8 attempts** to guess the number.
- After each guess the result is shown as `M:m; P:p` where:
  - **M** — digits present in the secret number but in the wrong position
  - **P** — digits in the exact correct position
- The game ends when the player guesses correctly (`P:4`) or runs out of attempts.

### Example

| Secret | Guess | Result |
|---|---|---|
| 7046 | 8724 | M:2; P:0 |
| 7046 | 7842 | M:0; P:2 |
| 7046 | 7640 | M:2; P:2 |
| 7046 | 7046 | M:0; P:4 |
