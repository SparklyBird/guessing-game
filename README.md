# Guessing Game

Game rules:
- Program chooses a random secret number with 4 digits.
- All digits in the secret number are different.
- Player has 8 tries to guess the secret number.
- After each guess program displays the message "M:m; P:p" where:
  - m - number of matching digits but not on the right places
  - p - number of matching digits on exact places
- Game ends after 8 tries or if the correct number is guessed.  

Samples:
Secret:  **7046**
Guess:   **8724**
Message: **M:2; P:0**

Secret:  **7046**
Guess:   **7842**
Message: **M:0; P:2**

Secret:  **7046**
Guess:   **7640**
Message: **M:2; P:2**