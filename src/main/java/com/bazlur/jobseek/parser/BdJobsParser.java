package com.bazlur.jobseek.parser;

import com.bazlur.jobseek.JobSummery;
import com.bazlur.jobseek.utils.JobUrlCache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bazlur.jobseek.Searcher.WORDS;
import static com.bazlur.jobseek.utils.DateUtils.parseDate;
import static com.bazlur.jobseek.utils.StringUtils.contains;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/28/16.
 */
@Service
public class BdJobsParser implements JobParser {
	private static final Logger log = LoggerFactory.getLogger(BdJobsParser.class);
	//private static final String[] KEYWORDS = {"ICT", "Software", "CSE", "Lecturer", "Dhaka"};
	//private static final String[] WORDS = {"Published", "Vacancies", "Job Nature", "Experience", "Job Location", "Salary Range", "Application Deadline"};

	public Optional<JobSummery> doParse(String url) {
		try {
			url = "http://bdnews24.com" + url;
			Document doc = Jsoup.connect(url).get();
			String baseUrl = doc.baseUri();

			if (!baseUrl.contains("bdjobs.com")) {
				JobUrlCache.getInstance().add(baseUrl);

				return Optional.empty();
			}
			log.info("parsing url: {}", baseUrl);

			String jobTitle = doc.getElementsByClass("job-title").text();
			String companyName = doc.getElementsByClass("company-name").text();

			Elements elementsByClass = doc.getElementsByClass("m-view");
			Optional<Element> firstElementOptional = elementsByClass.stream().filter(element -> {
				Elements body = element.getElementsByClass("job-summary");
				return !body.isEmpty();
			}).findFirst();

			if (firstElementOptional.isPresent()) {
				Element element = firstElementOptional.get();

				Elements h4s = element.getElementsByTag("h4");
				Set<Element> summaries = h4s.stream()
					.filter(e -> {
						String html = e.html();
						return Arrays.stream(WORDS).anyMatch(s -> contains(html, s));
					}).collect(Collectors.toSet());

				List<String> list = summaries.stream()
					.map(e -> e.text().replaceAll("\u00A0", ""))
					.collect(Collectors.toList());

				JobSummery jobSummery = parse(list, baseUrl);
				jobSummery.setCompanyName(companyName);
				jobSummery.setJobTitle(jobTitle);

				return Optional.of(jobSummery);
			}
		} catch (IOException e) {
			log.info("couldn't fetch item from url: {}", url);
		}

		return Optional.empty();
	}

	private JobSummery parse(List<String> items, String baseUrl) {
		JobSummery summery = new JobSummery();
		summery.setUrl(baseUrl);

		items.forEach(s -> {
			String[] split = s.split(":");
			if (contains(split[0], WORDS[0])) {
				parseDate(split[1].trim()).ifPresent(summery::setPublishedOn);
			} else if (contains(split[0], WORDS[1])) {
				String trim = split[1].trim();
				summery.setVacancies(Integer.parseInt(trim));
			} else if (contains(split[0], WORDS[2])) {
				String trim = split[1].trim();
				summery.setJobNature(trim);
			} else if (contains(split[0], WORDS[3])) {
				String trim = split[1].trim();
				summery.setExperience(trim);
			} else if (contains(split[0], WORDS[4])) {
				String trim = split[1].trim();
				summery.setJobLocation(trim);
			} else if (contains(split[0], WORDS[5])) {
				String trim = split[1].trim();
				summery.setSalaryRange(trim);
			} else if (contains(split[0], WORDS[6])) {
				parseDate(split[1].trim()).ifPresent(summery::setDeadLine);
			}
		});

		return summery;
	}
}
