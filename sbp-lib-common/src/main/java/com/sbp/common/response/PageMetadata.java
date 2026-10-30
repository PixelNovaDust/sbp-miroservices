package com.sbp.common.response;

/**
 * Pagination metadata for list responses.
 */
public record PageMetadata(
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean hasNext,
		boolean hasPrevious) {

	public static PageMetadata of(int page, int size, long totalElements) {
		int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / (double) size);
		boolean hasNext = page + 1 < totalPages;
		boolean hasPrevious = page > 0 && totalPages > 0;
		return new PageMetadata(page, size, totalElements, totalPages, hasNext, hasPrevious);
	}

}
