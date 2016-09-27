package com.bazlur.jobseek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */

@SpringBootApplication
public class MainApp implements CommandLineRunner {

	@Autowired
	private Searcher searcher;

	public static void main(String[] args) {
		SpringApplication.run(MainApp.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		boolean foundNew = searcher.foundNew();

		if (foundNew) {
			searcher.sendEmail();
		}
	}
}
