package com.bazlur.jobseek;

import com.bazlur.jobseek.parser.AllBdJobsParser;
import com.bazlur.jobseek.parser.BdJobsParser;
import com.bazlur.jobseek.utils.JobUrlCache;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.bazlur.jobseek.utils.StringUtils.contains;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
@Service
public class SearcherImpl implements Searcher {
	private static final Logger log = LoggerFactory.getLogger(SearcherImpl.class);

	//TODO keep a cache to prevent the same email again and again
	//Map<K, V> myCache = Collections.synchronizedMap(new WeakHashMap<K, V>());

	private static final String URL = "http://bdnews24.com/jobs/university-lecturer-jobs.html";
	private static final String JOB_LOCATION = "dhaka";

	private List<JobSummery> jobSummaries = new ArrayList<>();

	private BdJobsParser bdJobsParser;
	private AllBdJobsParser allBdJobsParser;
	private EmailService emailService;
	private Executor executor;

	@Autowired
	public SearcherImpl(Executor executor, EmailService emailService, AllBdJobsParser allBdJobsParser, BdJobsParser bdJobsParser) {
		this.executor = executor;
		this.emailService = emailService;
		this.allBdJobsParser = allBdJobsParser;
		this.bdJobsParser = bdJobsParser;
	}

	public boolean foundNew() {
		log.info("[event:FOUND_NEW] going to fetch new job information");

		try {
			Document doc = Jsoup.connect(URL).timeout(10 * 1000).get();
			Element mainM2List = doc.getElementById("ResultsPageRight");
			Elements sections = mainM2List.getElementsByTag("section");
			List<JobSummery> jobSummaries = findJobSummariesFromBd(sections);

			this.jobSummaries = new ArrayList<>(jobSummaries);
		} catch (IOException e) {
			log.info("Couldn't find extract information", e);
		}

		log.info("Total outside bd jobs: :{}", JobUrlCache.getInstance().getCache().size());
		List<JobSummery> summeries = JobUrlCache.getInstance().getCache().stream()
			.map(this::parseJobSummery)
			.map(this::extract)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(jobSummery -> jobSummery.getDeadLine() == null || jobSummery.getDeadLine().isAfter(LocalDate.now()))
			.collect(Collectors.toList());

		log.info("total found in outside bdjobs : {}", summeries.size());
		this.jobSummaries.addAll(summeries);

		return !jobSummaries.isEmpty();
	}

	private List<JobSummery> findJobSummariesFromBd(Elements sections) {

		return sections.stream()
			.filter(element -> element.getElementsByTag("h2") != null)
			.filter(this::filterJSoupElement)
			.map(this::parseJobSummery)
			.map(this::extract)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(jobSummery -> contains(jobSummery.getJobLocation(), JOB_LOCATION))
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

		return CompletableFuture.supplyAsync(() -> bdJobsParser.doParse(url), executor)
			.exceptionally(throwable -> {
				log.error("couldn't complete the request", throwable);

				return Optional.empty();
			});
	}

	private CompletableFuture<Optional<JobSummery>> parseJobSummery(String url) {

		return CompletableFuture.supplyAsync(() -> allBdJobsParser.doParse(url), executor)
			.exceptionally(throwable -> {
				log.error("couldn't complete the request", throwable);

				return Optional.empty();
			});
	}

	public boolean sendEmail() {
		StringBuilder builder = new StringBuilder();
		builder.append("Available jobs in Dhaka");
		builder.append("<br/>");

		builder.append("<table style=\"1px solid black;\"> ")
			.append("<tr>");

		builder.append("<th>")
			.append("#")
			.append("</th>");
		builder.append("<th>")
			.append("Job Title")
			.append("Company")
			.append("</th>");
		builder.append("<th>")
			.append("Company")
			.append("</th>");
		Arrays.stream(WORDS).forEach(s -> builder.append("<th>")
			.append(s)
			.append("</th>"));
		builder.append("<td>")
			.append("Details")
			.append("</td>");
		builder.append("</tr>");

		final int[] index = {0};
		jobSummaries.forEach(jobSummery -> {
			builder.append("<tr>");
			builder.append("<td>").
				append(++index[0])
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getJobTitle())
				.append("</td>");

			builder.append("<td>").
				append(jobSummery.getCompanyName())
				.append("</td>");

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

		emailService.sendEmail(builder.toString());

		JobUrlCache.getInstance().getCache().clear();
		return true;
	}
}
