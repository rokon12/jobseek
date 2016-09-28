package com.bazlur.jobseek.scheduler;

import com.bazlur.jobseek.Searcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/28/16.
 */
@Service
public class SearchScheduler {
	private static final Logger log = LoggerFactory.getLogger(SearchScheduler.class);

	@Autowired
	private Searcher searcher;

	@Scheduled(cron = "0 0 11 1/1 * ?")
	public void findJobs() {
		log.info("Find new jobs");

		if (searcher.foundNew()) {
			searcher.sendEmail();
		}
	}
}
