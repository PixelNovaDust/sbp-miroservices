package com.sbp.studio.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StudioSlugSupportTest {

	@Test
	void resolveFromNameWhenSlugOmitted() {
		assertEquals("reality-kings", StudioSlugSupport.resolveForCreate(null, "Reality Kings"));
	}

	@Test
	void normalizeProvidedSlug() {
		assertEquals("brazzers", StudioSlugSupport.normalize("  Brazzers "));
	}

	@Test
	void ignoresAccidentalRandomSuffixOnCreate() {
		assertEquals("brazzers", StudioSlugSupport.resolveForCreate("brazzers-341998372", "Brazzers"));
	}

}
