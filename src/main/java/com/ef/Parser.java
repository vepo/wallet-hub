package com.ef;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;

import com.ef.db.services.AccessLogService;
import com.ef.params.Duration;

/**
 * Command line application for processing the <b>access.log</b> file and block
 * IP that exceeds a given number of requests.
 *
 * @author <a href="mailto:victor.perticarrari@gmail.com">Victor Osório</a>
 */
public class Parser {
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

	private static final long ONE_MINUTE_IN_MILLIS = 60000;// milliseconds

	private AccessLogService accessLogService = new AccessLogService();

	private Date startDate;

	private Duration duration;

	private int threshold;

	private String logFile = "access.log";

	public Parser(String[] args) {
		processParameters(args);
	}

	/**
	 * Execute application
	 */
	public void run() {
		processFile();
		processBlocked();
	}

	/**
	 * Create blocked IPs
	 */
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
			endDate = null;
			break;
		}
		
		if (endDate == null) {
			System.err.println("Invalid Duration: " + duration);
			printUsage();
			System.exit(1);
		}
		accessLogService.createBlockedIPs(startDate, endDate, threshold);
	}

	/**
	 * Process Log File creating one database registry for each line
	 */
	private void processFile() {
		try (Stream<String> stream = Files.lines(Paths.get(logFile))) {
			stream.map(line -> line.split("\\|")).forEach(values -> {
				try {
					accessLogService.register(LOG_DATE_FORMAT.parse(values[0]), values[1], values[2],
							Integer.parseInt(values[3]), values[4]);
					// System.out.print("\u001B[2K\rProcessando: " + values[0]);
				} catch (NumberFormatException e) {
					System.err.println("Invalid response code \"" + values[3] + "\". Ignoring line.");
				} catch (ParseException e) {
					System.err.println("Invalid date \"" + values[0] + "\". Ignoring line.");
				}
			});
		} catch (IOException e) {
			System.err.println("Couldn't open access log file: " + this.logFile);
			printUsage();
			System.exit(1);
		}
	}

	/**
	 * Extract parameters
	 * 
	 * @param args
	 *            Application arguments
	 */
	private void processParameters(String[] args) {
		for (String arg : args) {
			if (arg.equals("--help")) {
				printUsage();
				System.exit(0);
			} else if (arg.startsWith("--startDate=")) {
				String startDate = arg.replace("--startDate=", "");
				if (startDate.isEmpty()) {
					System.err.println("Argument startDate is required!");
					printUsage();
					System.exit(1);
				} else {
					try {
						this.startDate = START_DATE_FORMAT.parse(startDate);
					} catch (ParseException e) {
						System.err.println("Argument startDate value is invalid!");
						printUsage();
						System.exit(1);
					}
				}
			} else if (arg.startsWith("--duration=")) {
				String duration = arg.replace("--duration=", "");
				if (duration.isEmpty()) {
					System.err.println("Argument duration is required!");
					printUsage();
					System.exit(1);
				} else {
					try {
						this.duration = Duration.valueOf(duration.toUpperCase());
					} catch (IllegalArgumentException e) {
						System.err.println("Argument duration value is invalid!");
						printUsage();
						System.exit(1);
					}
				}
			} else if (arg.startsWith("--threshold=")) {
				String threshold = arg.replace("--threshold=", "");
				if (threshold.isEmpty()) {
					System.err.println("Argument threshold is required!");
					printUsage();
					System.exit(1);
				} else {
					try {
						this.threshold = Integer.valueOf(threshold);
					} catch (NumberFormatException e) {
						System.err.println("Argument threshold value is invalid!");
						printUsage();
						System.exit(1);
					}
				}
			} else if (arg.startsWith("--accesslog=")) {
				File accesslog = Paths.get(arg.replace("--accesslog=", "")).toFile();
				if (!accesslog.exists() || !accesslog.isFile()) {
					System.err.println("accesslog is not a valid file!");
					printUsage();
					System.exit(1);
				} else {
					this.logFile = accesslog.getAbsolutePath();
				}
			}
		}
	}

	/**
	 * Print usage
	 */
	private void printUsage() {
		System.out.println("Process \"access.log\" file and add IPs to blocked list.\n"
				+ "Usage: java -cp \"parser.jar\" com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100\n\n"
				+ "Arguments:\n"
				+ "\t--startDate=DATE        Time that the parser will check for blocked ips. Use date format \"yyyy-MM-dd.HH:mm:ss\"\n"
				+ "\t--duration=DURATION     The window of check. Accepts: \"hourly\" or \"daily\"\n"
				+ "\t--accesslog=FILE        The access log file. The default value is \"access.log\"\n"
				+ "\t--threshold=THRESHOLD   The minimum number of request for block an IP");

	}

	public static void main(String[] args) throws Exception {
		Parser parser = new Parser(args);
		parser.run();
	}
}
