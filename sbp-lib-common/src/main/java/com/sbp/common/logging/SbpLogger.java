package com.sbp.common.logging;

import org.slf4j.Logger;

/**
 * Injectable facade over category SLF4J loggers. Each category writes to its own rolling file
 * under {@code sbp.logging.directory}; errors are also copied to {@code ErrorLog.log}.
 */
public interface SbpLogger {

	Logger application();

	Logger security();

	Logger authentication();

	Logger database();

}
