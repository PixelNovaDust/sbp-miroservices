package com.sbp.content.spec;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.sbp.content.domain.ContentStatus;
import com.sbp.content.domain.ContentType;
import com.sbp.content.domain.ContentVisibility;
import com.sbp.content.entity.Content;

public final class ContentSpecifications {

	private ContentSpecifications() {
	}

	public static Specification<Content> filters(String title, ContentType contentType, ContentStatus status,
			ContentVisibility visibility) {
		return Specification.allOf(
				titleContains(title),
				contentTypeEquals(contentType),
				statusEquals(status),
				visibilityEquals(visibility));
	}

	private static Specification<Content> titleContains(String title) {
		return (root, query, cb) -> {
			if (!StringUtils.hasText(title)) {
				return cb.conjunction();
			}
			return cb.like(cb.lower(root.get("title")), "%" + title.trim().toLowerCase() + "%");
		};
	}

	private static Specification<Content> contentTypeEquals(ContentType contentType) {
		return (root, query, cb) -> contentType == null ? cb.conjunction() : cb.equal(root.get("contentType"), contentType);
	}

	private static Specification<Content> statusEquals(ContentStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
	}

	private static Specification<Content> visibilityEquals(ContentVisibility visibility) {
		return (root, query, cb) -> visibility == null ? cb.conjunction() : cb.equal(root.get("visibility"), visibility);
	}

}
