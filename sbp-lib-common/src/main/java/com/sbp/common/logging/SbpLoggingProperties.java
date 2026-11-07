package com.sbp.common.logging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "sbp.logging")
@Getter
@Setter
public class SbpLoggingProperties {

	private boolean enabled = true;

	private String directory = "logs";

	private String archiveDirectory = "logs/archive";

	private boolean dailyRollover = true;

	private boolean monthlyArchive = true;

	/** Cron for monthly archive job (default: 00:05 on the 1st of each month). */
	private String monthlyArchiveCron = "0 5 0 1 * ?";

	private boolean accessLogEnabled = true;

	private String accessLogFileName = "localhost_access_log";

	private boolean catalinaOutEnabled = true;

	private boolean bridgeApplicationPackage = true;

	private String applicationPackage = "com.sbp";

	private LevelConfig level = new LevelConfig();

	private List<String> archiveBaseNames = defaultArchiveBaseNames();

	private static List<String> defaultArchiveBaseNames() {
		List<String> names = new ArrayList<>();
		names.add("ApplicationLog");
		names.add("SecurityLog");
		names.add("AuthenticationLog");
		names.add("DatabaseLog");
		names.add("ErrorLog");
		names.add("catalina");
		names.add("localhost_access_log");
		return names;
	}

	@Getter
	@Setter
	public static class LevelConfig {

		private String application = "INFO";

		private String security = "INFO";

		private String authentication = "INFO";

		private String database = "INFO";

		private String root = "INFO";

	}

}
