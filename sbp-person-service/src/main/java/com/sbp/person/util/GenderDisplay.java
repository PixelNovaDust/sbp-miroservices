package com.sbp.person.util;

public final class GenderDisplay {

	private GenderDisplay() {
	}

	public static String toDisplay(String gender) {
		if (gender == null || gender.isBlank()) {
			return null;
		}
		return switch (gender.trim().toUpperCase()) {
			case "MALE" -> "Male";
			case "FEMALE" -> "Female";
			case "TRANS" -> "Trans";
			default -> gender;
		};
	}

}
