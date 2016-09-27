package com.bazlur.jobseek;

import org.springframework.stereotype.Component;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
@Component
public interface Searcher {
	String[] KEYWORDS = {"ICT", "Software", "CSE", "Lecturer", "Dhaka"};
	String[] WORDS = {"Published", "Vacancies", "Job Nature", "Experience", "Job Location", "Salary Range", "Application Deadline"};

	boolean foundNew();

	boolean sendEmail();
}
