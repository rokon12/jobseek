package com.bazlur.jobseek.parser;

import com.bazlur.jobseek.JobSummery;
import com.bazlur.jobseek.utils.DateUtils;
import com.bazlur.jobseek.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static com.bazlur.jobseek.Searcher.KEYWORDS;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/28/16.
 */
@Service
public class AllBdJobsParser implements JobParser {
	private static final Logger log = LoggerFactory.getLogger(AllBdJobsParser.class);

	@Override
	public Optional<JobSummery> doParse(String url) {
		try {
			Document document = Jsoup.connect(url).timeout(10 * 1000).get();

			String baseUri = document.baseUri();
			String headers = document.getElementsByClass("postheader").text();
			boolean anyMatch = Arrays.stream(KEYWORDS).anyMatch(s -> StringUtils.contains(headers, s));
			if (!anyMatch) {
				return Optional.empty();
			}

			JobSummery jobSummery = new JobSummery();
			jobSummery.setUrl(baseUri);
			jobSummery.setJobTitle(headers);
			String published = document.getElementsByClass("published").text();

			if (StringUtils.isNotEmpty(published)) {
				Optional<LocalDate> date = DateUtils.getDate(published);
				date.ifPresent(jobSummery::setPublishedOn);
			}

			Elements postcontent = document.getElementsByClass("postcontent");
			postcontent.stream()
				.filter(element -> element.text().contains("Job expires at"))
				.findFirst().ifPresent(element -> {
				String text = element.text();
				Optional<LocalDate> date = DateUtils.getDate(text);
				date.ifPresent((jobSummery::setDeadLine));
			});

			return Optional.of(jobSummery);
		} catch (IOException e) {
			log.info("could not fetch from url {}", url);
			return Optional.empty();
		}
	}
}
