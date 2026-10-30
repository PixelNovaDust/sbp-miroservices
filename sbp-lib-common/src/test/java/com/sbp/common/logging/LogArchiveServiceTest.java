package com.sbp.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LogArchiveServiceTest {

	@Test
	void listsDailyFilesForTargetMonth(@TempDir Path temp) throws Exception {
		SbpLoggingProperties props = new SbpLoggingProperties();
		props.setDirectory(temp.toString());
		LogArchiveService service = new LogArchiveService(props);

		Files.writeString(temp.resolve("ApplicationLog.2026-09-15.log"), "line");
		Files.writeString(temp.resolve("ApplicationLog.2026-10-01.log"), "line");
		Files.writeString(temp.resolve("ApplicationLog.log"), "active");

		List<Path> september = service.listDailyFilesForMonth(temp, "ApplicationLog", YearMonth.of(2026, 9));
		assertThat(september).hasSize(1);
		assertThat(september.get(0).getFileName().toString()).isEqualTo("ApplicationLog.2026-09-15.log");
	}

}
