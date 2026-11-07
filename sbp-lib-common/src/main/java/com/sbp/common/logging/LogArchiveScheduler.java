package com.sbp.common.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor

@ConditionalOnProperty(prefix = "sbp.logging", name = "monthly-archive", havingValue = "true", matchIfMissing = true)
public class LogArchiveScheduler {

	private final LogArchiveService archiveService;


	@Scheduled(cron = "${sbp.logging.monthly-archive-cron:0 5 0 1 * ?}")
	public void runMonthlyArchive() {
		archiveService.archivePreviousMonth();
	}

}
