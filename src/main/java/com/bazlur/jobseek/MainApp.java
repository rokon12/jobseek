package com.bazlur.jobseek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */

@SpringBootApplication
public class MainApp implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(MainApp.class);

	public static void main(String[] args) {
		SpringApplication.run(MainApp.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Application has started successfully");
	}
}
