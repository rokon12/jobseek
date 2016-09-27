package com.bazlur.jobseek;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
@Service
public class BdJobsClassifier implements Searcher {
	private static final Logger log = LoggerFactory.getLogger(BdJobsClassifier.class);

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

	//TODO keep a cache to prevent the same email again and again
	//Map<K, V> myCache = Collections.synchronizedMap(new WeakHashMap<K, V>());

	private static final String URL = "http://bdnews24.com/jobs/university-lecturer-jobs.html";
	private static final String[] KEYWORDS = {"ICT", "Software", "CSE", "Lecturer", "Dhaka"};
	private static final String[] WORDS = {"Published", "Vacancies", "Job Nature", "Experience", "Job Location", "Salary Range", "Application Deadline"};

	private List<JobSummery> jobSummaries = new ArrayList<>();

	@Autowired
	private EmailService emailService;

	public boolean foundNew() {
		log.info("[event:FOUND_NEW] going to fetch new job information");

		try {
			Document doc = Jsoup.connect(URL).timeout(10 * 1000).get();
			Element mainM2List = doc.getElementById("ResultsPageRight");
			Elements sections = mainM2List.getElementsByTag("section");
			List<JobSummery> jobSummaries = findNewJobSummaries(sections);

			this.jobSummaries = new ArrayList<>(jobSummaries);
		} catch (IOException e) {
			log.info("Couldn't find extract information", e);
		}

		return !jobSummaries.isEmpty();
	}

	private List<JobSummery> findNewJobSummaries(Elements sections) {

		return sections.stream()
			.filter(element -> element.getElementsByTag("h2") != null)
			.filter(this::filterJSoupElement)
			.map(this::parseJobSummery)
			.map(this::extract)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(jobSummery -> contains(jobSummery.getJobLocation(), "dhaka"))
			.filter(jobSummery -> jobSummery.getDeadLine().isAfter(LocalDate.now()))
			.collect(Collectors.toList());
	}

	private boolean filterJSoupElement(Element element) {
		String html = element.html();
		return Arrays.stream(KEYWORDS).anyMatch(s -> contains(html, s));
	}

	private Optional<JobSummery> extract(CompletableFuture<Optional<JobSummery>> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Couldn't extract data from CompletableFuture", e);
		}

		return Optional.empty();
	}

	private CompletableFuture<Optional<JobSummery>> parseJobSummery(Element element) {
		String url = element.getElementsByTag("a").attr("href");
		CompletableFuture<Optional<JobSummery>> optionalCompletableFuture = CompletableFuture.supplyAsync(() -> doParse(url));

		return optionalCompletableFuture
			.exceptionally(throwable -> {
				log.error("couldn't complete the request", throwable);

				return Optional.empty();
			});
	}

	private Optional<JobSummery> doParse(String url) {
		try {
			url = "http://bdnews24.com" + url;
			Document doc = Jsoup.connect(url).get();
			String baseUrl = doc.baseUri();

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

				JobSummery parse = parse(list, baseUrl);
				return Optional.of(parse);
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

	private Optional<LocalDate> parseDate(String date) {
		try {
			String dateString = date.replaceAll("\\s+$", "");
			LocalDate parsed = LocalDate.parse(dateString, FORMATTER);

			return Optional.of(parsed);
		} catch (Exception e) {
			log.error("Couldn't parse date : {}", date, e);
			return Optional.empty();
		}
	}

	private boolean contains(final String haystack, final String needle) {
		String haystackTemp = ((haystack == null) ? "" : haystack).toLowerCase();
		String needleTemp = (needle == null ? "" : needle).toLowerCase();

		return haystackTemp.contains(needleTemp);
	}

	public boolean sendEmail() {
		StringBuilder builder = new StringBuilder();
		builder.append("Available jobs in Dhaka");
		builder.append("<br/>");

		builder.append("<table>")
			.append("<tr>");
		Arrays.stream(WORDS).forEach(s -> builder.append("<td>")
			.append(s)
			.append("</td>"));
		builder.append("<td>")
			.append("Details")
			.append("</td>");
		builder.append("</tr>");

		jobSummaries.forEach(jobSummery -> {
			builder.append("<tr>");
			builder.append("<td>").
				append(jobSummery.getPublishedOn())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getVacancies())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getJobNature())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getExperience() == null ? "" : jobSummery.getExperience())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getJobLocation())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getSalaryRange())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getDeadLine())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getUrl())
				.append("</td>");

			builder.append("</tr>");
		});

		builder.append("</table>");
		builder.append("<br/>");
		builder.append("<br/>");
		builder.append("-");
		builder.append("<br/>");
		builder.append("Job Seek Robot!");

		System.out.println(builder.toString());

		emailService.sendEmail(builder.toString());
		return true;
	}
}
