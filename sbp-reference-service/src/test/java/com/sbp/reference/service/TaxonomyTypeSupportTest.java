package com.sbp.reference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.sbp.reference.exception.InvalidTaxonomyTypeException;

class TaxonomyTypeSupportTest {

	@Test
	void normalizeType_mapsLegacySpecialty() {
		assertEquals("SPECIALTIES", TaxonomyTypeSupport.normalizeType("specialty"));
		assertEquals("MOOD_TAG", TaxonomyTypeSupport.normalizeType(" mood_tag "));
	}

	@Test
	void normalizeType_rejectsUnknown() {
		assertThrows(InvalidTaxonomyTypeException.class, () -> TaxonomyTypeSupport.normalizeType("   "));
	}

}
