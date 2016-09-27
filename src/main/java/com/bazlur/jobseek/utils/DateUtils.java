package com.bazlur.jobseek.utils;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/28/16.
 */
public class DateUtils {
	private static final Logger log = LoggerFactory.getLogger(DateUtils.class);
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

	public static Optional<LocalDate> parseDate(String date) {
		try {
			String dateString = date.replaceAll("\\s+$", "");
			LocalDate parsed = LocalDate.parse(dateString, FORMATTER);

			return Optional.of(parsed);
		} catch (Exception e) {
			log.error("Couldn't parse date : {}", date, e);
			return Optional.empty();
		}
	}


	public static Optional<LocalDate> parseDate2(String date) {

		try {
			Date parse = getFormattedDate(date).parse(date);
			LocalDate localDate = parse.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			System.out.println(parse);
			return Optional.of(localDate);
		} catch (Exception e) {
			log.error("Couldn't parse date : {}", date, e);
			return Optional.empty();
		}
	}

	public static Optional<LocalDate> getDate(String text) {
		PrettyTimeParser prettyTimeParser = new PrettyTimeParser();

		List<Date> dates = prettyTimeParser.parse(text);
		if (dates.size() > 0) {
			Date date = dates.get(0);
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return Optional.of(localDate);
		}
		return Optional.empty();
	}


	private static SimpleDateFormat getFormattedDate(String date) {
		if (date.contains("st")) {
			return new SimpleDateFormat("EEEE MMMM d'st', YYYY");
		} else if (date.contains("nd")) {
			return new SimpleDateFormat("EEEE MMMM d'nd', YYYY");

		} else if (date.contains("rd")) {
			return new SimpleDateFormat("EEEE MMMM d'rd', YYYY");

		} else
			return new SimpleDateFormat("EEEE MMMM d'th', YYYY");
	}


//	public static void main(String[] args) throws ParseException {
//		//String dateString = "Sunday August 28th, 2016";
//
//		PrettyTimeParser prettyTimeParser = new PrettyTimeParser();
//		List<Date> parse = prettyTimeParser.parse("Job expires at 8:09am on Wednesday October 19th, 2016\n\n");
//		parse.forEach(date -> {
//			System.out.println(date);
//		});
//		//LocalDate parsed = LocalDate.parse(dateString, FORMATTER[1]);
//		//System.out.println(parsed);
//	}
}
