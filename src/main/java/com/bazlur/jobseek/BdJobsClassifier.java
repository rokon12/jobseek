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
    private static final String URL = "http://bdnews24.com/jobs/university-lecturer-jobs.html";

    private String[] keywords = {"ICT", "Software", "CSE", "Lecturer", "Dhaka"};
    private String[] words = {"Published", "Vacancies", "Job Nature", "Experience", "Job Location", "Salary Range", "Application Deadline"};

    private List<JobSummery> jobSummaries = new ArrayList<>();

    @Autowired
    private EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(BdJobsClassifier.class);

    public boolean foundNew() {
        log.info("found new called: {}");

        try {
            Document doc = Jsoup.connect(URL).get();
            Element mainM2List = doc.getElementById("ResultsPageRight");
            Elements sections = mainM2List.getElementsByTag("section");

            List<JobSummery> jobSummaries = sections.stream()
                    .filter(element -> element.getElementsByTag("h2") != null)
                    .filter(this::filterJSoupElement)
                    .map(this::getOptionalCompletableFuture)
                    .map(this::extractFromFuture)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(jobSummery -> contains(jobSummery.getJobLocation(), "dhaka"))
                    .filter(jobSummery -> jobSummery.getDeadLine().isAfter(LocalDate.now()))
                    .collect(Collectors.toList());

            this.jobSummaries = new ArrayList<>(jobSummaries);

        } catch (IOException e) {
            log.info("Couldn't find extract information");
        }

        return !jobSummaries.isEmpty();
    }

    private boolean filterJSoupElement(Element element) {
        String html = element.html();
        return Arrays.stream(keywords).anyMatch(s -> contains(html, s));
    }

    private Optional<JobSummery> extractFromFuture(CompletableFuture<Optional<?>> future) {
        try {
            //cast is required
            return (Optional<JobSummery>) future.get();
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }
    }

    private CompletableFuture<Optional<?>> getOptionalCompletableFuture(Element element) {
        String url = element.getElementsByTag("a").attr("href");

        return CompletableFuture.supplyAsync(() -> {
            try {
                return doParse(url);
            } catch (IOException e1) {
                log.error("Couldn't get item: ", e1);
                return Optional.empty();
            }
        })
                .exceptionally(throwable -> {
                    log.error("couldn't found", throwable);
                    return Optional.empty();
                });
    }

    private Optional<JobSummery> doParse(String url) throws IOException {
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
                Set<Element> summaries = h4s.stream().filter(e -> {
                    String html = e.html();
                    return Arrays.stream(words).anyMatch(s -> contains(html, s));
                }).collect(Collectors.toSet());

                List<String> list = summaries.stream()
                        .map(e -> e.text().replaceAll("\u00A0", ""))
                        .collect(Collectors.toList());

                JobSummery parse = parse(list, baseUrl);
                return Optional.of(parse);
            }
        } catch (IOException e) {
            log.info("couldn't fetch item");
        }

        return Optional.empty();
    }

    private JobSummery parse(List<String> items, String baseUrl) {
        JobSummery summery = new JobSummery();
        summery.setUrl(baseUrl);

        items.forEach(s -> {

            String[] split = s.split(":");
            if (contains(split[0], words[0])) {
                LocalDate localDate = parseDate(split[1].trim());
                summery.setPublishedOn(localDate);
            }

            if (contains(split[0], words[1])) {
                String trim = split[1].trim();
                summery.setVacancies(Integer.parseInt(trim));
            }

            if (contains(split[0], words[2])) {
                String trim = split[1].trim();
                summery.setJobNature(trim);
            }

            if (contains(split[0], words[3])) {
                String trim = split[1].trim();
                summery.setExperience(trim);
            }

            if (contains(split[0], words[4])) {
                String trim = split[1].trim();
                summery.setJobLocation(trim);
            }

            if (contains(split[0], words[5])) {
                String trim = split[1].trim();
                summery.setSalaryRange(trim);
            }

            if (contains(split[0], words[6])) {
                LocalDate localDate = parseDate(split[1].trim());
                summery.setDeadLine(localDate);
            }
        });

        return summery;
    }

    public LocalDate parseDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            try {
                String dateString = date.replaceAll("\\s+$", "");
                dateString = dateString.substring(1);
                return LocalDate.parse(dateString, formatter);
            } catch (Exception e) {
                return LocalDate.parse(date, formatter);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private boolean contains(String haystack, String needle) {
        haystack = haystack == null ? "" : haystack;
        needle = needle == null ? "" : needle;

        return haystack.toLowerCase().contains(needle.toLowerCase());
    }

    public boolean sendEmail() {

        jobSummaries.forEach(System.out::println);

        emailService.sendEmail();
        return false;
    }
}
