package com.ef;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ef.db.hibernate.HibernateUtil;
import com.ef.db.services.AccessLogService;
import com.ef.params.Duration;
import com.ef.utils.DateUtils;

/**
 * Command line application for processing the <b>access.log</b> file and block
 * IP that exceeds a given number of requests.
 * 
 * <p>
 * The execution take a long time. For a file with 116484 entries, it takes:
 * 
 * <pre>
 * real	105m14.757s
 * user	0m37.668s
 * sys	0m18.568s
 * </pre>
 * </p>
 *
 * @author <a href="mailto:victor.perticarrari@gmail.com">Victor Osório</a>
 */
public class Parser {

	private static Logger LOGGER = LoggerFactory.getLogger(Parser.class);

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

	private AccessLogService accessLogService = new AccessLogService();

	private Date startDate;

	private Duration duration;

	private int threshold = -1;

	private String logFile = "access.log";

	public Parser(String[] args) {
		processParameters(args);
	}

	/**
	 * Execute application
	 */
	public void run() {
		LOGGER.info("Pasing " + logFile + ": startDate=" + LOG_DATE_FORMAT.format(startDate) + " duration=" + duration
				+ " threshold=" + threshold);
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
			endDate = DateUtils.oneDayAfter(startDate);
			break;
		case HOURLY:
			endDate = DateUtils.oneHourAfter(startDate);
			break;
		default:
			endDate = null;
			break;
		}

		if (endDate == null) {
			LOGGER.info("Invalid argument: " + duration);
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
					LOGGER.error("Invalid line: " + Arrays.toString(values), e);
					System.err.println("Invalid response code \"" + values[3] + "\". Ignoring line.");
				} catch (ParseException e) {
					LOGGER.error("Invalid line: " + Arrays.toString(values), e);
					System.err.println("Invalid date \"" + values[0] + "\". Ignoring line.");
				}
			});
		} catch (IOException e) {
			LOGGER.error("Couldn't open access log file: " + this.logFile, e);
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
						LOGGER.info("Invalid argument: " + startDate, e);
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
						LOGGER.info("Invalid argument: " + duration, e);
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
						LOGGER.info("Invalid argument: " + threshold, e);
						System.err.println("Argument threshold value is invalid!");
						printUsage();
						System.exit(1);
					}
				}
			} else if (arg.startsWith("--accesslog=")) {
				File accesslog = Paths.get(arg.replace("--accesslog=", "")).toFile();
				if (!accesslog.exists() || !accesslog.isFile()) {
					LOGGER.info("Invalid file: " + accesslog);
					System.err.println("accesslog is not a valid file!");
					printUsage();
					System.exit(1);
				} else {
					this.logFile = accesslog.getAbsolutePath();
				}
			}
		}

		if (startDate == null) {
			System.err.println("Argument startDate is required!");
			printUsage();
			System.exit(1);
		}
		if (duration == null) {
			System.err.println("Argument duration is required!");
			printUsage();
			System.exit(1);
		}

		if (threshold == -1) {
			System.err.println("Argument threshold is required!");
			printUsage();
			System.exit(1);
		}
	}

	/**
	 * Print usage
	 */
	private void printUsage() {
		System.out.println("Process \"access.log\" file and add IPs to blocked list.\n"
				+ "Usage: java -cp \"parser.jar\" com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100\n\n"
				+ "Arguments:\n"
				+ "\t--startDate=DATE       [REQUIRED]   Time that the parser will check for blocked ips. Use date format \"yyyy-MM-dd.HH:mm:ss\"\n"
				+ "\t--duration=DURATION    [REQUIRED]   The window of check. Accepts: \"hourly\" or \"daily\"\n"
				+ "\t--accesslog=FILE                    The access log file. The default value is \"access.log\"\n"
				+ "\t--threshold=THRESHOLD  [REQUIRED]   The minimum number of request for block an IP");

	}

	public static void main(String[] args) throws Exception {
		try {
			PropertyConfigurator.configure(Parser.class.getResourceAsStream("/log4j.properties")); // configure log
			Parser parser = new Parser(args);
			parser.run();
		} finally {
			HibernateUtil.shutdown();
		}
	}
}
