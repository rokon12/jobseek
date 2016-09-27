package com.bazlur.jobseek.parser;

import com.bazlur.jobseek.JobSummery;

import java.util.Optional;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/28/16.
 */
public interface JobParser {
	Optional<JobSummery> doParse(String url);
}
