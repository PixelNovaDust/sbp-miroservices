package com.sbp.studio.mapper;

import com.sbp.studio.dto.StudioAuditResponse;
import com.sbp.studio.dto.StudioResponse;
import com.sbp.studio.entity.Studio;

public final class StudioMapper {

	private StudioMapper() {
	}

	public static StudioResponse toResponse(Studio entity, String logoUrl) {
		return new StudioResponse(
				entity.getId(),
				entity.getName(),
				entity.getSlug(),
				entity.getWebsite(),
				entity.getDescription(),
				entity.getLogo(),
				entity.getSvg(),
				logoUrl,
				entity.isActive(),
				new StudioAuditResponse(entity.getCreatedAt(), entity.getUpdatedAt(), entity.getVersion()));
	}

}
