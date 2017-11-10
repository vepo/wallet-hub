package com.ef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ef.db.RollbackException;
import com.ef.db.services.AccessLogService;

/**
 * Command line application for processing the <b>access.log</b> file and block
 * IP that exceeds a given number of requests.
 *
 * @author <a href="mailto:victor.perticarrari@gmail.com">Victor Osório</a>
 */
@SpringBootApplication
public class Parser implements ApplicationRunner {
	private static final DateFormat START_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");

	// 2017-01-01 00:01:08.028
	private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/*
	 * We should set the timezone to UTC.
	 */
	static {
		START_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		LOG_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private static final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

	@Autowired
	private AccessLogService accessLogService;

	private Date startDate;

	private Duration duration;

	private int threshold;

	private String logFile = "access.log"; // FIXME: It can be an application parameter

	public void run(ApplicationArguments args) {
		processParameters(args);
		processFile();
		processBlocked();
	}

	private void processBlocked() {
		Date endDate;
		switch (duration) {
		case DAILY:
			endDate = new Date(startDate.getTime() + (60 * 24 * ONE_MINUTE_IN_MILLIS));
			break;
		case HOURLY:
			endDate = new Date(startDate.getTime() + (60 * ONE_MINUTE_IN_MILLIS));
			break;
		default:
			throw new NotYetImplementedException("Duration not implemented: " + duration);
		}
		accessLogService.createBlockedIPs(startDate, endDate, threshold);
	}

	private void processFile() {
		try (Stream<String> stream = Files.lines(Paths.get(logFile))) {
			stream.map(line -> line.split("\\|")).forEach(values -> {
				try {
					accessLogService.register(LOG_DATE_FORMAT.parse(values[0]), values[1], values[2],
							Integer.parseInt(values[3]), values[4]);
				} catch (NumberFormatException e) {
					System.err.println("Invalid response code \"" + values[3] + "\". Ignoring line.");
				} catch (ParseException e) {
					System.err.println("Invalid date \"" + values[0] + "\". Ignoring line.");
				} catch (RollbackException e) {
					System.err.println("Line already processed! Ignoring line.");
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processParameters(ApplicationArguments args) {
		if (args.containsOption("help")) {
			printUsage();
			System.exit(0);
		}

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

	}

	private void printUsage() {
		System.out.println("Process \"access.log\" file and add IPs to blocked list.\n"
				+ "Usage: java -cp \"parser.jar\" com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100\n\n"
				+ "Arguments:\n"
				+ "\tstartDate    Time that the parser will check for blocked ips\n"
				+ "\tduration     The window of check. Accepts: \"hourly\", \"daily\"\n"
				+ "\tthreshold:   The minimum number of request for block an IP");

	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Parser.class, args);
	}
}
