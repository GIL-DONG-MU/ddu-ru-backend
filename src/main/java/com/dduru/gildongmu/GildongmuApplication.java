package com.dduru.gildongmu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GildongmuApplication {

	public static void main(String[] args) {
		SpringApplication.run(GildongmuApplication.class, args);
	}

}
