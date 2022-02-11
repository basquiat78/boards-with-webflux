package io.basquiat.boards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
public class BoardsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardsApplication.class, args);
	}

}
