package com.sbp.migration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Locale;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Applies Liquibase changelogs for auth, user, person, studio, content, reference, and file-storage databases (env-driven JDBC URLs).
 */
public final class SbpMigrationRunner {

	private SbpMigrationRunner() {
	}

	public static void main(String[] args) throws Exception {
		runTarget("auth", MigrationTarget.authFromEnv());
		runTarget("user", MigrationTarget.userFromEnv());
		runTarget("person", MigrationTarget.personFromEnv());
		runTarget("studio", MigrationTarget.studioFromEnv());
		runTarget("content", MigrationTarget.contentFromEnv());
		runTarget("reference", MigrationTarget.referenceFromEnv());
		runTarget("file-storage", MigrationTarget.fileFromEnv());
		System.out.println("All migrations completed successfully.");
	}

	private static boolean clearChecksumsEnabled() {
		// Custom flag (not a Liquibase env var — avoids Liquibase 4.x "invalid LIQUIBASE_*" warnings).
		return "true".equalsIgnoreCase(System.getenv("SBP_CLEAR_LIQUIBASE_CHECKSUMS"))
				|| "true".equalsIgnoreCase(System.getenv("LIQUIBASE_CLEAR_CHECKSUMS"));
	}

	private static void runTarget(String label, MigrationTarget target) throws Exception {
		System.out.printf(Locale.ROOT, "Running %s DB migrations (%s)...%n", label, target.jdbcUrlForLog());
		try (Connection connection = DriverManager.getConnection(target.jdbcUrl(), target.username(), target.password())) {
			Database database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new JdbcConnection(connection));
			Liquibase liquibase = new Liquibase(target.changelogPath(), new ClassLoaderResourceAccessor(),
					database);
			if (clearChecksumsEnabled()) {
				System.out.printf(Locale.ROOT, "Clearing stored checksums for %s DB...%n", label);
				liquibase.clearCheckSums();
			}
			liquibase.update("");
		}
		System.out.printf(Locale.ROOT, "%s DB migrations finished.%n", label);
	}

}
