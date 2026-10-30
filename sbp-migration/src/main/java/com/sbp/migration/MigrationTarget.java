package com.sbp.migration;

record MigrationTarget(String changelogPath, String jdbcUrl, String username, String password, String host,
		String port, String databaseName) {

	static MigrationTarget authFromEnv() {
		return fromEnv("AUTH", "migration/auth/db.changelog-master.yaml");
	}

	static MigrationTarget userFromEnv() {
		return fromEnv("USER", "migration/user/db.changelog-master.yaml");
	}

	static MigrationTarget personFromEnv() {
		return fromEnv("PERSON", "migration/person/db.changelog-master.yaml");
	}

	static MigrationTarget studioFromEnv() {
		return fromEnv("STUDIO", "migration/studio/db.changelog-master.yaml");
	}

	static MigrationTarget contentFromEnv() {
		return fromEnv("CONTENT", "migration/content/db.changelog-master.yaml");
	}

	static MigrationTarget referenceFromEnv() {
		return fromEnv("REFERENCE", "migration/reference/db.changelog-master.yaml");
	}

	private static MigrationTarget fromEnv(String prefix, String changelogPath) {
		String host = require(prefix + "_DB_HOST");
		String port = env(prefix + "_DB_PORT", "5432");
		String name = require(prefix + "_DB_NAME");
		String user = require(prefix + "_DB_USERNAME");
		String password = require(prefix + "_DB_PASSWORD");
		String timezone = env("APP_TIMEZONE", "Asia/Kolkata");
		String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + name + "?TimeZone=" + timezone;
		return new MigrationTarget(changelogPath, jdbcUrl, user, password, host, port, name);
	}

	String jdbcUrlForLog() {
		return host + ":" + port + "/" + databaseName;
	}

	private static String require(String key) {
		String value = System.getenv(key);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing required environment variable: " + key);
		}
		return value;
	}

	private static String env(String key, String defaultValue) {
		String value = System.getenv(key);
		return value == null || value.isBlank() ? defaultValue : value;
	}

}
