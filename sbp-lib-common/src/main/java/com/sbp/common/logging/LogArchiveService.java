package com.sbp.common.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Compresses rolled daily log files from the previous calendar month into {@code *.tar.gz}
 * under the archive directory.
 */
@Service
public class LogArchiveService {

	private static final Logger log = LoggerFactory.getLogger(LogArchiveService.class);

	private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT);

	private static final Pattern DAILY_LOG = Pattern.compile(".+\\.\\d{4}-\\d{2}-\\d{2}\\.log$");

	private static final Pattern DAILY_OUT = Pattern.compile("catalina\\.\\d{4}-\\d{2}-\\d{2}\\.out$");

	private static final Pattern DAILY_ACCESS = Pattern.compile("localhost_access_log\\.\\d{4}-\\d{2}-\\d{2}\\.txt$");

	private final SbpLoggingProperties properties;

	public LogArchiveService(SbpLoggingProperties properties) {
		this.properties = properties;
	}

	public void archivePreviousMonth() {
		if (!properties.isMonthlyArchive()) {
			return;
		}
		YearMonth target = YearMonth.now().minusMonths(1);
		Path logDir = Path.of(properties.getDirectory()).toAbsolutePath().normalize();
		Path archiveDir = Path.of(properties.getArchiveDirectory()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(archiveDir);
			for (String baseName : properties.getArchiveBaseNames()) {
				archiveGroup(logDir, archiveDir, baseName, target);
			}
		}
		catch (IOException ex) {
			log.error("Monthly log archive failed: {}", ex.getMessage(), ex);
		}
	}

	void archiveGroup(Path logDir, Path archiveDir, String baseName, YearMonth month) throws IOException {
		List<Path> candidates = listDailyFilesForMonth(logDir, baseName, month);
		if (candidates.isEmpty()) {
			return;
		}
		String archiveFileName = baseName + "-" + MONTH.format(month) + ".tar.gz";
		Path archiveFile = archiveDir.resolve(archiveFileName);
		if (Files.exists(archiveFile)) {
			log.info("Archive already exists, skipping: {}", archiveFile);
			return;
		}
		writeTarGz(archiveFile, candidates);
		for (Path file : candidates) {
			Files.deleteIfExists(file);
		}
		log.info("Archived {} files to {}", candidates.size(), archiveFile);
	}

	List<Path> listDailyFilesForMonth(Path logDir, String baseName, YearMonth month) throws IOException {
		if (!Files.isDirectory(logDir)) {
			return List.of();
		}
		String monthPrefix = month.toString();
		List<Path> matched = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(logDir)) {
			for (Path entry : stream) {
				if (!Files.isRegularFile(entry)) {
					continue;
				}
				String name = entry.getFileName().toString();
				if (!matchesBaseAndMonth(baseName, name, monthPrefix)) {
					continue;
				}
				matched.add(entry);
			}
		}
		return matched;
	}

	private boolean matchesBaseAndMonth(String baseName, String fileName, String monthPrefix) {
		if ("catalina".equals(baseName)) {
			return DAILY_OUT.matcher(fileName).matches() && fileName.contains(monthPrefix);
		}
		if ("localhost_access_log".equals(baseName)) {
			return DAILY_ACCESS.matcher(fileName).matches() && fileName.contains(monthPrefix);
		}
		if (!fileName.startsWith(baseName + ".")) {
			return false;
		}
		return DAILY_LOG.matcher(fileName).matches() && fileName.contains(monthPrefix);
	}

	private void writeTarGz(Path archiveFile, List<Path> files) throws IOException {
		try (OutputStream fos = Files.newOutputStream(archiveFile);
				GZIPOutputStream gzip = new GZIPOutputStream(fos);
				TarArchiveWriter tar = new TarArchiveWriter(gzip)) {
			for (Path file : files) {
				tar.addFile(file);
			}
		}
	}

}
