package com.sbp.common.logging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbp.logging")
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getArchiveDirectory() {
		return archiveDirectory;
	}

	public void setArchiveDirectory(String archiveDirectory) {
		this.archiveDirectory = archiveDirectory;
	}

	public boolean isDailyRollover() {
		return dailyRollover;
	}

	public void setDailyRollover(boolean dailyRollover) {
		this.dailyRollover = dailyRollover;
	}

	public boolean isMonthlyArchive() {
		return monthlyArchive;
	}

	public void setMonthlyArchive(boolean monthlyArchive) {
		this.monthlyArchive = monthlyArchive;
	}

	public String getMonthlyArchiveCron() {
		return monthlyArchiveCron;
	}

	public void setMonthlyArchiveCron(String monthlyArchiveCron) {
		this.monthlyArchiveCron = monthlyArchiveCron;
	}

	public boolean isAccessLogEnabled() {
		return accessLogEnabled;
	}

	public void setAccessLogEnabled(boolean accessLogEnabled) {
		this.accessLogEnabled = accessLogEnabled;
	}

	public String getAccessLogFileName() {
		return accessLogFileName;
	}

	public void setAccessLogFileName(String accessLogFileName) {
		this.accessLogFileName = accessLogFileName;
	}

	public boolean isCatalinaOutEnabled() {
		return catalinaOutEnabled;
	}

	public void setCatalinaOutEnabled(boolean catalinaOutEnabled) {
		this.catalinaOutEnabled = catalinaOutEnabled;
	}

	public boolean isBridgeApplicationPackage() {
		return bridgeApplicationPackage;
	}

	public void setBridgeApplicationPackage(boolean bridgeApplicationPackage) {
		this.bridgeApplicationPackage = bridgeApplicationPackage;
	}

	public String getApplicationPackage() {
		return applicationPackage;
	}

	public void setApplicationPackage(String applicationPackage) {
		this.applicationPackage = applicationPackage;
	}

	public LevelConfig getLevel() {
		return level;
	}

	public void setLevel(LevelConfig level) {
		this.level = level;
	}

	public List<String> getArchiveBaseNames() {
		return archiveBaseNames;
	}

	public void setArchiveBaseNames(List<String> archiveBaseNames) {
		this.archiveBaseNames = archiveBaseNames;
	}

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

	public static class LevelConfig {

		private String application = "INFO";

		private String security = "INFO";

		private String authentication = "INFO";

		private String database = "INFO";

		private String root = "INFO";

		public String getApplication() {
			return application;
		}

		public void setApplication(String application) {
			this.application = application;
		}

		public String getSecurity() {
			return security;
		}

		public void setSecurity(String security) {
			this.security = security;
		}

		public String getAuthentication() {
			return authentication;
		}

		public void setAuthentication(String authentication) {
			this.authentication = authentication;
		}

		public String getDatabase() {
			return database;
		}

		public void setDatabase(String database) {
			this.database = database;
		}

		public String getRoot() {
			return root;
		}

		public void setRoot(String root) {
			this.root = root;
		}

	}

}
