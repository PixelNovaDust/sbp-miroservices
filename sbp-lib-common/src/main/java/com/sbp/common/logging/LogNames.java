package com.sbp.common.logging;

/**
 * SLF4J logger names bound to file appenders in {@code logback-spring-sbp.xml}.
 */
public final class LogNames {

	public static final String APPLICATION = "com.sbp.logging.application";

	public static final String SECURITY = "com.sbp.logging.security";

	public static final String AUTHENTICATION = "com.sbp.logging.authentication";

	public static final String DATABASE = "com.sbp.logging.database";

	private LogNames() {
	}

}
