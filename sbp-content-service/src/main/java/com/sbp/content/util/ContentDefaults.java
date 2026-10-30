package com.sbp.content.util;

import com.sbp.content.dto.ContentDetailResponse.ContentStatsResponse;

public final class ContentDefaults {

	private ContentDefaults() {
	}

	public static ContentStatsResponse defaultStats() {
		return new ContentStatsResponse(0, 0, 0);
	}

}
