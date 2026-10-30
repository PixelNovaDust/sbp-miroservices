package com.sbp.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SbpLoggerImpl implements SbpLogger {

	private final Logger application = LoggerFactory.getLogger(LogNames.APPLICATION);

	private final Logger security = LoggerFactory.getLogger(LogNames.SECURITY);

	private final Logger authentication = LoggerFactory.getLogger(LogNames.AUTHENTICATION);

	private final Logger database = LoggerFactory.getLogger(LogNames.DATABASE);

	@Override
	public Logger application() {
		return application;
	}

	@Override
	public Logger security() {
		return security;
	}

	@Override
	public Logger authentication() {
		return authentication;
	}

	@Override
	public Logger database() {
		return database;
	}

}
