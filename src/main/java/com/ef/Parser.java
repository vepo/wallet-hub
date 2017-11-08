package com.ef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

/**
 * Hello world!
 *
 */
@Component
public class Parser implements ApplicationRunner {
	private Date startDate;
	private Duration duration;
	private int threshold;
	private String logFile;
	private static DateFormat START_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");

	private Parser() {
	}

	public void run(ApplicationArguments args) {
		processParameters(args);
		processFile();
	}

	private void processFile() {
		try (Stream<String> stream = Files.lines(Paths.get(logFile))) {
			stream.forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processParameters(ApplicationArguments args) {
		List<String> startDates = args.getOptionValues("startDate");
		if (startDates == null || startDates.isEmpty()) {
			System.err.println("Argument startDate is required!");
			System.exit(1);
		} else {
			try {
				startDate = START_DATE_FORMAT.parse(startDates.get(0));
			} catch (ParseException e) {
				System.err.println("Argument startDate value is invalid!");
				System.exit(1);
			}
		}

		List<String> durations = args.getOptionValues("duration");
		if (durations == null || durations.isEmpty()) {
			System.err.println("Argument duration is required!");
			System.exit(1);
		} else {
			try {
				duration = Duration.valueOf(durations.get(0).toUpperCase());
			} catch (IllegalArgumentException e) {
				System.err.println("Argument duration value is invalid!");
				System.exit(1);
			}
		}

		List<String> thresholds = args.getOptionValues("threshold");
		if (thresholds == null || thresholds.isEmpty()) {
			System.err.println("Argument threshold is required!");
			System.exit(1);
		} else {
			try {
				threshold = Integer.valueOf(thresholds.get(0));
			} catch (NumberFormatException e) {
				System.err.println("Argument threshold value is invalid!");
				System.exit(1);
			}
		}

		logFile = "access.log";
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Parser.class, args);
	}
}
