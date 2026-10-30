package com.sbp.reference.mapper;

import com.sbp.reference.dto.EntityTaxonomyResponse;
import com.sbp.reference.dto.SocialPlatformResponse;
import com.sbp.reference.dto.TaxonomyResponse;
import com.sbp.reference.entity.EntityTaxonomy;
import com.sbp.reference.entity.SocialPlatform;
import com.sbp.reference.entity.Taxonomy;

public final class ReferenceMapper {

	private ReferenceMapper() {
	}

	public static TaxonomyResponse toTaxonomyResponse(Taxonomy entity) {
		return new TaxonomyResponse(
				entity.getId(),
				entity.getType(),
				entity.getName(),
				entity.getSlug(),
				entity.getDescription(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getCreatedBy(),
				entity.getUpdatedBy(),
				entity.getVersion());
	}

	public static SocialPlatformResponse toSocialPlatformResponse(SocialPlatform entity) {
		return new SocialPlatformResponse(
				entity.getId(),
				entity.getName(),
				entity.getSlug(),
				entity.getBaseUrl(),
				entity.getSvgIcon(),
				entity.getBrandColor(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getCreatedBy(),
				entity.getUpdatedBy(),
				entity.getVersion());
	}

	public static EntityTaxonomyResponse toEntityTaxonomyResponse(EntityTaxonomy entity) {
		return new EntityTaxonomyResponse(
				entity.getId(),
				entity.getEntityId(),
				entity.getEntityType(),
				entity.getTaxonomy().getId(),
				entity.getCreatedAt());
	}

}
