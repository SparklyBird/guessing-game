package com.example.task.guessing_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class GuessingGameApplication {
	public static void main(String[] args) {
		SpringApplication.run(GuessingGameApplication.class, args);
	}
}