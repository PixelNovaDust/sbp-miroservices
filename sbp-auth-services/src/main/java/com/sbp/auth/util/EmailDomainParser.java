package com.sbp.auth.util;

public final class EmailDomainParser {

	private EmailDomainParser() {
	}

	public static String domainFromEmail(String email) {
		if (email == null || email.isBlank()) {
			return "";
		}
		int at = email.lastIndexOf('@');
		if (at < 0 || at == email.length() - 1) {
			return "";
		}
		return email.substring(at + 1).trim().toLowerCase();
	}

}
